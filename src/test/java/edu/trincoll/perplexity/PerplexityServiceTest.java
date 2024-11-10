package edu.trincoll.perplexity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static edu.trincoll.perplexity.PerplexityRecords.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "PERPLEXITY_API_KEY", matches = ".*")
class PerplexityServiceTest {
    private final PerplexityService service = new PerplexityService();

    @Test
    void chat() {
        Message systemMessage = new Message("system", """
                You are an artificial intelligence assistant and you need to
                engage in a helpful, detailed, polite conversation with a user.
                """);
        Message userMessage = new Message("user",
                "How many stars are in the sky?");
        var request = new ChatCompletionRequest(
                "llama-3.1-sonar-small-128k-online",
                List.of(systemMessage, userMessage)
        );
        ChatCompletionResponse response = service.chat(request);

        assertNotNull(response);
        String content = response.choices().getFirst().message().content();
        System.out.println(content);

        assertFalse(response.citations().isEmpty());
        System.out.println("\nCitations:");
        for (int i = 0; i < response.citations().size(); i++) {
            System.out.println("[" + (i+1) + "] " + response.citations().get(i));
        }
    }
}