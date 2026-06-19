package com.smit.compliq.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.service.AIService;

@RestController
@RequestMapping("/api/ai")
public class AIController {
	
	private final AIService aiService;
	
	public AIController(AIService aiService) {
		this.aiService = aiService;
	}
	
	@GetMapping("/test")
	public String testAi() {
		try {
			String response = aiService.generateResponse("What is the capital of India? in one word");
			return response;
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
