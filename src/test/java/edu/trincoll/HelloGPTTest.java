package edu.trincoll;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelloGPTTest {
    private final HelloGPT helloGPT = new HelloGPT();

    @Test
    void testGetModels() {
        var models = helloGPT.getModels();
        assertNotNull(models);
        assertFalse(models.data().isEmpty());
        models.data().forEach(System.out::println);
    }

}