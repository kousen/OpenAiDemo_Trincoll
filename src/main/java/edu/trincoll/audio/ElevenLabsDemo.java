package edu.trincoll.audio;

import net.andrewcpu.elevenlabs.ElevenLabs;
import net.andrewcpu.elevenlabs.builders.SpeechGenerationBuilder;
import net.andrewcpu.elevenlabs.enums.ElevenLabsVoiceModel;
import net.andrewcpu.elevenlabs.enums.GeneratedAudioOutputFormat;
import net.andrewcpu.elevenlabs.enums.StreamLatencyOptimization;
import net.andrewcpu.elevenlabs.model.voice.Voice;
import net.andrewcpu.elevenlabs.model.voice.VoiceSettings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ElevenLabsDemo {
    public static void main(String[] args) {
        ElevenLabs.setApiKey(System.getenv("ELEVENLABS_API_KEY"));

        String text = """
                Here is text that I would like
                to convert to speech using the
                ElevenLabs API. This is a
                multi-line string that will be
                converted to speech.
                """;

        try (InputStream inputStream = SpeechGenerationBuilder.textToSpeech()
                .streamed() // output type of file (or use .streamed() for an InputStream)
                .setText(text)
                .setGeneratedAudioOutputFormat(GeneratedAudioOutputFormat.MP3_44100_128)
                .setVoiceId("voiceIdString")
                .setVoiceSettings(VoiceSettings.getDefaultVoiceSettings())
                .setVoice(Voice.getVoice("onwK4e9ZLuTAKqWW03F9")) // or use a voice object, which will pull settings / ID out of the Voice
                .setModel(ElevenLabsVoiceModel.ELEVEN_MULTILINGUAL_V2)
                .setLatencyOptimization(StreamLatencyOptimization.NONE)
                .build()) {

            // Save the InputStream to a file
            Path outputPath = Paths.get("src/main/resources/output.mp3");
            Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
