package com.example.talflow_backend.speechtotext.service;

import com.example.talflow_backend.entity.Transcripcion;
import com.example.talflow_backend.entity.User;
import com.example.talflow_backend.repository.TranscripcionRepository;
import com.example.talflow_backend.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class SpeechToTextService {


    private final AzureSpeechConfig speechConfig;

    @Autowired
    private UserRepository repoUser;

    @Autowired
    private TranscripcionRepository repoTranscription;

    @Autowired
    public SpeechToTextService(AzureSpeechConfig speechConfig) {
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
            if (!audioFilePath.toLowerCase().endsWith(".wav")) {
                return new SpeechRecognitionResponse(null, "ERROR",
                        "Only WAV files are supported");
            }

            SpeechConfig config = speechConfig.speechConfig();
            if (language.isPresent()) {
                config.setSpeechRecognitionLanguage(language.get());
            }
            // Configuración adicional para mejores resultados
            config.setProperty(PropertyId.SpeechServiceConnection_InitialSilenceTimeoutMs, "10000");
            config.setProperty(PropertyId.SpeechServiceConnection_EndSilenceTimeoutMs, "3000");

            List<String> transcribedParts = new ArrayList<>();
            Semaphore recognitionEnd = new Semaphore(0);
            try (AudioConfig audioConfig = AudioConfig.fromWavFileInput(audioFilePath);
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

    public void saveTranscriptionHistory(String userId, String language, String recognizedText) {

        try {
            Long userID = Long.parseLong(userId);
            Optional<User> optionalUsuario = repoUser.findById(userID);
            if (optionalUsuario.isPresent()) {
                User usuario = optionalUsuario.get();

                Transcripcion t = new Transcripcion();
                t.setUser(usuario);
                t.setIdioma(language);
                t.setTexto(recognizedText);
                t.setFecha(LocalDateTime.now());

                repoTranscription.save(t);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid user ID: " + userId);
        }
    }
}
