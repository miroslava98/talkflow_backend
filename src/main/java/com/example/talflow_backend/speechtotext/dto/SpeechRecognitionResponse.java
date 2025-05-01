package com.example.talflow_backend.speechtotext.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class SpeechRecognitionResponse {
        private String recognizedText;
        private String status;
        private String errorMessage;
    }

