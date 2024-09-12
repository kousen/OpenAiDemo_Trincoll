package edu.trincoll;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class HelloGPTTest {
    private final HelloGPT helloGPT = new HelloGPT();

    @Test
    void testGetModels() {
        var models = helloGPT.getModels();
        assertNotNull(models);
        assertFalse(models.data().isEmpty());
        models.data().stream()
                .sorted(Comparator.comparing(HelloGPT.Model::id))
                .forEach(System.out::println);
    }

}