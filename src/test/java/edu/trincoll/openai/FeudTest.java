package edu.trincoll.openai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*")
@Execution(ExecutionMode.CONCURRENT)
public class FeudTest {

    public static final String WIKIPEDIA_FEUD_ARTICLE =
            "https://en.wikipedia.org/wiki/Drake%E2%80%93Kendrick_Lamar_feud";

    private final ChatLanguageModel chatModel = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName(OpenAiChatModelName.GPT_4_O_MINI)
            .build();

    private final List<String> prompts = List.of(
            "Who started the beef about between Drake and Kendrick Lamar?",
            "How did it escalate in 2024?",
            "Who won?",
            """
                    What are the chances Kendrick will perform
                    "Not Like Us" during the 2025 Super Bowl
                    halftime show?
                    """
    );

    @Test
    void feud_without_prompt_stuffing() {
        interface Assistant {
            String answer(String prompt);
        }

        // Create the assistant with memory
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        prompts.forEach(prompt -> {
            System.out.println("## " + prompt);
            String response = assistant.answer(prompt);
            System.out.println(response + "\n");
        });
    }

    @Test
    void prompt_stuffing_url_document_loader() {
        interface Assistant {
            String answer(@UserMessage String prompt);
        }

        Document feudDoc = UrlDocumentLoader.load(WIKIPEDIA_FEUD_ARTICLE, new TextDocumentParser());

        // GPT-4o has a 128KB limit in the context window
        if (feudDoc.text().length() > 128 * 1024) {
            System.out.println("Document too large: " + feudDoc.text().length());
            return;
        }

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        prompts.forEach(prompt -> {
            System.out.println("## " + prompt);
            String response = assistant.answer(prompt);
            System.out.println(response + "\n");
        });
    }

    @Test
    void prompt_stuffing_jsoup() throws Exception {
        // Assistant with system message
        interface Assistant {
            @SystemMessage("Given {{information}}, answer the question")
            String answer(@UserMessage String prompt, @V("information") String information);
        }

        String wikiText = Jsoup.connect(WIKIPEDIA_FEUD_ARTICLE).get().text();
        System.out.println("Length of wikipedia article " + wikiText.length());

        // This should fit in the 128KB limit
        if (wikiText.length() > 128 * 1024) {
            System.out.println("Document too large: " + wikiText.length());
            return;
        }

        // Create the assistant with memory
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        prompts.forEach(prompt -> {
            System.out.println("## " + prompt);
            String response = assistant.answer(prompt, wikiText);
            System.out.println(response + "\n");
        });
    }

    @Test
    void easy_rag() {
        interface Assistant {
            String answer(String prompt);
        }

        // Set up and load the embedding store
        Document document = UrlDocumentLoader.load(
                WIKIPEDIA_FEUD_ARTICLE, new TextDocumentParser());
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(document, embeddingStore);

        // Create the assistant with memory and content retriever
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .build();

        // Ask the assistant questions
        prompts.forEach(prompt -> {
            System.out.println("## " + prompt);
            String response = assistant.answer(prompt);
            System.out.println(response + "\n");
        });
    }

}
