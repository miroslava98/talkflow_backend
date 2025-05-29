package com.example.talflow_backend.STT_TTS.texttospeech.service;

import com.example.talflow_backend.STT_TTS.texttospeech.dto.TextToSpeechResponse;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import com.microsoft.cognitiveservices.speech.audio.PullAudioOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TextToSpeechService {

    private final SpeechConfig speechConfig;

    @Autowired
    public TextToSpeechService(SpeechConfig speechConfig) {
        this.speechConfig = speechConfig;
    }

    private static final Map<String, String> LANGUAGE_TO_ISO_CODE = Map.of(
            "español", "es",
            "inglés", "en",
            "francés", "fr",
            "alemán", "de"
    );
    private static final Map<String, String> VOICE_BY_LANGUAGE = Map.of(
            "es", "es-ES-ElviraNeural",
            "en", "en-US-JennyNeural",
            "fr", "fr-FR-DeniseNeural",
            "de", "de-DE-KatjaNeural"
    );


    public TextToSpeechResponse textToAudio(String generatedText, String language) {
        String isoCode = LANGUAGE_TO_ISO_CODE.getOrDefault(language.toLowerCase(), "english");
        // Buscar frase entre comillas
        Pattern pattern = Pattern.compile("\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(generatedText);

        String phraseToRead = null;

        if (matcher.find()) {
            phraseToRead = matcher.group(1); // La primera frase entre comillas
        } else {
            // Fallback: usar la primera línea si no hay comillas
            phraseToRead = generatedText.split("\\R")[0]; // \\R es salto de línea
        }

        try {
            String voice = VOICE_BY_LANGUAGE.getOrDefault(isoCode, "en-US-JennyNeural");
            speechConfig.setSpeechSynthesisVoiceName(voice);
            speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio16Khz32KBitRateMonoMp3);


            SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, null);

            // Ejecutar la síntesis
            SpeechSynthesisResult result = synthesizer.SpeakText(phraseToRead);

            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                byte[] audioData = result.getAudioData();

                if (audioData == null || audioData.length == 0) {
                    return new TextToSpeechResponse("Error: audio vacío", generatedText, null);
                }

                // Opcional: escribir archivo local para debug
                try {
                    Files.write(Paths.get("/tmp/testaudio.mp3"), audioData);
                } catch (Exception fileEx) {
                    System.err.println("No se pudo guardar el audio localmente: " + fileEx.getMessage());
                }

                String base64 = Base64.getEncoder().encodeToString(audioData);
                return new TextToSpeechResponse("Audio generado correctamente", generatedText, base64);
            } else {
                return new TextToSpeechResponse("Error al generar audio: " + result.getReason(), generatedText, null);
            }

        } catch (Exception e) {
            return new TextToSpeechResponse("Excepción durante la síntesis: " + e.getMessage(), generatedText
                    , null);
        }
    }

}
