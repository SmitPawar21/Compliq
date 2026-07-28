package com.smit.compliq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.dto.ClauseAnalysisDTO;
import com.smit.compliq.dto.ContractSummaryDTO;
import com.smit.compliq.dto.WorkflowResultDTO;
import com.smit.compliq.service.AIService;
import com.smit.compliq.service.SummaryService;
import com.smit.compliq.service.ClauseExtractionService;
import com.smit.compliq.service.AIWorkflowService;

@RestController
@RequestMapping("/api/ai")
public class AIController {
	
	private final AIService aiService;
	private final SummaryService summaryService;
	private final ClauseExtractionService clauseExtractionService;
	private final AIWorkflowService aiWorkflowService;
	
	public AIController(AIService aiService, SummaryService summaryService, ClauseExtractionService clauseExtractionService, AIWorkflowService aiWorkflowService) {
		this.aiService = aiService;
		this.summaryService = summaryService;
		this.clauseExtractionService = clauseExtractionService;
		this.aiWorkflowService = aiWorkflowService;
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
			ContractSummaryDTO response = summaryService.getAISummaryOfContract(id);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
		}
	}
	
	@GetMapping("/document/{id}/clause-analysis") 
	public ResponseEntity<?> clauseAnalysis(@PathVariable long id) {
		try {
			ClauseAnalysisDTO response = clauseExtractionService.getAIClauseAnalysis(id);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
		}
	}
	
	@GetMapping("/workflow/{contractId}/{invoiceId}/{poId}")
	public ResponseEntity<?> runWorkflow(@PathVariable long contractId, @PathVariable long invoiceId, @PathVariable long poId) {
		try {
			WorkflowResultDTO response = aiWorkflowService.runWorkflow(contractId, invoiceId, poId);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}
