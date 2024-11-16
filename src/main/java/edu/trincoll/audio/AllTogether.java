package edu.trincoll.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static edu.trincoll.audio.LibreTranslateApp.TranslateRequest;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class AllTogether {
    private static final String RECORDING_FILE = "recorded_audio.wav";
    private final Logger logger = LoggerFactory.getLogger(AllTogether.class);

    private final AudioRecorder recorder = new AudioRecorder();
    private final AssemblyAITranscribeApp assemblyAITranscribe = new AssemblyAITranscribeApp();
    private final LibreTranslateApp libreTranslate = new LibreTranslateApp();
    private final ElevenLabsApp elevenLabs = new ElevenLabsApp();

    public void run(List<String> languages) throws IOException {
        recordAudio();
        String transcribedText = transcribeAudio();
        if (transcribedText.isBlank()) {
            logger.error("No text transcribed. Exiting...");
            return;
        }
        translateAndGenerateSpeech(languages, transcribedText);
        logger.info("All done");
    }

    private void recordAudio() throws IOException {
        recorder.startRecording();
        try (var input = System.in) {
            logger.info("Recording started. Press Enter to stop...");
            input.read();
        }
        recorder.stopRecording();
        logger.info("Recording stopped.");
    }

    private String transcribeAudio() {
        try {
            return assemblyAITranscribe.transcribe(
                    new File(RECORDING_FILE)).orElseThrow();
        } catch (IOException e) {
            logger.error("Error during transcription", e);
            throw new RuntimeException(e);
        }
    }

    private void translateAndGenerateSpeech(List<String> languages, String transcribedText) {
        languages.forEach(language -> {
            String translatedText = libreTranslate.translate(
                    new TranslateRequest("en", language, transcribedText));
            String fileName = "translated_audio_" + language;
            elevenLabs.generateSpeech(translatedText, fileName);
        });
    }

    public static void main(String[] args) throws IOException {
        new AllTogether().run(List.of("es", "fr", "de", "hi", "zh"));
    }
}
