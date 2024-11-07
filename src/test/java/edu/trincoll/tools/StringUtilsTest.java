package edu.trincoll.tools;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    private final StringUtils utils = new StringUtils();

    private final ChatLanguageModel llama32 = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("llama3.2")
            .build();

    @Test
    void howManyTimesALetterAppearsInAString() {
        assertAll(
                () -> assertEquals(1,
                        utils.howManyTimesALetterAppearsInAString("hello", 'h')),
                () -> assertEquals(1,
                        utils.howManyTimesALetterAppearsInAString("hello", 'e')),
                () -> assertEquals(2,
                        utils.howManyTimesALetterAppearsInAString("hello", 'l')),
                () -> assertEquals(1,
                        utils.howManyTimesALetterAppearsInAString("hello", 'o')),
                () -> assertEquals(0,
                        utils.howManyTimesALetterAppearsInAString("hello", 'z'))
        );
    }

    @Test
    void strawberry_no_tools() {
        Assistant llama32NoTools = AiServices.builder(Assistant.class)
                .chatLanguageModel(llama32)
                .build();

        String question = """
                How many times does the letter 'r' appear in the word 'strawberry'?
                """;
        String answer = llama32NoTools.answer(question);
        System.out.println(answer);
        assertTrue(answer.contains("3") || answer.contains("three"));
    }

    @Test
    void strawberry_with_tools() {
        Assistant llama32WithTools = AiServices.builder(Assistant.class)
                .chatLanguageModel(llama32)
                .tools(utils)
                .build();

        String question = """
                How many times does the letter 'r' appear in the word 'strawberry'?
                """;
        String answer = llama32WithTools.answer(question);
        System.out.println(answer);
        assertTrue(answer.contains("3") || answer.contains("three"));
    }
}