package edu.trincoll.blackforestlabs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BFLImageGenerationServiceTest {
    @Test
    void testImageGeneration() throws Exception {
        var service = new BFLImageGenerationService();

        // Create the request object
        ImageRequest imageRequest = new ImageRequest(
                "a warrior cat rides a dragon into battle",
                1024,
                768,
                false,
                null,
                null
        );

        // Send the request and retrieve the request ID
        String requestId = service.requestImageGeneration(imageRequest);
        assertNotNull(requestId, "The request ID should not be null");

        // Poll for the result and get the image URL
        String resultSample = service.pollForResult(requestId);
        assertNotNull(resultSample, "The result sample should not be null");
        System.out.println("Generated image: " + resultSample);

        // Download and save the image to src/main/resources
        service.downloadAndSaveImage(resultSample);
    }
}