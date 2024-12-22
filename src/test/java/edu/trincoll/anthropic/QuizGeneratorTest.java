package edu.trincoll.anthropic;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class QuizGeneratorTest {
    private final QuizGenerator quizGenerator = new QuizGenerator();

    @Execution(ExecutionMode.SAME_THREAD)
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"JUnit testing", "Architecture", "REST web services"})
    void generateQuiz(String topic) {
        System.out.println("Generating quiz for: " + topic);
        String quiz = quizGenerator.generateQuiz("Software Design using Kotlin");
        String name = topic.toLowerCase().replaceAll("\\s+", "_");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        quizGenerator.writeToXmlFile(quiz, "%s_%s.xml".formatted(name, timestamp));
    }

}