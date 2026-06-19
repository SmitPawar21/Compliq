package com.smit.compliq.config;

import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;

@Configuration
public class GeminiConfig {
	@Value("${gemini-api-key}")
	private String apiKey;
	
	@Value("${gemini-model}")
    private String model;
	
	@Bean
    public Client googleGenAiClient() {
        // Initializes the new unified Google GenAI client using your API key
        return Client.builder()
            .apiKey(apiKey)
            .build();
    }
	
	@Bean
    public GoogleGenAiChatModel chatModel(Client genAiClient) {
        // Sets up the new GoogleGenAiChatModel using the next-gen client
        GoogleGenAiChatOptions defaultOptions = GoogleGenAiChatOptions.builder()
            .model(model)
            .temperature(0.7)
            .build();
    
        return GoogleGenAiChatModel.builder()
            .genAiClient(genAiClient)
            .defaultOptions(defaultOptions)
            .build();
	}	
}
