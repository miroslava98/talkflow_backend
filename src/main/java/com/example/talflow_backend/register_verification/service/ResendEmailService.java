package com.example.talflow_backend.register_verification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ResendEmailService {

    private static final String API_URL = "https://api.resend.com/emails";


    private static final String verifcationBaseUrl = "https://205b-62-42-180-254.ngrok-free.app";
    @Value("${resend.api.key}")
    private String API_KEY;

    // Puedes definir el HTML una sola vez
    private static final String HTML_CONTENT = """
                <h1>Verifica tu cuenta</h1>
                <p>Gracias por registrarte en TalkFlow.</p>
                <p>Haz clic en el siguiente enlace para verificar tu cuenta:</p>
                <a href="%s">Verificar cuenta</a>
            """;

    public void sendVerificationEmail(String toEmail, String token) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String pathApi = "/auth/verificationRegister/verify";
        String verificationUrl = verifcationBaseUrl + pathApi+ "?token=" + token;
        String html = String.format(HTML_CONTENT, verificationUrl);

        Map<String, Object> body = new HashMap<>();
        body.put("from", "onboarding@resend.dev");
        body.put("to", List.of(toEmail));
        body.put("subject", "Verifica tu cuenta - TalkFlow");
        body.put("html", html);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
            System.out.println("Resend Response: " + response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("❌ Error al enviar email con Resend:");
            System.err.println("Status: " + e.getStatusCode());
            System.err.println("Body: " + e.getResponseBodyAsString());
            throw e; // Opcional: o puedes lanzar una RuntimeException personalizada
        } catch (Exception e) {
            System.err.println("❌ Error inesperado:");
            e.printStackTrace();
            throw e;
        }
    }
}
