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
        var imageRequest = new ImageRequest("""
                        The old god Cthulhu
                        having a beer with the boys
                        while watching football on TV
                        """);

        // Send the request and retrieve the request ID
        String requestId = service.requestImageGeneration(imageRequest);
        assertNotNull(requestId, "The request ID should not be null");

        // Poll for the result and get the image URL
        String resultSample = service.pollForResult(requestId);
        assertNotNull(resultSample, "The result sample should not be null");
        System.out.println(resultSample);
    }

    @Test
    void testImageGenerationWithUpSampling() throws Exception {
        var service = new BFLImageGenerationService();
        var imageRequest = new ImageRequest("""
                        a stochastic parrot
                        """,
                1024, 768,
                true,
                null,
                6);

        // Send the request and retrieve the request ID
        String requestId = service.requestImageGeneration(imageRequest);
        assertNotNull(requestId, "The request ID should not be null");

        // Poll for the result and get the image URL
        String resultSample = service.pollForResult(requestId);
        assertNotNull(resultSample, "The result sample should not be null");
        System.out.println(resultSample);
    }
}