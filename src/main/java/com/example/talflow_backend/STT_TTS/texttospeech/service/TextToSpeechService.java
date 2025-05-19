package com.example.talflow_backend.STT_TTS.texttospeech.service;

import com.example.talflow_backend.STT_TTS.texttospeech.dto.TextToSpeechResponse;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import com.microsoft.cognitiveservices.speech.audio.PullAudioOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Base64;
import java.util.Optional;

@Service
public class TextToSpeechService {

    private final SpeechConfig speechConfig;

    @Autowired
    public TextToSpeechService(SpeechConfig speechConfig) {
        this.speechConfig = speechConfig;
    }

    public TextToSpeechResponse textToAudio(String generatedText, Optional<String> language) {

        try {
            speechConfig.setSpeechSynthesisVoiceName("es-ES-AlvaroNeural");

            AudioConfig audioConfig = AudioConfig.fromDefaultSpeakerOutput();
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, null);

            // Ejecutar la síntesis
            SpeechSynthesisResult result = synthesizer.SpeakText(generatedText);

            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                byte[] audioData = result.getAudioData();
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
