package com.example.talflow_backend.STT_TTS.texttospeech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextToSpeechResponse {

    private String errorMessage;
    private String generatedText;
    private String audioBase64;
}
