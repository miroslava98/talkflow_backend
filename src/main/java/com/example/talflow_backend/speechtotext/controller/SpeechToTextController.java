package com.example.talflow_backend.speechtotext.controller;


import com.example.talflow_backend.speechtotext.dto.SpeechRecognitionResponse;
import com.example.talflow_backend.speechtotext.service.SpeechToTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/speech")
public class SpeechToTextController {

    private final SpeechToTextService speechToTextService;
    private final Path uploadLocation;

    @Autowired
    public SpeechToTextController(SpeechToTextService speechToTextService) {
        this.speechToTextService = speechToTextService;
        this.uploadLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @PostMapping("/transcribe")
    public ResponseEntity<SpeechRecognitionResponse> transcribeAudio(
            @RequestParam("file") MultipartFile file) {
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
            SpeechRecognitionResponse response = speechToTextService.transcribeAudio(destination.toString());

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
        SpeechRecognitionResponse response = speechToTextService.transcribeAudio("harvard.wav");
        return ResponseEntity.ok(response);
    }
}