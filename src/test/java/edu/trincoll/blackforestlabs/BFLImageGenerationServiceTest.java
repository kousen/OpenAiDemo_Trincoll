package edu.trincoll.blackforestlabs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static edu.trincoll.blackforestlabs.BFLRecords.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "BFL_API_KEY", matches = ".*")
class BFLImageGenerationServiceTest {
    @Test
    void testImageGeneration() throws Exception {
        var service = new BFLImageGenerationService();

        // Create the request object
        ImageRequest imageRequest = new ImageRequest("a warrior cat rides a dragon into battle");

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