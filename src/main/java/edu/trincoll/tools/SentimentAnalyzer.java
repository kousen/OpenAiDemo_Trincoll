package edu.trincoll.tools;

import dev.langchain4j.service.UserMessage;

public interface SentimentAnalyzer {

    enum Sentiment {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    @UserMessage("Does {{it}} have a positive sentiment?")
    Sentiment isPositive(String text);

}