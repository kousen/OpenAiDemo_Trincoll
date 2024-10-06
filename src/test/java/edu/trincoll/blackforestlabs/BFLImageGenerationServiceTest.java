package edu.trincoll.blackforestlabs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "BFL_API_KEY", matches = ".*")
class BFLImageGenerationServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(BFLImageGenerationServiceTest.class);

    @Test
    void testImageGeneration() throws Exception {
        var service = new BFLImageGenerationService();

        // Create the request object
        ImageRequest imageRequest = new ImageRequest("a warrior cat rides a dragon into battle");

        // Send the request and retrieve the request ID
        String requestId = service.requestImageGeneration(imageRequest);
        assertNotNull(requestId, "The request ID should not be null");
        logger.info("Request ID: {}", requestId);

        // Download the image asynchronously and wait for the result
        CompletableFuture<Path> future = service.downloadImageAsync(requestId);

        // Log before waiting for the result
        logger.info("Waiting for the image to be ready...");
        Path imagePath = future.get(1, TimeUnit.MINUTES); // Wait for up to 1 minute for the image to be ready

        assertNotNull(imagePath, "The image path should not be null");
        assertTrue(imagePath.toFile().exists(), "The image file should exist");
        logger.info("Generated image saved to: {}", imagePath.toAbsolutePath());
    }
}