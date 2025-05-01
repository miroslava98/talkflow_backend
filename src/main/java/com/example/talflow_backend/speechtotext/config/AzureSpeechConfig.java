package com.example.talflow_backend.speechtotext.config;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureSpeechConfig {

    private static final Dotenv dotenv = Dotenv.load();
    private static String speechKey = dotenv.get("SPEECH_KEY");
    private static String speechRegion = dotenv.get("SPEECH_REGION");


    @Bean
    public SpeechConfig speechConfig() {
        return SpeechConfig.fromSubscription(speechKey, speechRegion);
    }
}
