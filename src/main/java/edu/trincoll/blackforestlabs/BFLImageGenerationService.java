package edu.trincoll.blackforestlabs;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class BFLImageGenerationService {
    private static final String API_URL = "https://api.bfl.ml/v1/flux-pro-1.1";
    private static final String RESULT_URL = "https://api.bfl.ml/v1/get_result";
    private static final String API_KEY = System.getenv("BFL_API_KEY");

    public record ApiResponse(String id, String status, Result result) {
        record Result(String sample) {} // The sample is expected to be the URL to the generated image
    }

    private final Gson gson = new Gson();

    // Method to send the image generation request
    public String requestImageGeneration(String prompt, int width, int height) throws Exception {
        String requestBody = """
                {
                    "prompt": "%s",
                    "width": %d,
                    "height": %d
                }
                """.formatted(prompt, width, height);

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
                //System.out.println("Raw response: " + resultResponse.body());
                ApiResponse resultApiResponse = gson.fromJson(resultResponse.body(), ApiResponse.class);

                String status = resultApiResponse.status();
                if (status == null) {
                    status = "Unknown";
                }

                if (status.equals("Ready")) {
                    return resultApiResponse.result().sample();  // Returning the URL to the generated image
                } else if (status.equals("TaskNotFound")) {
                    throw new IllegalStateException("Error: Task not found");
                } else if (status.equals("Failed")) {
                    throw new IllegalStateException("Error: Task failed");
                } else {
                    System.out.println("Status: " + status);  // Handle statuses that require waiting or unknown handling
                }
            }
        }
    }
}