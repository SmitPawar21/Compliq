package com.smit.compliq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.dto.ClauseAnalysisDTO;
import com.smit.compliq.dto.ContractSummaryDTO;
import com.smit.compliq.service.AIAnalysisService;
import com.smit.compliq.service.AIService;

@RestController
@RequestMapping("/api/ai")
public class AIController {
	
	private final AIService aiService;
	private final AIAnalysisService aiAnalysisService;
	
	public AIController(AIService aiService, AIAnalysisService aiAnalysisService) {
		this.aiService = aiService;
		this.aiAnalysisService = aiAnalysisService;
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
	
	@GetMapping("/document/{id}/analyze-summary") 
	public ResponseEntity<?> analyzeSummary(@PathVariable long id) {
		try {
			ContractSummaryDTO response = aiAnalysisService.getAISummaryOfContract(id);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
		}
	}
	
	@GetMapping("/document/{id}/clause-analysis") 
	public ResponseEntity<?> clauseAnalysis(@PathVariable long id) {
		try {
			ClauseAnalysisDTO response = aiAnalysisService.getAIClauseAnalysis(id);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
		}
	}
	
}
