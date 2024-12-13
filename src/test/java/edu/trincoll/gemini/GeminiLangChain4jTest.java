package edu.trincoll.gemini;

import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@EnabledIfEnvironmentVariable(named = "GOOGLEAI_API_KEY", matches = ".*")
public class GeminiLangChain4jTest {
    private final ChatLanguageModel geminiFlashModel = GoogleAiGeminiChatModel.builder()
            .apiKey(System.getenv("GOOGLEAI_API_KEY"))
            .modelName("gemini-exp-1121")
            .build();

    @Test
    void generateFromString() {
        String answer = geminiFlashModel.generate(
                "Explain to me how AI works");
        System.out.println(answer);
    }

    @Test
    void generateFromUserAndSystemMessage() {
        Response<AiMessage> response = geminiFlashModel.generate(
                SystemMessage.from("""
                        You are an artificial intelligence assistant and you need to
                        engage in a helpful, detailed, polite conversation with a user.
                        """),
                UserMessage.from("Explain to me how AI works"));
        System.out.println("Finish reason: " + response.finishReason());
        System.out.println("Metadata: " + response.metadata());
        System.out.println("Answer: " + response.content()
                .text());
        System.out.println("Token usage: " + response.tokenUsage());
    }

    @Test @Disabled("Gemini doesn't support remote image URLs")
    void visionChatFromRemotePublicURL() {
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/0/0f/023_Glossy_ibis_fishing_in_the_Camargue_Photo_by_Giles_Laurent.jpg";
        Response<AiMessage> response = geminiFlashModel.generate(
                UserMessage.from(
                        ImageContent.from(imageUrl),
                        TextContent.from("What do you see?")
                )
        );
        System.out.println(response.content().text());
    }

    @Test @Disabled("Gemini doesn't support local file URLs either")
    void visionChatFromLocalFileURL() {
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/0/0f/023_Glossy_ibis_fishing_in_the_Camargue_Photo_by_Giles_Laurent.jpg";
        String localFileUrl = downloadImageToFileAndGetUrl(imageUrl);
        if (localFileUrl != null) {
            Response<AiMessage> response = geminiFlashModel.generate(
                    UserMessage.from(
                            ImageContent.from(localFileUrl),
                            TextContent.from("What do you see?")
                    )
            );
            System.out.println(response.content().text());
        }
    }

    private static String downloadImageToFileAndGetUrl(String imageUrl) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .build();
            Path tempFile = Files.createTempFile("downloaded_image", ".jpg");
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to download image: HTTP code " + response.statusCode());
            }
            return tempFile.toUri().toString();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to download image", e);
        }
    }

    @Test
    void visionChatWithBase64EncodedImage() {
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/0/0f/023_Glossy_ibis_fishing_in_the_Camargue_Photo_by_Giles_Laurent.jpg";
        String base64Image = downloadImageAsBase64(imageUrl);
        if (base64Image != null) {
            Response<AiMessage> response = geminiFlashModel.generate(
                    UserMessage.from(
                            ImageContent.from(base64Image, "image/jpeg"),
                            TextContent.from("What do you see?")
                    )
            );
            System.out.println(response.content().text());
        }
    }

    private static String downloadImageAsBase64(String imageUrl) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to download image: HTTP code " + response.statusCode());
            }
            // Convert the byte array to a Base64 encoded string
            byte[] imageBytes = response.body();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to download image", e);
        }
    }

}
