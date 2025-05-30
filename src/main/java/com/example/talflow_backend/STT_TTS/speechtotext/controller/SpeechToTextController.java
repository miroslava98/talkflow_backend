package com.example.talflow_backend.STT_TTS.speechtotext.controller;


import com.example.talflow_backend.STT_TTS.speechtotext.dto.SpeechRecognitionResponse;
import com.example.talflow_backend.STT_TTS.speechtotext.service.SpeechToTextService;
import com.example.talflow_backend.ai.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@RestController
@RequestMapping("/api/speech")
public class SpeechToTextController {

    private final SpeechToTextService speechToTextService;
    private final Path uploadLocation;
    private final OllamaService ollamaService;

    @Autowired
    public SpeechToTextController(SpeechToTextService speechToTextService, OllamaService ollamaService) {
        this.speechToTextService = speechToTextService;
        this.uploadLocation = Paths.get("uploads").toAbsolutePath().normalize();
        this.ollamaService = ollamaService;

        try {
            Files.createDirectories(uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @PostMapping("/transcribe")
    public ResponseEntity<SpeechRecognitionResponse> transcribeAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("language") Optional<String> language,
            @RequestParam(value = "userId", required = false) Optional<String> userId,
            @RequestParam("scene") Optional<String> scene) {
        try {
            // Validación básica
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new SpeechRecognitionResponse(null, "ERROR", "File is empty"));
            }
            // Guardar el archivo temporalmente
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path destination = this.uploadLocation.resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Procesar el archivo
            SpeechRecognitionResponse response = speechToTextService.transcribeAudio(destination.toString(), language);



            // Eliminar el archivo temporal
            Files.deleteIfExists(destination);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    new SpeechRecognitionResponse(
                            null,
                            "ERROR",
                            "Failed to process file: " + e.getMessage()
                    )
            );
        }
    }

    @GetMapping("/test")
    public ResponseEntity<SpeechRecognitionResponse> testTranscription() {
        Optional<String> language = Optional.of("en-EN");
        SpeechRecognitionResponse response = speechToTextService.transcribeAudio("harvard.wav", language);
        return ResponseEntity.ok(response);
    }
}