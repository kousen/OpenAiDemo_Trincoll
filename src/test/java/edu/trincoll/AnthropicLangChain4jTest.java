package edu.trincoll;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModelName;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

public class AnthropicLangChain4jTest {
    private final ChatLanguageModel chatModel = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName(AnthropicChatModelName.CLAUDE_3_HAIKU_20240307)
                //.modelName("o1-preview")
                .build();

    public String downloadImageAndConvertToBase64(String imageUrl) throws IOException, InterruptedException {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            return switch (response.statusCode()) {
                case 200 -> Base64.getEncoder().encodeToString(response.body());
                case 404 -> throw new IOException("Image not found (404)");
                case 403 -> throw new IOException("Access forbidden (403)");
                default -> throw new IOException("Failed to download image. HTTP status code: " + response.statusCode());
            };
        }
    }

    @Test @Disabled("Doesn't work for Claude models")
    void chatWithMessages() {
        ChatResponse response = chatModel.chat(ChatRequest.builder()
                .messages(List.of(new UserMessage("""
                    What is the Ultimate Answer to
                    the Ultimate Question of
                    Life, the Universe, and Everything?""")))
                .build());
        System.out.println(response.aiMessage().text());
        System.out.println(response.tokenUsage());
    }

    @Test
    void chatWithString() {
        String answer = chatModel.generate("""
                Who is the best musician of the 2010s?
                """);
        System.out.println(answer);
    }

    @Test
    void visionChat() throws Exception {
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg";
        String encodedImage = downloadImageAndConvertToBase64(imageUrl);
        Response<AiMessage> response = chatModel.generate(
                UserMessage.from(
                        ImageContent.from(encodedImage, "image/jpeg"),
                        TextContent.from("What do you see?")
                )
        );
        System.out.println(response.content().text());
    }

    @Test
    void promptStuffing() {
        Document feudDoc = UrlDocumentLoader.load(
                "https://en.wikipedia.org/wiki/Drake%E2%80%93Kendrick_Lamar_feud",
                new TextDocumentParser());
        if (feudDoc.text().length() > 128 * 1024) {
            System.out.println("Document too large: " + feudDoc.text().length());
            System.out.println(feudDoc.text().substring(0, 1000));
            return;
        }

        String response = chatModel.generate(
                UserMessage.from(
                        TextContent.from("""
                            Given the information in %s,
                            What was the beef about between
                            Drake and Kendrick Lamar?
                            """.formatted(feudDoc.text()))))
                .content().text();
        System.out.println(response);
    }

}
