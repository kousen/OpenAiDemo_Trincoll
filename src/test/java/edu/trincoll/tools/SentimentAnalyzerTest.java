package edu.trincoll.tools;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;

class SentimentAnalyzerTest {

    private final ChatLanguageModel chatModel = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName(OpenAiChatModelName.GPT_4_O_MINI)
            .logRequests(true)
            .logResponses(true)
            .build();

    private final SentimentAnalyzer sentimentAnalyzer =
            AiServices.create(SentimentAnalyzer.class, chatModel);

    @Test
    void isPositive() {
        System.out.println(sentimentAnalyzer.isPositive("I am happy"));
        System.out.println(sentimentAnalyzer.isPositive("I am sad"));
        System.out.println(sentimentAnalyzer.isPositive("I am sort of okay."));
    }
}