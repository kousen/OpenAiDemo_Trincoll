package edu.trincoll.ollama;

import dev.langchain4j.agent.tool.Tool;

public class StringUtils {

    @Tool("Determine how many times a character appears in a string")
    public int countChar(String str, char c) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

}
