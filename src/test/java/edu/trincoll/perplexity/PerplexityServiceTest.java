package edu.trincoll.perplexity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.trincoll.perplexity.PerplexityRecords.*;
import static org.junit.jupiter.api.Assertions.*;

class PerplexityServiceTest {
    private final PerplexityService service = new PerplexityService();

    @Test
    void chat() {
        Message message = new Message("user", "How many stars are in the sky?");
        var request = new ChatCompletionRequest(
                "llama-3.1-sonar-small-128k-online",
                List.of(message)
        );
        ChatCompletionResponse response = service.chat(request);
        assertNotNull(response);
        String content = response.choices().getFirst().message().content();
        System.out.println(content);
        System.out.println("Citations:");
        for (int i = 0; i < response.citations().size(); i++) {
            System.out.println("[" + (i+1) + "] " + response.citations().get(i));
        }
    }
}