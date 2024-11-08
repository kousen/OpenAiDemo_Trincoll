package edu.trincoll.openai;

import edu.trincoll.recraft.RecraftImageGenerationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static edu.trincoll.recraft.RecraftRecords.ImageRequest;
import static edu.trincoll.recraft.RecraftRecords.ImagesResponse;

@EnabledIfEnvironmentVariable(named = "RECRAFT_API_KEY", matches = ".*")
public class RecraftLC4jTest {
    private final RecraftImageGenerationService service = new RecraftImageGenerationService();

    @Test
    void testImageGeneration() throws Exception {
        ImagesResponse imagesResponse = service.requestImageGeneration(
                new ImageRequest(
                        """
                                Capybara witches dance around a
                                campfire in the forest at night.
                                """,
                        1,
                        "realistic_image",
                        "url",
                        "1024x1024"));
        ImagesResponse.Image imageResponse = imagesResponse.data()[0];
        System.out.println(service.saveImage(imageResponse.url()));
    }

}
