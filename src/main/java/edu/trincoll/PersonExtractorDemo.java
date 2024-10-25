package edu.trincoll;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;

import java.time.LocalDate;

public class PersonExtractorDemo {
    record Person(
            @Description("first name of a person")
            String firstName,
            String lastName,
            LocalDate birthDate,
            Address address
    ) {
    }

    @Description("an address")
    record Address(
            String street,
            Integer streetNumber,
            String city
    ) {
    }

    interface PersonExtractor {
        @UserMessage("Extract information about a person from {{it}}")
        Person extractPersonFrom(String text);
    }

    public static void main(String[] args) {
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .build();

        PersonExtractor personExtractor = AiServices.create(PersonExtractor.class, model);

        String text = """
                In 1968, amidst the fading echoes of Independence Day,
                a child named John arrived under the calm evening sky.
                This newborn, bearing the surname Doe, marked the start of a new journey.
                He was welcomed into the world at 345 Whispering Pines Avenue
                a quaint street nestled in the heart of Springfield
                an abode that echoed with the gentle hum of suburban dreams and aspirations.
                """;

        Person person = personExtractor.extractPersonFrom(text);

        System.out.println(person);
    }
}
