package com.smit.compliq.service;

import org.springframework.stereotype.Service;

@Service
public interface AIService {
	String generateResponse(String prompt);
	<T> T generateStructuredResponse(String prompt, Class<T> responseType);
}
