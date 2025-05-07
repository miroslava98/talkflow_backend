package com.example.talflow_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Transcripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idioma;

    @Lob
    private String texto;

    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    public Transcripcion() {
    }


    public Transcripcion(Long id, String idioma, String texto, LocalDateTime fecha, User user) {
        this.id = id;
        this.idioma = idioma;
        this.texto = texto;
        this.fecha = fecha;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
