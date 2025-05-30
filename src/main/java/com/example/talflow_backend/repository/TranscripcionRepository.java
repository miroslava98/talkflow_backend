package com.example.talflow_backend.repository;

import com.example.talflow_backend.ENTITIES.entity.Transcripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranscripcionRepository extends JpaRepository<Transcripcion, Integer> {

    List<Transcripcion>findByUserEmailOrderByFechaDesc(String email);
    List<Transcripcion> findByUserIdOrderByFechaDesc(Long usuarioId);


}
