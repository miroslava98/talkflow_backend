package com.example.talflow_backend.speechtotext.service;

import com.example.talflow_backend.speechtotext.config.AzureSpeechConfig;
import com.example.talflow_backend.speechtotext.dto.SpeechRecognitionResponse;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class SpeechToTextService {


    private final AzureSpeechConfig speechConfig;

    @Autowired
    public SpeechToTextService(AzureSpeechConfig speechConfig) {
        this.speechConfig = speechConfig;
    }

    public SpeechRecognitionResponse transcribeAudio(String audioFilePath) {
        try {
            File audioFile = new File(audioFilePath);
            // Validaciones adicionales
            if (!audioFile.exists()) {
                return new SpeechRecognitionResponse(null, "ERROR", "Audio file not found at: " + audioFilePath);
            }
            if (audioFile.length() == 0) {
                return new SpeechRecognitionResponse(null, "ERROR",
                        "Audio file is empty");
            }
            if (!audioFilePath.toLowerCase().endsWith(".wav")) {
                return new SpeechRecognitionResponse(null, "ERROR",
                        "Only WAV files are supported");
            }

            try (AudioConfig audioConfig = AudioConfig.fromWavFileInput(audioFilePath); SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig.speechConfig(), audioConfig)) {
                Future<SpeechRecognitionResult> task = recognizer.recognizeOnceAsync();
                SpeechRecognitionResult result = task.get();

                if (result.getReason() == ResultReason.RecognizedSpeech) {
                    return new SpeechRecognitionResponse(
                            result.getText(),
                            "SUCCESS",
                            null
                    );
                } else {
                    return handleErrorResult(result);
                }

            }

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return new SpeechRecognitionResponse(
                    null,
                    "ERROR",
                    "Error processing audio: " + e.getMessage()
            );
        }
    }

    private SpeechRecognitionResponse handleErrorResult(SpeechRecognitionResult result) {
        if (result.getReason() == ResultReason.NoMatch) {
            return new SpeechRecognitionResponse(
                    null,
                    "NO_MATCH",
                    "Speech could not be recognized"
            );
        } else {
            CancellationDetails cancellation = CancellationDetails.fromResult(result);
            return new SpeechRecognitionResponse(
                    null,
                    "CANCELLED",
                    "Error: " + cancellation.getErrorDetails()
            );
        }
    }
}
