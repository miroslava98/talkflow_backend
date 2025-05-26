package com.example.talflow_backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public  class OllamaResponse {
    private String response;
    private String audioBase64;

}
