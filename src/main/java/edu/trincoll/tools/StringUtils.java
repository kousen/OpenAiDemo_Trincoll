package edu.trincoll.tools;

import dev.langchain4j.agent.tool.Tool;

public class StringUtils {

    @Tool("Count the number of times a letter appears in a string")
    public long howManyTimesALetterAppearsInAString(String s, char c) {
        return s.chars()
                .filter(ch -> ch == c)
                .count();
    }
}
