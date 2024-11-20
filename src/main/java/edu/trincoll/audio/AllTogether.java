package edu.trincoll.audio;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static edu.trincoll.audio.LibreTranslateApp.TranslateRequest;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class AllTogether {
    private static final String RECORDING_FILE = "recorded_audio.wav";

    private final AudioRecorder recorder = new AudioRecorder();
    private final AssemblyAITranscribeApp assemblyAITranscribe = new AssemblyAITranscribeApp();
    private final LibreTranslateApp libreTranslate = new LibreTranslateApp();
    private final ElevenLabsApp elevenLabs = new ElevenLabsApp();

    public void run(List<String> languages) throws IOException {
        recordAudio();
        String transcribedText = transcribeAudio();
        if (transcribedText.isBlank()) {
            System.out.println("No text transcribed. Exiting...");
            return;
        }
        translateAndGenerateSpeech(languages, transcribedText);
        System.out.println("All done");
    }

    private void recordAudio() throws IOException {
        recorder.startRecording();
        try (var input = System.in) {
            System.out.println("Recording started. Press Enter to stop...");
            input.read();
        }
        recorder.stopRecording();
        System.out.println("Recording stopped.");
    }

    private String transcribeAudio() {
        try {
            return assemblyAITranscribe.transcribe(
                    new File(RECORDING_FILE)).orElseThrow();
        } catch (IOException e) {
            System.out.println("Error during transcription");
            throw new RuntimeException(e);
        }
    }

    private void translateAndGenerateSpeech(List<String> languages, String transcribedText) {
        // Step 1: Perform translations in parallel
        Map<String, String> translations;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var start = System.currentTimeMillis();

            // First, create all futures without joining them
            Map<String, CompletableFuture<String>> translationFutures = languages.stream()
                    .collect(Collectors.toMap(
                            language -> language,
                            language -> CompletableFuture.supplyAsync(
                                    () -> libreTranslate.translate(
                                            new TranslateRequest("en", language, transcribedText)),
                                    executor)
                    ));

            // Then wait for all futures to complete
            translations = translationFutures.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().join()
                    ));

            var end = System.currentTimeMillis();
            System.out.println("All translations completed in " + (end - start) + "ms");
        } catch (Exception e) {
            System.out.println("Error during translation");
            throw new RuntimeException(e);
        }

        // Step 2: Generate speech sequentially using the completed translations
        translations.forEach((language, translatedText) -> {
            try {
                String fileName = "translated_audio_" + language;
                elevenLabs.generateSpeech(translatedText, fileName);
                System.out.println("Generated speech for language: " + language);
            } catch (Exception e) {
                System.out.println("Error generating speech for language: " + language);
            }
        });
    }

    public static void main(String[] args) throws IOException {
        new AllTogether().run(List.of("de", "el", "ga", "hi", "pl", "zh"));
    }
}
