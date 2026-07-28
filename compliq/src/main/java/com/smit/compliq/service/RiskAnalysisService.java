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
	              "risks": [],
	              "recommendations": []
	            }

	            Rules:
	            - riskLevel must be LOW, MEDIUM, or HIGH.
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
	        String jsonResponse = aiService.generateResponse(prompt);
	        return objectMapper.readValue(jsonResponse, RiskAssessmentDTO.class);
	    } catch (JsonProcessingException e) {
	        throw new ObjectMappingException("Failed to parse risk assessment response: "+ e.getMessage());
	    }

	}
}
