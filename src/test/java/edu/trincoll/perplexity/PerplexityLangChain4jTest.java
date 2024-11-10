package edu.trincoll.perplexity;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "PERPLEXITY_API_KEY", matches = ".*")
public class PerplexityLangChain4jTest {
    private final ChatLanguageModel perplexityModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("PERPLEXITY_API_KEY"))
                .baseUrl("https://api.perplexity.ai")
                .modelName("llama-3.1-sonar-small-128k-online")
                .build();

    @Test
    void generateFromString() {
        String answer = perplexityModel.generate(
                "How many stars are in the sky?");
        System.out.println(answer);
    }

    @Test
    void generateFromUserMessage() {
        Response<AiMessage> response = perplexityModel.generate(
                SystemMessage.from("""
                        You are an artificial intelligence assistant and you need to
                        engage in a helpful, detailed, polite conversation with a user.
                        """),
                UserMessage.from("How many stars are in the sky?"));
        System.out.println("Finish reason: " + response.finishReason());
        System.out.println("Metadata: " + response.metadata());
        System.out.println("Answer: " + response.content().text());
        System.out.println("Token usage: " + response.tokenUsage());
    }
}
