package edu.trincoll;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.openai.OpenAiImageModelName;
import dev.langchain4j.model.output.Response;

public class Dalle3Service {
    public static void main(String[] args) {
        ImageModel model = OpenAiImageModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(OpenAiImageModelName.DALL_E_3)
                .build();

        Response<Image> imageResponse = model.generate("""
            Two giraffes in a samurai sword fight
            on the Moon with the Earth in the background.
            """);
        System.out.println(imageResponse.content().revisedPrompt());
        System.out.println(imageResponse.tokenUsage());
        System.out.println(imageResponse.content().url());
    }
}