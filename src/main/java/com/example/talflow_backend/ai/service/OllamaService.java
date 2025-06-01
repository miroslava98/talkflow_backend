package com.example.talflow_backend.ai.service;

import com.example.talflow_backend.ai.controller.OllamaController;
import com.example.talflow_backend.ai.dto.ChatRequest;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*

{
    "model": "gemma:2b",
    "messages": [
        {
            "role": "system",
            "content": "Eres un camarero francés que sabe frances y algo de inglés, eres abierto a hablar con turistas y les ayudas a aprender el idioma."
        },
        {
            "role": "user",
            "content": "I want to drink coffee, how do I say in french?"
        }
    ]
}
 */

@Service
public class OllamaService {

    private final WebClient webClient = WebClient.create("http://20.82.90.53:11434");


    public String enviarAlModelo(ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // Primero el prompt del sistema
        messages.add(Map.of(
                "role", "system",
                "content", request.getSystemPrompt()
        ));
        messages.add(Map.of(
                "role", "system",
                "content", "You are a language assistant helping users learn " + request.getLanguage() + ". " +
                        "Always answer following this structure:\n" +
                        "- In " + request.getLanguage() + ", you say: \"[translated phrase]\".\n" +
                        "- Translation: [translation to ].\n" +
                        "- Explanation: \"word1\" = meaning, \"word2\" = meaning.\n" +
                        "Use natural and accurate phrases, and avoid placeholders like 'word1' or 'literal translation'."
        ));

        // Ejemplo 1
        messages.add(Map.of(
                "role", "user",
                "content", "How do I greet someone politely?"
        ));
        messages.add(Map.of(
                "role", "assistant",
                "content", "In " + request.getLanguage() +" , you say: \"Good morning\".\n" +
                        "Translation: Buenos días.\n" +
                        "Explanation: \"Good\" = bueno, \"morning\" = mañana."
        ));

// Ejemplo 2
        messages.add(Map.of(
                "role", "user",
                "content", "How do I say 'I want to book a room' in English?"
        ));
        messages.add(Map.of(
                "role", "assistant",
                "content", "In " + request.getLanguage() + ", you say: \"I want to book a room\".\n" +
                        "Translation: Quiero reservar una habitación.\n" +
                        "Explanation: \"I want\" = quiero, \"to book\" = reservar, \"a room\" = una habitación."
        ));


        // Luego los mensajes previos (historial)
        for (ChatRequest.Message m : request.getHistory()) {
            messages.add(Map.of(
                    "role", m.getRole(),
                    "content", m.getContent()
            ));
        }

        Map<String, Object> body = Map.of(
                "model", "gemma:2b",
                "stream", false,
                "messages", messages
        );

        OllamaApiResponse response = webClient.post()
                .uri("/api/chat")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(OllamaApiResponse.class)
                .block();

        if (response != null && response.getMessage() != null) {
            return response.getMessage().getContent().trim();
        }

        return "No response from model";
    }


    // DTO para deserializar respuesta de Ollama
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OllamaApiResponse {
        private Message message;

        @Data
        public static class Message {
            private String role;
            private String content;


        }
    }
}


