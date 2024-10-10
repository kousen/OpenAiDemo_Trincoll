package edu.trincoll;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.output.Response;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.List;

public class OpenAiLangChain4jTest {
    private final String apiKey = System.getenv("OPENAI_API_KEY");

    private final ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                //.modelName("o1-preview")
                .build();

    @Test
    void o1mini() {
        ChatLanguageModel o1mini = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("o1-mini")
                .build();
        System.out.println(
                o1mini.generate("What is the meaning of life?"));
    }

    @Test
    void chatWithMessages() {
        ChatResponse response = chatModel.chat(ChatRequest.builder()
                .messages(List.of(new UserMessage("""
                    What is the Ultimate Answer to
                    the Ultimate Question of
                    Life, the Universe, and Everything?""")))
                .build());
        System.out.println(response.aiMessage().text());
        System.out.println(response.tokenUsage());
    }

    @Test
    void chatWithString() {
        String answer = chatModel.generate("""
                Who is the best musician of the
                2010s?
                """);
        System.out.println(answer);
    }

    @Test
    void visionChat() {
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg";
        Response<AiMessage> response = chatModel.generate(
                UserMessage.from(
                        ImageContent.from(imageUrl),
                        TextContent.from("What do you see?")
                )
        );
        System.out.println(response.content().text());
    }

    @Test
    void promptStuffing() throws Exception {
        String wikiText = Jsoup.connect(
                "https://en.wikipedia.org/wiki/Drake%E2%80%93Kendrick_Lamar_feud")
                .get()
                .text();
        System.out.println("Length of wikipedia article " + wikiText.length());

//        Document feudDoc = UrlDocumentLoader.load(
//                "https://en.wikipedia.org/wiki/Drake%E2%80%93Kendrick_Lamar_feud",
//                new TextDocumentParser());
//        if (feudDoc.text().length() > 128 * 1024) {
//            System.out.println("Document too large: " + feudDoc.text().length());
//            System.out.println(feudDoc.text().substring(0, 1000));
//            return;
//        }

        String response = chatModel.generate(
                UserMessage.from(
                        TextContent.from("""
                            Given the information in %s,
                            What was the beef about between
                            Drake and Kendrick Lamar?
                            """.formatted(wikiText))))
                .content().text();
        System.out.println(response);
    }

}
