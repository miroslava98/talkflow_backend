package com.example.talflow_backend.STT_TTS.texttospeech.service;

import com.example.talflow_backend.STT_TTS.texttospeech.dto.TextToSpeechResponse;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import com.microsoft.cognitiveservices.speech.audio.PullAudioOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class TextToSpeechService {

    private final SpeechConfig speechConfig;

    @Autowired
    public TextToSpeechService(SpeechConfig speechConfig) {
        this.speechConfig = speechConfig;
    }

    private static final Map<String, String> VOICE_BY_LANGUAGE = Map.of(
            "es", "es-ES-ElviraNeural",
            "en", "en-US-JennyNeural",
            "fr", "fr-FR-DeniseNeural",
            "de", "de-DE-KatjaNeural"
            // Agrega más si lo necesitas
    );


    public TextToSpeechResponse textToAudio(String generatedText, Optional<String> language) {
        String fullLang = language.orElse("en"); // inglés por defecto
        String langKey = fullLang.split("-")[0];
        try {
            String voice = VOICE_BY_LANGUAGE.getOrDefault(langKey, "en-US-JennyNeural");
            speechConfig.setSpeechSynthesisVoiceName(voice);
            speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio16Khz32KBitRateMonoMp3);


            SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, null);

            // Ejecutar la síntesis
            SpeechSynthesisResult result = synthesizer.SpeakText(generatedText);

            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                byte[] audioData = result.getAudioData();

                if (audioData == null || audioData.length == 0) {
                    return new TextToSpeechResponse("Error: audio vacío", generatedText, null);
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
