package edu.trincoll.openai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;

public class PerplexityLangChain4jTest {
    @Test
    void perplexity() {
        ChatLanguageModel perplexityModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("PERPLEXITY_API_KEY"))
                .baseUrl("https://api.perplexity.ai")
                .modelName("llama-3.1-sonar-small-128k-online")
                .build();

        String answer = perplexityModel.generate(
                "How many r's are in the word 'strawberry'?");
        System.out.println(answer);
    }
}
