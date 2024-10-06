package edu.trincoll.blackforestlabs;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static edu.trincoll.blackforestlabs.BFLRecords.*;

public class BFLImageGenerationService {
    private static final Logger logger = LoggerFactory.getLogger(BFLImageGenerationService.class);
    private static final String API_URL = "https://api.bfl.ml/v1/flux-pro-1.1";
    private static final String RESULT_URL = "https://api.bfl.ml/v1/get_result";
    private static final String API_KEY = System.getenv("BFL_API_KEY");

    private final Gson gson;

    public BFLImageGenerationService() {
        this.gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    // Method to send the image generation request
    public String requestImageGeneration(ImageRequest imageRequest) throws Exception {
        String requestBody = gson.toJson(imageRequest);

        try (var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("accept", "application/json")
                    .header("x-key", API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
                return apiResponse.id();
            } else {
                throw new IOException("Failed to request image generation. HTTP Status Code: " + response.statusCode());
            }
        }
    }

    // Refactored method to download the image using CompletableFuture
    public CompletableFuture<Path> downloadImageAsync(String requestId) {
        CompletableFuture<Path> future = new CompletableFuture<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        logger.info("Starting scheduled polling to check if image is ready for request ID: {}", requestId);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("Polling for image readiness for request ID: {}", requestId);
                if (isImageReady(requestId)) {
                    logger.info("Image is ready for request ID: {}", requestId);
                    scheduler.shutdown(); // Shut down the scheduler once the image is ready
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    Path outputPath = Paths.get("src/main/resources/output_image_" + timestamp + ".jpg");
                    saveImageToPath(requestId, outputPath);
                    logger.info("Image is available and saved to: {}", outputPath.toAbsolutePath());
                    future.complete(outputPath);
                } else {
                    logger.info("Image status is not ready yet for request ID: {}", requestId);
                }
            } catch (Exception e) {
                logger.error("An error occurred while checking image status or saving the image", e);
                future.completeExceptionally(e);
            }
        }, 0, 5, TimeUnit.SECONDS);

        return future;
    }

    // Method to check if the image is ready
    private boolean isImageReady(String requestId) throws IOException, InterruptedException {
        try (var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(RESULT_URL + "?id=" + requestId))
                    .header("accept", "application/json")
                    .header("x-key", API_KEY)
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
                return apiResponse.status() == Status.Ready;
            } else {
                throw new IOException("Failed to check image status. HTTP Status Code: " + response.statusCode());
            }
        }
    }

    // Updated method to save the image to a specified path
    private void saveImageToPath(String requestId, Path outputPath) throws IOException, InterruptedException {
        try (var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(RESULT_URL + "?id=" + requestId))
                    .header("x-key", API_KEY)
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
                if (apiResponse.status() == Status.Ready && apiResponse.result() != null) {
                    String imageUrl = apiResponse.result().sample();
                    downloadImage(imageUrl, outputPath);
                    logger.info("Image saved successfully to: {}", outputPath.toAbsolutePath());
                } else {
                    throw new IOException("Image not ready or result is null");
                }
            } else {
                throw new IOException("Failed to get image URL. HTTP Status Code: " + response.statusCode());
            }
        }
    }

    // New method to download the image from the URL
    private void downloadImage(String imageUrl, Path outputPath) throws IOException, InterruptedException {
        try (var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofFile(outputPath));

            if (response.statusCode() != 200) {
                throw new IOException("Failed to download the image. HTTP Status Code: " + response.statusCode());
            }
        }
    }
}