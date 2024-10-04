package edu.trincoll.blackforestlabs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BFLImageGenerationServiceTest {
    @Test
    void testImageGeneration() throws Exception {
        var service = new BFLImageGenerationService();

        String prompt = "a warrior cat rides a dragon into battle";

        // Send the request and retrieve the request ID
        String requestId = service.requestImageGeneration(prompt, 1024, 768);
        assertNotNull(requestId, "The request ID should not be null");

        // Poll for the result and get the image URL
        String resultSample = service.pollForResult(requestId);
        assertNotNull(resultSample, "The result sample should not be null");
        System.out.println("Generated image: " + resultSample);
    }
}