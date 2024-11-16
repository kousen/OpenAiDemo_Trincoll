package edu.trincoll.audio;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class AudioRecorderTest {
    private AudioRecorder recorder;

    @BeforeEach
    public void setUp() {
        recorder = new AudioRecorder();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void audioRecorderClient() throws IOException {
        System.out.println("Recording started. Press Enter to stop...");
        recorder.startRecording();
        assertTrue(recorder.isRecording(), "Recorder should be in a recording state");

        // Wait for the user to press Enter
        System.in.read();

        recorder.stopRecording();
        assertFalse(recorder.isRecording(), "Recorder should have stopped recording");
        System.out.println("Recording stopped.");
    }

    @AfterEach
    public void tearDown() {
        if (recorder.isRecording()) {
            recorder.stopRecording(); // Ensure recording is stopped after each test
        }
    }

    @Test
    public void testStartRecording() throws InterruptedException {
        recorder.startRecording();
        assertTrue(recorder.isRecording(), "Recorder should be in a recording state");
        Thread.sleep(2000); // Let it record for 2 seconds
        recorder.stopRecording();
        assertFalse(recorder.isRecording(), "Recorder should have stopped recording");
    }

    @Test
    public void testStopRecordingWhenNotStarted() {
        // Attempt to stop when recording is not active
        recorder.stopRecording();
        assertFalse(recorder.isRecording(), "Recorder should not be in a recording state");
    }

    @Test
    public void testStartAndStopRecordingWithSimulatedInput() {
        // Simulate user input for starting and stopping recording
        String simulatedInput = "start\nstop\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        Scanner scanner = new Scanner(System.in);
        String command;

        while (scanner.hasNextLine()) {
            command = scanner.nextLine().trim().toLowerCase();

            if ("start".equals(command)) {
                if (!recorder.isRecording()) {
                    recorder.startRecording();
                    assertTrue(recorder.isRecording(), "Recorder should be in a recording state");
                } else {
                    System.out.println("Recording is already in progress.");
                }
            } else if ("stop".equals(command)) {
                if (recorder.isRecording()) {
                    recorder.stopRecording();
                    assertFalse(recorder.isRecording(), "Recorder should have stopped recording");
                    break; // Exit after stopping the recording
                } else {
                    System.out.println("No recording in progress.");
                }
            } else {
                System.out.println("Invalid command. Use 'start' or 'stop'.");
            }
        }

        scanner.close();
    }
}