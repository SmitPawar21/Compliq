package com.smit.compliq.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.smit.compliq.service.AIService;

@Service
public class GeminiAIService implements AIService {
	private final ChatClient chatClient;

    public GeminiAIService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String generateResponse(String prompt) {
        return this.chatClient.prompt()
            .user(prompt)
            .call()
            .content(); // Safely extracts the text response
    }
}
