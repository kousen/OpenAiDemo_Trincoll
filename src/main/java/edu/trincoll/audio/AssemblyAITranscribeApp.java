package edu.trincoll.audio;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class AssemblyAITranscribeApp {
    private final AssemblyAI client;

    public AssemblyAITranscribeApp() {
        client = AssemblyAI.builder()
                .apiKey(System.getenv("ASSEMBLYAI_API_KEY"))
                .build();
    }

    public Optional<String> transcribe(File audioFile) throws IOException {
        Transcript transcript = client.transcripts().transcribe(audioFile);

        if (transcript.getStatus().equals(TranscriptStatus.ERROR)) {
            System.err.println(transcript.getError());
            System.exit(1);
        }

        return transcript.getText();
    }
}
