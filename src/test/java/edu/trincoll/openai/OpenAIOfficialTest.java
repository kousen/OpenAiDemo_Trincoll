package edu.trincoll.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.*;
import org.junit.jupiter.api.Test;

// Based on sample code from
// openai-java-example/src/main/java/com/openai/example/Main.java
// in GitHub repo: https://github.com/openai/openai-java

public class OpenAIOfficialTest {

    private final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    @Test
    void completion() {
        ChatCompletionCreateParams completionCreateParams = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addMessage(ChatCompletionMessageParam.ofChatCompletionUserMessageParam(
                        ChatCompletionUserMessageParam.builder()
                                .role(ChatCompletionUserMessageParam.Role.USER)
                                .content(ChatCompletionUserMessageParam.Content.ofTextContent(
                                        "Tell me a story about building the best SDK!"))
                                .build()))
                .build();

        // Non-streaming example
        client.chat().completions().create(completionCreateParams).choices().stream()
                .flatMap(choice -> choice.message().content().stream())
                .forEach(System.out::println);

        System.out.println("\n-----------------------------------\n");

        // Streaming example
        try (StreamResponse<ChatCompletionChunk> messageStreamResponse =
                     client.chat().completions().createStreaming(completionCreateParams)) {
            messageStreamResponse.stream()
                    .flatMap(completion -> completion.choices().stream())
                    .flatMap(choice -> choice.delta().content().stream())
                    .forEach(System.out::print);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
