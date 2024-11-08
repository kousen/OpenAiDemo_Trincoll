package edu.trincoll.ollama;

import edu.trincoll.tools.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    private final StringUtils utils = new StringUtils();

    @Test
    void howManyTimesALetterAppearsInAString() {
        assertAll(
                () -> assertEquals(1,
                        utils.howManyTimesALetterAppearsInAString("hello", 'h')),
                () -> assertEquals(1,
                        utils.howManyTimesALetterAppearsInAString("hello", 'e')),
                () -> assertEquals(2,
                        utils.howManyTimesALetterAppearsInAString("hello", 'l')),
                () -> assertEquals(1,
                        utils.howManyTimesALetterAppearsInAString("hello", 'o')),
                () -> assertEquals(0,
                        utils.howManyTimesALetterAppearsInAString("hello", 'z'))
        );
    }

}