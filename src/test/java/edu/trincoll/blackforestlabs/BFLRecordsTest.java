package edu.trincoll.blackforestlabs;

import org.junit.jupiter.api.Test;

import static edu.trincoll.blackforestlabs.BFLRecords.*;
import static org.junit.jupiter.api.Assertions.*;

class BFLRecordsTest {

    // Valid requests
    @Test
    void testImageRequestValidation() {
        assertAll(
                () -> assertDoesNotThrow(() -> new ImageRequest("prompt")),
                () -> assertDoesNotThrow(() -> new ImageRequest("prompt", 256, 256)),
                () -> assertDoesNotThrow(() -> new ImageRequest("prompt", 1440, 1440)),
                () -> assertDoesNotThrow(() -> new ImageRequest("prompt", 1440, 1440, false, null, 6))
        );
    }

    // Invalid requests
    @Test
    void testImageRequestValidationExceptions() {
        assertAll(
                // Width below minimum
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ImageRequest("prompt", 255, 256)),
                // Height below minimum
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ImageRequest("prompt", 256, 255)),
                // Width above maximum
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ImageRequest("prompt", 1441, 1440)),
                // Height above maximum
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ImageRequest("prompt", 1440, 1441)),
                // Width not a multiple of 32
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ImageRequest("prompt", 257, 256)),
                // Height not a multiple of 32
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ImageRequest("prompt", 256, 257)),
                // Safety tolerance below minimum
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ImageRequest("prompt", 1440, 1440, false, null, -1)),
                // Safety tolerance above maximum
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ImageRequest("prompt", 1440, 1440, false, null, 7))
        );
    }
}