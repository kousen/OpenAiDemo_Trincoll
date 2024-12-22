package edu.trincoll.anthropic;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModelName;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class QuizGenerator {
    private final ChatLanguageModel chatModel = AnthropicChatModel.builder()
            .apiKey(System.getenv("ANTHROPIC_API_KEY"))
            .modelName(AnthropicChatModelName.CLAUDE_3_5_SONNET_20241022)
            .maxTokens(4096)
            .build();

    private final QuizAssistant assistant = AiServices.builder(QuizAssistant.class)
            .chatLanguageModel(chatModel)
            .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
            .build();

    public interface QuizAssistant {
        @SystemMessage("""
                You are an expert at undergraduate education.
                Your job is to generating quizzes for undergraduates
                on the given topic. The quizzes are for students
                attending a course called "Software Design using Kotlin",
                so any code-related questions should use that language.
                
                The resulting quiz must be in Moodle XML
                format, so they can be imported into an online
                learning management system.
                """)
        @UserMessage("""
                Please generate ten questions on
                the topic of {{topic}}, suitable for an online
                quiz given using Moodle. Do not include anything
                other than the XML response with questions, answers,
                and feedback, and be sure to include the complete
                response so the result is a valid XML file.
                """)
        String generateQuiz(@V("topic") String topic);
    }

    public String generateQuiz(String topic) {
        return assistant.generateQuiz(topic);
    }

    public void writeToXmlFile(String content, String filePath) {
        try {
            Files.writeString(Paths.get("src/main/resources/" + filePath), content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
