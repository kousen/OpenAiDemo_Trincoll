package edu.trincoll;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "BFL_API_KEY", matches = ".+")
public class BFLApiClientTest {

    private static BFLApiClient apiClient;

    @BeforeAll
    static void setUp() {
        apiClient = new BFLApiClient();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        apiClient.close();
    }

    @Test
    @DisplayName("Test Full Workflow: Submit FluxPro Task and Retrieve Result")
    void testExecuteFluxProWorkflow() {
        BFLApiClient.FluxProInputs inputs = new BFLApiClient.FluxProInputs(
                "A warrior cat riding a dragon into battle",
                1024,
                768,
                40,      // steps
                false,  // promptUpsampling
                null,      // seed
                2.5,     // guidance
                null,       // safetyTolerance
                2.0      // interval
        );

        CompletableFuture<String> future = apiClient.execute("flux-pro", inputs);

        assertDoesNotThrow(() -> {
            String result = future.join();
            assertNotNull(result, "The generated image sample should not be null.");
            assertFalse(result.isEmpty(), "The generated image sample should not be empty.");
            assertTrue(result.startsWith("https://"), "The result should be a valid URL.");
            System.out.println("Generated Image Sample: " + result);
        });
    }

    @Test
    @DisplayName("Test Full Workflow: Submit FluxProPlus Task and Retrieve Result")
    void testExecuteFluxProPlusWorkflow() {
        BFLApiClient.FluxProPlusInputs inputs = new BFLApiClient.FluxProPlusInputs(
                "A warrior cat riding a dragon into battle",
                1024,
                768,
                false,  // promptUpsampling
                null,     // seed
                null       // safetyTolerance
        );

        CompletableFuture<String> future = apiClient.execute("flux-pro-1.1", inputs);

        assertDoesNotThrow(() -> {
            String result = future.join();
            assertNotNull(result, "The generated image sample should not be null.");
            assertFalse(result.isEmpty(), "The generated image sample should not be empty.");
            assertTrue(result.startsWith("https://"), "The result should be a valid URL.");
            System.out.println("Generated Image Sample (FluxProPlus): " + result);
        });
    }

    @Test
    @DisplayName("Test Full Workflow: Submit FluxDev Task and Retrieve Result")
    void testExecuteFluxDevWorkflow() {
        BFLApiClient.FluxDevInputs inputs = new BFLApiClient.FluxDevInputs(
                "A warrior cat riding a dragon into battle",
                1024,
                768,
                28,      // steps
                false,  // promptUpsampling
                null,      // seed
                3.0,     // guidance
                null        // safetyTolerance
        );

        CompletableFuture<String> future = apiClient.execute("flux-dev", inputs);

        assertDoesNotThrow(() -> {
            String result = future.join();
            assertNotNull(result, "The generated image sample should not be null.");
            assertFalse(result.isEmpty(), "The generated image sample should not be empty.");
            assertTrue(result.startsWith("https://"), "The result should be a valid URL.");
            System.out.println("Generated Image Sample (FluxDev): " + result);
        });
    }
}
