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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static edu.trincoll.blackforestlabs.BFLRecords.*;

public class BFLImageGenerationService {
    private static final String BASE_URL = "https://api.bfl.ml/v1";
    private static final String API_KEY = System.getenv("BFL_API_KEY");

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    // Method to send the image generation request
    public String requestImageGeneration(ImageRequest imageRequest) throws Exception {
        String requestBody = gson.toJson(imageRequest);

        try (var client = HttpClient.newHttpClient()) {

            var request = HttpRequest.newBuilder()
                    .uri(URI.create("%s/flux-pro-1.1".formatted(BASE_URL)))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("x-key", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            AsyncResponse asyncResponse =
                    gson.fromJson(response.body(), AsyncResponse.class);
            return asyncResponse.id();
        }
    }

    public String pollForResult(String requestId) throws Exception {
        try (var client = HttpClient.newHttpClient()) {

            var getResultRequest = HttpRequest.newBuilder()
                    .uri(URI.create("%s/get_result?id=%s".formatted(BASE_URL, requestId)))
                    .header("Accept", "application/json")
                    .header("x-key", API_KEY)
                    .build();

            while (true) {
                HttpResponse<String> resultResponse =
                        client.send(getResultRequest, HttpResponse.BodyHandlers.ofString());
                ApiResponse resultApiResponse =
                        gson.fromJson(resultResponse.body(), ApiResponse.class);
                Status status = resultApiResponse.status();

                switch (status) {
                    case Ready -> {
                        ApiResponse.Result result = resultApiResponse.result();
                        if (!result.prompt().isBlank())
                            System.out.println(result.prompt());
                        return downloadAndSaveImage(result.sample());
                    }
                    case TaskNotFound -> throw new IllegalStateException("Error: Task not found");
                    case Failed -> throw new IllegalStateException("Error: Task failed");
                    case Unknown -> throw new IllegalStateException("Error: Unknown status");
                    case Pending, InProgress -> {
                        TimeUnit.MILLISECONDS.sleep(500);
                        System.out.printf("Task is %s, waiting...%n", status);
                    }
                }
            }
        }
    }

    // Method to download and save the image to src/main/resources
    public String downloadAndSaveImage(String imageUrl)
            throws IOException, InterruptedException {
        // Generate the timestamp-based filename
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "generated_image_" + timestamp + ".jpg";

        // Path to save the file (src/main/resources)
        Path outputPath = Paths.get("src/main/resources", fileName);

        // Using try-with-resources for AutoCloseable HttpClient
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .build();

            HttpResponse<Path> response =
                    client.send(request, HttpResponse.BodyHandlers.ofFile(outputPath));

            if (response.statusCode() == 200) {
                return "Image saved successfully to: " + response.body().toAbsolutePath();
            } else {
                throw new IOException(
                        "Failed to download the image. HTTP Status Code: " + response.statusCode());
            }
        }
    }
}