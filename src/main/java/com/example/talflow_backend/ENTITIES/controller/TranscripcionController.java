package com.example.talflow_backend.ENTITIES.controller;

import com.example.talflow_backend.ENTITIES.entity.Transcripcion;
import com.example.talflow_backend.ENTITIES.entity.User;
import com.example.talflow_backend.ENTITIES.service.TranscripcionService;
import com.example.talflow_backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transcriptions")
public class TranscripcionController {

    private final TranscripcionService transcripcionService;
    private final UserRepository userRepository;

    public TranscripcionController(TranscripcionService transcripcionService, UserRepository userRepository) {
        this.transcripcionService = transcripcionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> saveTranscripcion(@RequestBody TranscripcionDto dto) {
        // Busca el usuario por email (o por id, según como tengas)
        User user = userRepository.findByEmail(dto.getUserEmail());

        if (user == null) {
            return ResponseEntity.badRequest().body("No hay usuario con ese correo");
        }

        Transcripcion transcripcion = new Transcripcion();
        transcripcion.setIdioma(dto.getIdioma());
        transcripcion.setTextoUser(dto.getTextoUser());
        transcripcion.setTextoChat(dto.getTextoChat());
        transcripcion.setUser(user);

        transcripcionService.saveTranscripcion(transcripcion);

        return ResponseEntity.ok().build();
    }


    @GetMapping("/listTranscriptions")
    public ResponseEntity<?> getTranscripciones(@RequestParam String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            return ResponseEntity.badRequest().body("No hay usuario con ese correo");
        }
        List<Transcripcion> transcripciones = transcripcionService.getTranscripcionesByUserEmail(userEmail);
        return ResponseEntity.ok(transcripciones);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TranscripcionDto {
        private String idioma;
        private String textoUser;
        private String textoChat;
        private String userEmail;  // para buscar al usuario

        // getters y setters
    }

}
