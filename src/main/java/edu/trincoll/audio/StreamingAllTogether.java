package edu.trincoll.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static edu.trincoll.audio.LibreTranslateApp.TranslateRequest;

public class StreamingAllTogether {
    private static final Logger logger = LoggerFactory.getLogger(StreamingAllTogether.class);

    private final AudioStreamRecorder recorder = new AudioStreamRecorder();
    private final AssemblyAIStreamTranscribeApp assemblyAITranscribe = new AssemblyAIStreamTranscribeApp();
    private final LibreTranslateApp libreTranslate = new LibreTranslateApp();
    private final ElevenLabsApp elevenLabs = new ElevenLabsApp();

    public void run(List<String> languages) throws IOException {
        logger.debug("Starting application with languages: {}", languages);

        logger.info("Initializing audio recording...");
        CompletableFuture<InputStream> recordingFuture = recordAudio();

        logger.info("Starting transcription...");
        String transcribedText = transcribeAudio(recordingFuture);

        if (transcribedText.isBlank()) {
            logger.error("No text transcribed. Exiting...");
            return;
        }

        logger.info("Transcription successful. Text: {}", transcribedText);
        translateAndGenerateSpeech(languages, transcribedText);
        logger.info("All processing completed successfully");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private CompletableFuture<InputStream> recordAudio() throws IOException {
        CompletableFuture<InputStream> recordingFuture = recorder.startRecording();
        try (var input = System.in) {
            logger.info("Recording started. Press Enter to stop...");
            input.read();
        }
        recorder.stopRecording();
        logger.info("Recording stopped successfully");
        return recordingFuture;
    }

    private String transcribeAudio(CompletableFuture<InputStream> recordingFuture) {
        try {
            logger.debug("Waiting for recording future to complete...");
            InputStream audioStream = recordingFuture.join();
            logger.debug("Recording future completed, starting transcription...");
            String result = assemblyAITranscribe.transcribe(audioStream).orElseThrow();
            logger.info("Transcription completed successfully");
            System.out.println("Transcription: " + result);
            return result;
        } catch (IOException e) {
            logger.error("Error during transcription: {}", e.getMessage(), e);
            throw new RuntimeException("Transcription failed", e);
        } catch (Exception e) {
            logger.error("Unexpected error during transcription: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during transcription", e);
        }
    }

    private void translateAndGenerateSpeech(List<String> languages, String transcribedText) {
        // Step 1: Perform translations in parallel
        Map<String, String> translations;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var start = System.currentTimeMillis();
            logger.info("Starting parallel translations for {} languages", languages.size());

            Map<String, CompletableFuture<String>> translationFutures = languages.stream()
                    .collect(Collectors.toMap(
                            language -> language,
                            language -> CompletableFuture.supplyAsync(
                                    () -> {
                                        logger.debug("Starting translation for language: {}", language);
                                        return libreTranslate.translate(
                                                new TranslateRequest("en", language, transcribedText));
                                    },
                                    executor)
                    ));

            translations = translationFutures.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                try {
                                    String result = entry.getValue().join();
                                    System.out.println("Translation for " + entry.getKey() + ": " + result);
                                    return result;
                                } catch (Exception e) {
                                    logger.error("Translation failed for language {}: {}",
                                            entry.getKey(), e.getMessage());
                                    throw e;
                                }
                            }
                    ));

            var end = System.currentTimeMillis();
            logger.info("All translations completed in {} ms", end - start);
        } catch (Exception e) {
            logger.error("Error during translation phase: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        // Step 2: Generate speech sequentially using the completed translations
        translations.forEach((language, translatedText) -> {
            try {
                String fileName = "translated_audio_" + language;
                logger.info("Generating speech for language: {}", language);
                elevenLabs.generateSpeech(translatedText, fileName);
                logger.info("Successfully generated speech for language: {}", language);
            } catch (Exception e) {
                logger.error("Error generating speech for language {}: {}",
                        language, e.getMessage(), e);
            }
        });
    }

    public static void main(String[] args) throws IOException {
        logger.info("Starting StreamingAllTogether application");
        new StreamingAllTogether().run(List.of("de", "el", "en", "ga", "hi", "pl", "zh"));
    }
}