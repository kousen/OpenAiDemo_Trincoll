package edu.trincoll.audio;

import com.assemblyai.api.RealtimeTranscriber;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

// Copied from the AssemblyAI Java SDK documentation
// https://www.assemblyai.com/docs/getting-started/transcribe-streaming-audio-from-a-microphone/java
public class AssemblyAIDemo {
    public static void main(String... args) throws IOException {
        Thread thread = new Thread(() -> {
            try {
                RealtimeTranscriber realtimeTranscriber = RealtimeTranscriber.builder()
                        .apiKey(System.getenv("ASSEMBLYAI_API_KEY"))
                        .sampleRate(16_000)
                        .onSessionBegins(sessionBegins -> System.out.println(
                                "Session opened with ID: " + sessionBegins.getSessionId()))
                        .onPartialTranscript(transcript -> {
                            if (!transcript.getText().isEmpty())
                                System.out.println("Partial: " + transcript.getText());
                        })
                        .onFinalTranscript(transcript -> System.out.println("Final: " + transcript.getText()))
                        .onError(err -> System.out.println("Error: " + err.getMessage()))
                        .build();

                System.out.println("Connecting to real-time transcript service");
                realtimeTranscriber.connect();

                System.out.println("Starting recording");
                AudioFormat format = new AudioFormat(16_000, 16, 1, true, false);
                // `line` is your microphone
                TargetDataLine line = AudioSystem.getTargetDataLine(format);
                line.open(format);
                byte[] data = new byte[line.getBufferSize()];
                line.start();
                while (!Thread.interrupted()) {
                    // Read the next chunk of data from the TargetDataLine.
                    line.read(data, 0, data.length);
                    realtimeTranscriber.sendAudio(data);
                }

                System.out.println("Stopping recording");
                line.close();

                System.out.println("Closing real-time transcript connection");
                realtimeTranscriber.close();
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        System.out.println("Press ENTER key to stop...");
        //noinspection ResultOfMethodCallIgnored
        System.in.read();
        thread.interrupt();
        System.exit(0);
    }
}
