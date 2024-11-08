package edu.trincoll.recraft;

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

import static edu.trincoll.recraft.RecraftRecords.*;

// See https://www.recraft.ai/docs
public class RecraftImageGenerationService {
    private static final String BASE_URL = "https://external.api.recraft.ai/v1/";
    private static final String API_KEY = System.getenv("RECRAFT_API_KEY");

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    // Method to send the image generation request
    public ImagesResponse requestImageGeneration(ImageRequest imageRequest) throws Exception {
        String requestBody = gson.toJson(imageRequest);

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "images/generations"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer %s".formatted(API_KEY))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            response.headers().map().forEach((k, v) -> System.out.println(k + ": " + v));
            return gson.fromJson(response.body(), ImagesResponse.class);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public String saveImage(String imageUrl)
            throws IOException, InterruptedException {
        // Generate the timestamp-based filename
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "generated_image_" + timestamp + ".webp";

        // Path to save the file (src/main/resources)
        Path outputPath = Paths.get("src/main/resources", fileName);

        // Using try-with-resources for AutoCloseable HttpClient
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .build();

            HttpResponse<Path> response =
                    client.send(request, HttpResponse.BodyHandlers.ofFile(outputPath));
            System.out.println("Headers: " + response.headers());

            if (response.statusCode() == 200) {
                return "Image saved successfully to: " + response.body().toAbsolutePath();
            } else {
                throw new IOException(
                        "Failed to download the image. HTTP Status Code: " + response.statusCode());
            }
        }
    }
}