package edu.trincoll.perplexity;

import java.util.List;

public class PerplexityRecords {
    public record Message(String role, String content) {}

    public record ChatCompletionRequest(
            String model,
            List<Message> messages,
            Integer maxTokens,
            Double temperature,
            Double topP,
            Integer topK,
            Boolean stream
    ) {
        public ChatCompletionRequest(String model, List<Message> messages) {
            this(model, messages, 1500, 0.2, 0.9, 0, false);
        }
    }

    public record Choice(
            int index,
            String finishReason,
            Message message
    ) {}

    public record Usage(
            int promptTokens,
            int completionTokens,
            int totalTokens
    ) {}

    public record ChatCompletionResponse(
            String id,
            String model,
            String object,
            long created,
            List<Choice> choices,
            Usage usage,
            List<String> citations
    ) {}
}
