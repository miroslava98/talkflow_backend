package com.example.talflow_backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {
    private List<Message> history;   // El texto transcrito
    private String scenePrompt;   // La escena o prompt de sistema
    private String language;      // Idioma para la respuesta


    public String getSystemPrompt() {
        return switch (scenePrompt.toLowerCase()) {
            case "restaurante" ->
                    "Responde en el idioma equivalente de la siguiente localización: " + language +
                            "Eres un camarero amable que ayuda a los turistas a aprender el idioma y mejora su pronunciación.";

            case "aeropuerto" ->
                    "Eres un agente de aeropuerto que asiste a pasajeros. Responde exclusivamente en el idioma equivalente de la siguiente localización: " + language + ". También ayuda con la pronunciación.";
            case "hospital" ->
                    "Eres un médico que explica síntomas y tratamientos de forma clara. Responde siempre en el idioma equivalente de la siguiente localización: " + language + ". También ayuda con la pronunciación.";
            case "hotel" ->
                    "Eres recepcionista en un hotel que ayuda a los huéspedes. Responde siempre en el idioma equivalente de la siguiente localización: " + language + ". También ayuda con la pronunciación.";
            default ->
                    "Eres un asistente conversacional. Responde en el idioma equivalente de la siguiente localización: " + language + ". También ayuda con la pronunciación.";
        };
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}