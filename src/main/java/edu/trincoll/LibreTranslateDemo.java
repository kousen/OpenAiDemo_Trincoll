package edu.trincoll;

import com.google.gson.Gson;
import net.suuft.libretranslate.Language;
import net.suuft.libretranslate.Translator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LibreTranslateDemo {
    private final static String LOCAL_TRANSLATE_URL = "http://localhost:5001/translate";

    private final Gson gson = new Gson();

    public record TranslateRequest(String source, String target, String q) {}
    public record TranslateResponse(String translatedText) {}

    public String translate(TranslateRequest request) {
        String text = gson.toJson(request);
        try (var httpClient = HttpClient.newHttpClient()) {
            var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(LOCAL_TRANSLATE_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(text))
                .build();
            var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            var json = gson.fromJson(response.body(), TranslateResponse.class);
            return json.translatedText();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        String translation = Translator.translate(
                Language.ENGLISH, Language.SPANISH, "Hello, World!");
        System.out.println(translation);
    }
}
