package edu.trincoll.gemini;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "GOOGLEAI_API_KEY", matches = ".*")
public class GeminiOpenAiLangChain4jTest {
    private final ChatLanguageModel geminiFlashModel = OpenAiChatModel.builder()
            .apiKey(System.getenv("GOOGLEAI_API_KEY"))
            .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai/")
            .modelName("gemini-1.5-flash")
//            .logResponses(true)
//            .logRequests(true)
            .build();

    @Test
    void generateFromString() {
        String answer = geminiFlashModel.generate(
                "How many stars are in the sky?");
        System.out.println(answer);
    }

    @Test
    void generateFromUserMessage() {
        Response<AiMessage> response = geminiFlashModel.generate(
                SystemMessage.from("""
                        You are an artificial intelligence assistant and you need to
                        engage in a helpful, detailed, polite conversation with a user.
                        """),
                UserMessage.from("How many stars are in the sky?"));
        System.out.println("Finish reason: " + response.finishReason());
        System.out.println("Metadata: " + response.metadata());
        System.out.println("Answer: " + response.content()
                .text());
        System.out.println("Token usage: " + response.tokenUsage());
    }
}