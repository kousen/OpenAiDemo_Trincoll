package edu.trincoll;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;

public class HelloGPT {
    public static void main(String[] args) {
        String endpoint = "https://api.openai.com/v1/models";
        String apiKey = System.getenv("OPENAI_API_KEY");

        record Model(String id, Long created) {}
        record ModelList(List<Model> data) {}

        Gson gson = new Gson();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + apiKey)
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            //System.out.println(response.body());
            ModelList modelList = gson.fromJson(response.body(), ModelList.class);
            modelList.data.stream()
                    .sorted(Comparator.comparing(Model::id))
                    .forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

    }
}
