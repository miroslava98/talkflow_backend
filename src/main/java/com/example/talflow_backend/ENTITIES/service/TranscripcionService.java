package com.example.talflow_backend.ENTITIES.service;

import com.example.talflow_backend.ENTITIES.entity.Transcripcion;
import com.example.talflow_backend.repository.TranscripcionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TranscripcionService {

    private final TranscripcionRepository transcripcionRepository;

    public TranscripcionService(TranscripcionRepository transcripcionRepository) {
        this.transcripcionRepository = transcripcionRepository;
    }

    public Transcripcion saveTranscripcion(Transcripcion transcripcion) {
        transcripcion.setFecha(LocalDateTime.now()); // asigna fecha actual
        return transcripcionRepository.save(transcripcion);
    }


    public List<Transcripcion> getTranscripcionesByUserEmail(String email) {
        return transcripcionRepository.findByUserEmailOrderByFechaDesc(email);
    }

}
