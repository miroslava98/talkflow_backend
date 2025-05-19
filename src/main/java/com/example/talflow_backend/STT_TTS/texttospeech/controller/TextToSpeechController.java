package com.example.talflow_backend.STT_TTS.texttospeech.controller;


import com.example.talflow_backend.STT_TTS.texttospeech.dto.TextToSpeechRequest;
import com.example.talflow_backend.STT_TTS.texttospeech.dto.TextToSpeechResponse;
import com.example.talflow_backend.STT_TTS.texttospeech.service.TextToSpeechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tts")
public class TextToSpeechController {

    private final TextToSpeechService ttsService;

    @Autowired
    public TextToSpeechController(TextToSpeechService ttsService) {
        this.ttsService = ttsService;
    }

    @PostMapping("/generate")
    public ResponseEntity<TextToSpeechResponse> generateSpeech(@RequestBody TextToSpeechRequest request) {
        if (request.getText() == null || request.getText().isBlank()) {
            return ResponseEntity.badRequest().body(
                    new TextToSpeechResponse(null, null, "El texto es obligatorio")
            );
        }

        TextToSpeechResponse response = ttsService.textToAudio(
                request.getText(),
                Optional.ofNullable(request.getLanguage())
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/ping")
    public String ping() {
        System.out.println("Ping recibido");
        return "pong";
    }
}