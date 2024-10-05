package edu.trincoll.blackforestlabs;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
import java.util.concurrent.TimeUnit;

public class BFLImageGenerationService {
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

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            ApiResponse apiResponse = gson.fromJson(response.body(), ApiResponse.class);
            return apiResponse.id();
        }
    }

    // Method to poll for the result
    public String pollForResult(String requestId) throws Exception {
        try (var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()) {

            while (true) {
                TimeUnit.MILLISECONDS.sleep(500);

                var getResultRequest = HttpRequest.newBuilder()
                        .uri(URI.create(RESULT_URL + "?id=" + requestId))
                        .header("accept", "application/json")
                        .header("x-key", API_KEY)
                        .build();

                HttpResponse<String> resultResponse =
                        client.send(getResultRequest, HttpResponse.BodyHandlers.ofString());
                ApiResponse resultApiResponse = gson.fromJson(resultResponse.body(), ApiResponse.class);

                Status status = resultApiResponse.status();
                if (status == null) {
                    status = Status.Unknown;
                }

                switch (status) {
                    case Ready -> {
                        return resultApiResponse.result().sample();  // Returning the URL to the generated image
                    }
                    case TaskNotFound -> throw new IllegalStateException("Error: Task not found");
                    case Failed -> throw new IllegalStateException("Error: Task failed");
                    case Unknown, Pending, InProgress -> System.out.println("Status: " + status);  // Handle statuses that require waiting or unknown handling
                }
            }
        }
    }

    // Method to download and save the image to src/main/resources
    public void downloadAndSaveImage(String imageUrl)
            throws IOException, InterruptedException {
        // Generate the timestamp-based filename
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "generated_image_" + timestamp + ".jpg";

        // Path to save the file (src/main/resources)
        Path outputPath = Paths.get("src/main/resources", fileName);

        // Using try-with-resources for AutoCloseable HttpClient
        try (HttpClient client = HttpClient.newBuilder().build()) {
            // Send GET request to download and directly save the image to file
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .build();

            // Using ofFile to directly save the response body into the file
            HttpResponse<Path> response =
                    client.send(request, HttpResponse.BodyHandlers.ofFile(outputPath));

            // Check if the response is successful (HTTP status 200)
            if (response.statusCode() == 200) {
                System.out.println("Image saved successfully to: " + response.body().toAbsolutePath());
            } else {
                throw new IOException("Failed to download the image. HTTP Status Code: " + response.statusCode());
            }
        }
    }
}