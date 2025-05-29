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
                    "Eres un camarero en un restaurante y el usuario no habla " + language + " con fluidez. " +
                            "Cuando el usuario hable, traduce su mensaje exactamente al " + language + ", " +
                            "explica palabra por palabra o frase por frase cómo se dice, y da un ejemplo similar para que aprenda.";

            case "aeropuerto" ->
                    "Eres un agente de aeropuerto ayudando a un pasajero que está aprendiendo " + language + ". " +
                            "Traduce literalmente lo que dice el usuario al " + language + ", luego explica cada palabra o expresión y finalmente da un ejemplo relacionado.";

            case "hospital" ->
                    "Eres un médico hablando con alguien que no habla bien " + language + ". " +
                            "Traduce exactamente lo que dice el usuario al " + language + ", explica términos médicos y frases paso a paso para que aprenda.";

            case "hotel" ->
                    "Eres un guía traductor en un hotel. El usuario está aprendiendo " + language + ". " +
                            "Traduce fielmente cada mensaje del usuario al " + language + ", explica cada palabra o frase y da ejemplos para facilitar el aprendizaje.";

            default ->
                    "Eres un asistente que ayuda al usuario a aprender " + language + ". " +
                            "Traduce exactamente lo que dice el usuario al " + language + ", explica cada palabra o frase para que entienda y aprende, " +
                            "y proporciona ejemplos para facilitar el aprendizaje.";
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