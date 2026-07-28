package com.smit.compliq.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.compliq.dto.ClauseAnalysisDTO;
import com.smit.compliq.dto.ContractSummaryDTO;
import com.smit.compliq.dto.RiskAssessmentDTO;
import com.smit.compliq.dto.ValidationResultDTO;
import com.smit.compliq.exception.ObjectMappingException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskAnalysisService {

	private final AIService aiService;
	private final ObjectMapper objectMapper;

	public RiskAssessmentDTO getRiskAssessment(ValidationResultDTO validationResult, ClauseAnalysisDTO clauseAnalysis, ContractSummaryDTO contractSummary) {
		
		String prompt = """
	            You are a procurement compliance and risk analyst.

	            Analyze the information below and return ONLY valid JSON.

	            {
	              "riskLevel": "",
	              "confidenceScore": 0,
	              "risks": [],
	              "recommendations": []
	            }

	            Rules:
	            - riskLevel must be LOW, MEDIUM, or HIGH.
	            - confidenceScore must be an integer between 0 and 100 based on context completeness.
	            - For every risk and recommendation, append a citation referencing the exact source. (e.g. 'Evidence: Section 4.2').
	            - If evidence does not exist inside retrieved documents, return NOT FOUND. Never fabricate.
	            - look for financial, compliance, operational risks and list them.
	            - recommendations should contain actionable recommendations.
	            - Do not return markdown.
	            - Do not return explanations.

	            Contract Summary:
	            %s

	            Missing Clauses:
	            %s

	            Validation Violations:
	            %s
	            """.formatted(
	            contractSummary.getSummary(),
	            clauseAnalysis.getMissingClauses(),
	            validationResult.getViolations()
	    );
		
		try {
	        return aiService.generateStructuredResponse(prompt, RiskAssessmentDTO.class);
	    } catch (Exception e) {
	        throw new ObjectMappingException("Failed to generate risk assessment response: "+ e.getMessage());
	    }

	}
}
