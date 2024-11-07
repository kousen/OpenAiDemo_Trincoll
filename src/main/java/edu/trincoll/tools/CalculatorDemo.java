package edu.trincoll.tools;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;

public class CalculatorDemo {
    interface MathGenius {
        String ask(String question);
    }

    static class Calculator {

        @Tool
        public double add(int a, int b) {
            return a + b;
        }

        @Tool
        public double squareRoot(double x) {
            return Math.sqrt(x);
        }
    }

    public static void main(String[] args) {
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .build();
        MathGenius mathGenius = AiServices.builder(MathGenius.class)
                .chatLanguageModel(model)
                .tools(new Calculator())
                .build();

        String answer = mathGenius.ask("What is the square root of 475695037565?");

        System.out.println(answer); // The square root of 475695037565 is 689706.486532.

    }
}
