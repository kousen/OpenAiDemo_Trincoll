package edu.trincoll;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static edu.trincoll.HelloGPT.*;
import static org.junit.jupiter.api.Assertions.*;

class HelloGPTTest {
    private final HelloGPT helloGPT = new HelloGPT();

    @Test
    void testGetModels() {
        var models = helloGPT.getModels();
        Assertions.assertNotNull(models);
        assertFalse(models.data().isEmpty());
        models.data().stream()
                .sorted(Comparator.comparing(Model::id))
                .forEach(System.out::println);
    }

}