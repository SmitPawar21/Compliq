package com.smit.compliq.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.smit.compliq.exception.AIGenerationException;
import com.smit.compliq.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiAIService implements AIService {
	private final ChatClient chatClient;
	private final ObjectMapper objectMapper;

    public GeminiAIService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateResponse(String prompt) {
    	try {    		
    		return this.chatClient.prompt()
    				.user(prompt)
    				.call()
    				.content(); // Safely extracts the text response
    	} catch (Exception e) {
    		throw new AIGenerationException("Failed to generate AI response: \n"+ e);
    	}
    }

    @Override
    public <T> T generateStructuredResponse(String prompt, Class<T> responseType) {
        int maxRetries = 2;
        int attempts = 0;
        String currentPrompt = prompt;

        while (attempts < maxRetries) {
            try {
                String response = this.chatClient.prompt().user(currentPrompt).call().content();
                
                // Clean markdown JSON wrapping if any
                if (response.startsWith("```json")) {
                    response = response.substring(7);
                }
                if (response.endsWith("```")) {
                    response = response.substring(0, response.length() - 3);
                }
                response = response.trim();
                
                return objectMapper.readValue(response, responseType);
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxRetries) {
                    throw new AIGenerationException("Failed to generate valid JSON after retries: " + e.getMessage());
                }
                currentPrompt = prompt + "\n\nWARNING: Your previous response was invalid JSON. Please ensure you output strictly valid JSON conforming to the requested schema. Error: " + e.getMessage();
            }
        }
        return null;
    }
}
