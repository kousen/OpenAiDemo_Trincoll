package edu.trincoll;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class OpenAiLangChain4jTest {
    private final String apiKey = System.getenv("OPENAI_API_KEY");

    @Test
    void chat() {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .build();
        ChatResponse response = chatModel.chat(ChatRequest.builder()
                .messages(List.of(new UserMessage("What was the score of the last Super Bowl?")))
                .build());
        System.out.println(response.aiMessage().text());
        System.out.println("Input tokens: " + response.tokenUsage().inputTokenCount());
        System.out.println("Output tokens: " + response.tokenUsage().outputTokenCount());
    }

}
