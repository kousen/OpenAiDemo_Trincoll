package edu.trincoll;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class BFLApiClient {
    private static final Logger logger = LoggerFactory.getLogger(BFLApiClient.class);
    private static final String API_BASE_URL = "https://api.bfl.ml/v1";
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    public BFLApiClient() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public record FluxProInputs(
            String prompt,
            int width,
            int height,
            Integer steps,
            boolean promptUpsampling,
            Integer seed,
            Double guidance,
            Integer safetyTolerance,
            Double interval
    ) {
    }

    public record FluxProPlusInputs(
            String prompt,
            int width,
            int height,
            boolean promptUpsampling,
            Integer seed,
            Integer safetyTolerance
    ) {
    }

    public record FluxDevInputs(
            String prompt,
            int width,
            int height,
            Integer steps,
            boolean promptUpsampling,
            Integer seed,
            Double guidance,
            Integer safetyTolerance
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AsyncResponse(String id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultResponse(String id, String status, Result result) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(String sample) {
    }

    public <T> CompletableFuture<String> submitTask(String endpoint, T inputs) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String apiKey = getApiKey();
                String requestBody = objectMapper.writeValueAsString(inputs);

                HttpRequest postRequest = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL + endpoint))
                        .timeout(HTTP_TIMEOUT)
                        .header("Accept", "application/json")
                        .header("x-key", apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

                if (postResponse.statusCode() != 200) {
                    throw new BFLApiException("Failed to submit task. Status Code: " + postResponse.statusCode() +
                                              ", Response Body: " + postResponse.body());
                }

                AsyncResponse response = objectMapper.readValue(postResponse.body(), AsyncResponse.class);
                logger.info("{} POST Response: {}", endpoint, postResponse.body());

                return response.id();
            } catch (IOException | InterruptedException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    public CompletableFuture<String> execute(String taskType, Object inputs) {
        return CompletableFuture.supplyAsync(() ->
                        switch (taskType.toLowerCase()) {
                            case "flux-pro" -> submitTask("/flux-pro", inputs);
                            case "flux-pro-1.1" -> submitTask("/flux-pro-1.1", inputs);
                            case "flux-dev" -> submitTask("/flux-dev", inputs);
                            default -> throw new IllegalArgumentException("Unknown task type: " + taskType);
                        }, executor)
                .thenCompose(future -> future) // flatten the nested CompletableFuture
                .thenCompose(this::pollForResult)
                .thenApply(this::saveImage);
    }

    private CompletableFuture<String> pollForResult(String requestId) {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();

        Runnable pollTask = new Runnable() {
            @Override
            public void run() {
                try {
                    ResultResponse resultResponse = getResult(requestId);
                    switch (resultResponse) {
                        case ResultResponse(String id, String status, Result result)
                                when status.equalsIgnoreCase("Ready") -> resultFuture.complete(result.sample());
                        case ResultResponse(String id, String status, Result result)
                                when status.equalsIgnoreCase("Pending") ||
                                     status.equalsIgnoreCase("Request Moderated") ||
                                     status.equalsIgnoreCase("Content Moderated") -> {
                            logger.info("Status: {} | Waiting for result...", status);
                            scheduler.schedule(this, 500, TimeUnit.MILLISECONDS);
                        }
                        case ResultResponse(String id, String status, Result result)
                                when status.equalsIgnoreCase("Task not found") ->
                                resultFuture.completeExceptionally(new BFLApiException("Task not found with ID: " + requestId));
                        case ResultResponse(String id, String status, Result result)
                                when status.equalsIgnoreCase("Error") ->
                                resultFuture.completeExceptionally(new BFLApiException("An error occurred while processing the task with ID: " + requestId));
                        default -> {
                            logger.warn("Unhandled status: {}", resultResponse.status());
                            scheduler.schedule(this, 500, TimeUnit.MILLISECONDS);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    resultFuture.completeExceptionally(e);
                }
            }
        };

        scheduler.schedule(pollTask, 0, TimeUnit.MILLISECONDS);
        return resultFuture;
    }

    private ResultResponse getResult(String requestId) throws IOException, InterruptedException {
        String apiKey = getApiKey();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/get_result?id=" + requestId))
                .timeout(HTTP_TIMEOUT)
                .header("Accept", "application/json")
                .header("x-key", apiKey)
                .GET()
                .build();

        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

        if (getResponse.statusCode() != 200) {
            throw new BFLApiException("Failed to get result. Status Code: " + getResponse.statusCode() +
                                      ", Response Body: " + getResponse.body());
        }

        return objectMapper.readValue(getResponse.body(), ResultResponse.class);
    }

    private String saveImage(String imageUrl) {
        try {
            HttpRequest imageRequest = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path outputPath = Paths.get("src/main/resources", "generated_image_" + timestamp + ".png");

            HttpResponse<Path> imageResponse = httpClient.send(imageRequest, HttpResponse.BodyHandlers.ofFile(outputPath));

            if (imageResponse.statusCode() != 200) {
                throw new BFLApiException("Failed to download image. Status Code: " + imageResponse.statusCode());
            }

            logger.info("Image saved to: {}", outputPath);
            return imageUrl;
        } catch (IOException | InterruptedException e) {
            logger.error("Error saving image: ", e);
            throw new CompletionException(e);
        }
    }

    private String getApiKey() {
        String apiKey = System.getenv("BFL_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Environment variable BFL_API_KEY is not set.");
        }
        return apiKey;
    }

    public void close() {
        executor.shutdown();
        scheduler.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static class BFLApiException extends IOException {
        public BFLApiException(String message) {
            super(message);
        }
    }
}