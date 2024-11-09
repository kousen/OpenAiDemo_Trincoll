package edu.trincoll.perplexity;

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

import static edu.trincoll.perplexity.PerplexityRecords.ChatCompletionRequest;
import static edu.trincoll.perplexity.PerplexityRecords.ChatCompletionResponse;

public class PerplexityService {
    public static final String BASE_URL = "https://api.perplexity.ai";
    public static final String API_KEY = System.getenv("PERPLEXITY_API_KEY");

    private static final Logger log = LoggerFactory.getLogger(PerplexityService.class);

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public ChatCompletionResponse chat(ChatCompletionRequest request) {
        String body = gson.toJson(request);

        try (var client = HttpClient.newHttpClient()) {
            var httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/chat/completions"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer %s".formatted(API_KEY))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            log.debug("Chat completion response: {}", response.body());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to complete chat: " + response.body());
            }
            return gson.fromJson(response.body(), ChatCompletionResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to complete chat", e);
        }
    }
}
