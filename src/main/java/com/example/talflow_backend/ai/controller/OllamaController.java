package com.example.talflow_backend.ai.controller;

import com.example.talflow_backend.STT_TTS.texttospeech.dto.TextToSpeechResponse;
import com.example.talflow_backend.STT_TTS.texttospeech.service.TextToSpeechService;
import com.example.talflow_backend.ai.dto.ChatRequest;
import com.example.talflow_backend.ai.dto.OllamaResponse;
import com.example.talflow_backend.ai.service.OllamaService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")

public class OllamaController {
    private final OllamaService ollamaService;
    private final TextToSpeechService ttsService;

    public OllamaController(OllamaService ollamaService, TextToSpeechService ttsService) {
        this.ollamaService = ollamaService;
        this.ttsService = ttsService;
    }

    @PostMapping
    public ResponseEntity<OllamaResponse> chat(@RequestBody ChatRequest request) {

        try {
            String ollamaReply = ollamaService.enviarAlModelo(request);


            //Generar audio en base64 con el texto recibido
            TextToSpeechResponse ttsResponse = ttsService.textToAudio(ollamaReply, Optional.empty());


            return ResponseEntity.ok(new OllamaResponse(ollamaReply, ttsResponse.getAudioBase64()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OllamaResponse("ERROR: " + e.getMessage(), null));
        }
    }


}
