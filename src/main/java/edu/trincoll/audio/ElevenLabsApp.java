package edu.trincoll.audio;

import net.andrewcpu.elevenlabs.ElevenLabs;
import net.andrewcpu.elevenlabs.builders.SpeechGenerationBuilder;
import net.andrewcpu.elevenlabs.enums.ElevenLabsVoiceModel;
import net.andrewcpu.elevenlabs.enums.StreamLatencyOptimization;
import net.andrewcpu.elevenlabs.model.voice.Voice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;

public class ElevenLabsApp {
    private final Logger logger = LoggerFactory.getLogger(ElevenLabsApp.class);
    private static final String VOICE_ID = "pVnrL6sighQX7hVz89cp";

    public ElevenLabsApp() {
        String apiKey = System.getenv("ELEVENLABS_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ELEVENLABS_API_KEY environment variable not set");
        }
        ElevenLabs.setApiKey(apiKey);
    }

    public void generateSpeech(String text, String fileName) {
        logger.info("Starting speech generation for file: {}", fileName);
        Instant start = Instant.now();

        try {
            Path outputDir = Paths.get("src/main/resources/");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            Path outputPath = outputDir.resolve(fileName + ".mp3");

            // Get the input stream with streamed audio
            try (InputStream inputStream = SpeechGenerationBuilder.textToSpeech()
                    .streamed()
                    .setText(text)
                    .setVoice(Voice.getVoice(VOICE_ID))
                    .setModel(ElevenLabsVoiceModel.ELEVEN_MULTILINGUAL_V2)
                    .setLatencyOptimization(StreamLatencyOptimization.NONE)
                    .build()) {

                // Copy the stream to the file
                Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }

            Duration duration = Duration.between(start, Instant.now());
            logger.info("Successfully generated speech file: {} in {} seconds",
                    fileName, duration.toSeconds());

        } catch (IOException e) {
            logger.error("Failed to generate speech for file: {}", fileName, e);
            throw new RuntimeException("Failed to generate speech: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error generating speech for file: {}", fileName, e);
            throw new RuntimeException("Unexpected error generating speech: " + e.getMessage(), e);
        }
    }
}