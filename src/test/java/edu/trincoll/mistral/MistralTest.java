package edu.trincoll.mistral;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModelName;
import org.junit.jupiter.api.Test;

public class MistralTest {
    private final String apiKey = System.getenv("MISTRAL_API_KEY");

    private final ChatLanguageModel chatModel = MistralAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(MistralAiChatModelName.MISTRAL_LARGE_LATEST)
            .logRequests(true)
            .logResponses(true)
            .build();


    @Test
    void chatWithString() {
        String answer = chatModel.generate("""
                Who is the best musician of the 2010s?
                """);
        System.out.println(answer);
    }
}
