package com.example.talflow_backend.STT_TTS.speechtotext.service;

import com.example.talflow_backend.repository.TranscripcionRepository;
import com.example.talflow_backend.repository.UserRepository;
import com.example.talflow_backend.STT_TTS.speechtotext.dto.SpeechRecognitionResponse;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class SpeechToTextService {


    private final SpeechConfig speechConfig;

    @Autowired
    private UserRepository repoUser;

    @Autowired
    private TranscripcionRepository repoTranscription;

    @Autowired
    public SpeechToTextService(SpeechConfig speechConfig) {
        this.speechConfig = speechConfig;
    }

    public SpeechRecognitionResponse transcribeAudio(String audioFilePath, Optional<String> language) {
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
            if (!audioFilePath.toLowerCase().endsWith(".mp4")) {
                return new SpeechRecognitionResponse(null, "ERROR",
                        "Only MP4 files are supported");
            }

            SpeechConfig config = speechConfig;
            if (language.isPresent()) {
                config.setSpeechRecognitionLanguage(language.get());
            }
            // Configuración adicional para mejores resultados
            config.setProperty(PropertyId.SpeechServiceConnection_InitialSilenceTimeoutMs, "10000");
            config.setProperty(PropertyId.SpeechServiceConnection_EndSilenceTimeoutMs, "3000");

            List<String> transcribedParts = new ArrayList<>();
            Semaphore recognitionEnd = new Semaphore(0);

            String wavPath = audioFilePath.replace(".mp4", ".wav");
            Process process = null;
            try {
                process = new ProcessBuilder(
                        "ffmpeg",
                        "-y",
                        "-i", audioFilePath,
                        "-ac", "1",
                        "-ar", "16000",
                        "-af", "afftdn",
                        wavPath
                ).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            process.waitFor();

            try (AudioConfig audioConfig = AudioConfig.fromWavFileInput(wavPath);
                 SpeechRecognizer recognizer = new SpeechRecognizer(config, audioConfig)) {

                recognizer.recognized.addEventListener((s, e) -> {
                    if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                        transcribedParts.add(e.getResult().getText());
                    }
                });

                recognizer.canceled.addEventListener((s, e) -> {
                    if (e.getReason() == CancellationReason.Error) {
                        System.err.println("Error: " + e.getErrorDetails());
                    }
                    recognitionEnd.release();
                });


                recognizer.sessionStopped.addEventListener((s, e) -> {
                    recognitionEnd.release();
                });

                // Iniciar reconocimiento continuo
                recognizer.startContinuousRecognitionAsync().get();

                // Esperar hasta que termine (máximo 10 minutos)
                recognitionEnd.tryAcquire(10, TimeUnit.MINUTES);

                // Detener el reconocimiento
                recognizer.stopContinuousRecognitionAsync().get();

                // Unir todas las partes reconocidas
                String fullText = String.join(" ", transcribedParts);
                return new SpeechRecognitionResponse(fullText, "SUCCESS", null);

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


