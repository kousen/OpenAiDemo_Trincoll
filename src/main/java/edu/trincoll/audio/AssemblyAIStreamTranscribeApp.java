package edu.trincoll.audio;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class AssemblyAIStreamTranscribeApp {
    private final AssemblyAI client;

    public AssemblyAIStreamTranscribeApp() {
        client = AssemblyAI.builder()
                .apiKey(System.getenv("ASSEMBLYAI_API_KEY"))
                .build();
    }

    public Optional<String> transcribe(InputStream audioStream) throws IOException {
        Transcript transcript = client.transcripts().transcribe(audioStream);

        if (transcript.getStatus().equals(TranscriptStatus.ERROR)) {
            System.err.println(transcript.getError());
            System.exit(1);
        }

        return transcript.getText();
    }
}