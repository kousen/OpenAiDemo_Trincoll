package edu.trincoll.audio;

import javax.sound.sampled.*;
import java.io.File;

@SuppressWarnings("CallToPrintStackTrace")
public class AudioRecorder {

    private TargetDataLine targetLine;
    private Thread recordingThread;
    private boolean isRecording = false;

    public void startRecording() {
        if (isRecording) {
            System.out.println("Recording is already in progress!");
            return;
        }

        try {
            AudioFormat format = new AudioFormat(16000, 8, 2, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                return;
            }

            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open();
            targetLine.start();

            isRecording = true;
            System.out.println("Recording started...");

            recordingThread = new Thread(() -> {
                AudioInputStream audioStream = new AudioInputStream(targetLine);
                File audioFile = new File("recorded_audio.wav");

                try {
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
                    System.out.println("Recording saved to " + audioFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            recordingThread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stopRecording() {
        if (!isRecording) {
            System.out.println("No recording is currently in progress!");
            return;
        }

        isRecording = false;
        targetLine.stop();
        targetLine.close();
        System.out.println("Recording stopped.");

        try {
            recordingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Convenience method to check if the recording is currently active
    public boolean isRecording() {
        return isRecording;
    }
}