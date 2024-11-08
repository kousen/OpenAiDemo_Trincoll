package edu.trincoll.ollama;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import edu.trincoll.tools.Assistant;
import org.junit.jupiter.api.Test;

public class OllamaLC4jTest {
    @Test
    void ollama() {
        ChatLanguageModel llama32 = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3.2")
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(llama32)
                .tools(new StringUtils())
                .build();

        String answer = assistant.answer(
                "How many r's are in the word 'strawberry'?");
        System.out.println(answer);
    }
}
