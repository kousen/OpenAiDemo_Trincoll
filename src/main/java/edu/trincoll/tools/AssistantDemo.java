package edu.trincoll.tools;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;

public class AssistantDemo {
    public static void main(String[] args) {
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .build();

        Assistant assistant = AiServices.create(Assistant.class, chatModel);
        System.out.println(assistant.answer("What is the meaning of life?"));
    }
}
