package com.smit.compliq.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.compliq.AIPrompts;
import com.smit.compliq.dto.ClauseAnalysisDTO;
import com.smit.compliq.dto.ContractSummaryDTO;
import com.smit.compliq.dto.RiskAssessmentDTO;
import com.smit.compliq.dto.ValidationResultDTO;
import com.smit.compliq.entity.ClauseAnalysis;
import com.smit.compliq.entity.ContractSummary;
import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.ExtractedDocument;
import com.smit.compliq.exception.AIGenerationException;
import com.smit.compliq.exception.DocumentNotFoundException;
import com.smit.compliq.exception.ObjectMappingException;
import com.smit.compliq.repository.ClauseAnalysisRepository;
import com.smit.compliq.repository.ContractSummaryRepository;
import com.smit.compliq.repository.DocumentRepository;
import com.smit.compliq.repository.ExtractedDocumentRepository;

@Service
public class AIAnalysisService {
	
	private DocumentRepository docRepo;
	private ExtractedDocumentRepository extractedDocRepo;
	private ContractSummaryRepository conSumRepo;
	private ClauseAnalysisRepository clauseAnalysisRepo;
	private AIService aiService;
	private final ObjectMapper objectMapper;
	
	public AIAnalysisService(DocumentRepository docRepo, ExtractedDocumentRepository extractedDocRepo, AIService aiService, ObjectMapper objectMapper, ContractSummaryRepository conSumRepo, ClauseAnalysisRepository clauseAnalysisRepo) {
		this.docRepo = docRepo;
		this.extractedDocRepo = extractedDocRepo;
		this.aiService = aiService;
		this.objectMapper = objectMapper;
		this.conSumRepo = conSumRepo;
		this.clauseAnalysisRepo = clauseAnalysisRepo;
	}
	
	public ContractSummaryDTO getAISummaryOfContract(long docId) {
		Document doc = docRepo.findById(docId)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found with this id: "+docId));

		ExtractedDocument extractedDocument = extractedDocRepo.findByDocument_DocId(docId);
		
		String contractExtractedText = extractedDocument.getExtractedText();
		
		if(contractExtractedText == null || contractExtractedText.isBlank()) {
		    throw new AIGenerationException(
		            "Cannot generate summary. Extracted text is empty."
		    );
		}
		
		String prompt = com.smit.compliq.prompts.AIPrompts.contractSummaryPrompt.concat(contractExtractedText);
					
		try {			
			String jsonResponse = aiService.generateResponse(prompt);
			ContractSummaryDTO response = objectMapper.readValue(jsonResponse, ContractSummaryDTO.class);
			
			saveSummary(doc, response);
			return response;
		} catch (JsonProcessingException e) {
			throw new ObjectMappingException("Object Mapping Error: "+ e.getMessage());
		}
	}
	
	public ClauseAnalysisDTO getAIClauseAnalysis(long docId) {
		Document doc = docRepo.findById(docId)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found with this id: "+docId));

		ExtractedDocument extractedDocument = extractedDocRepo.findByDocument_DocId(docId);
		
		String contractExtractedText = extractedDocument.getExtractedText();
		
		if(contractExtractedText == null || contractExtractedText.isBlank()) {
		    throw new AIGenerationException(
		            "Cannot generate summary. Extracted text is empty."
		    );
		}
		
		String prompt = com.smit.compliq.prompts.AIPrompts.clauseAnalysisPrompt.concat(contractExtractedText);
		
		try {			
			String jsonResponse = aiService.generateResponse(prompt);
			ClauseAnalysisDTO response = objectMapper.readValue(jsonResponse, ClauseAnalysisDTO.class);
			
			saveClauseAnalysis(doc, response);
			return response;
		} catch (JsonProcessingException e) {
			throw new ObjectMappingException("Object Mapping Error: "+ e.getMessage());
		}	
	}
	
	public RiskAssessmentDTO getRiskAssessment(ValidationResultDTO validationResult, ClauseAnalysisDTO clauseAnalysis, ContractSummaryDTO contractSummary) {
		
		String prompt = """
	            You are a procurement compliance and risk analyst.

	            Analyze the information below and return ONLY valid JSON.

	            {
	              "riskLevel": "",
	              "risks",: [],
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
	        throw new ObjectMappingException("Failed to parse risk assessment response"+ e);
	    }

	}
	
	private void saveSummary(Document doc, ContractSummaryDTO dto) {
		ContractSummary conSum = conSumRepo.findByDocument(doc)
	                				.orElse(new ContractSummary());
		
		conSum.setDocument(doc);
		conSum.setContractDuration(dto.getContractDuration());
		conSum.setContractPurpose(dto.getContractPurpose());
		conSum.setImportantClauses(dto.getImportantClauses());
		conSum.setKeyObligations(dto.getKeyObligations());
		conSum.setPaymentTerms(dto.getPaymentTerms());
		conSum.setSummary(dto.getSummary());
		
		conSumRepo.save(conSum);
	}
	
	private void saveClauseAnalysis(Document doc, ClauseAnalysisDTO dto) {
		ClauseAnalysis clauseAnalysis = clauseAnalysisRepo.findByDocument(doc)
										.orElse(new ClauseAnalysis());
		
		clauseAnalysis.setDocument(doc);
		clauseAnalysis.setMissingClauses(dto.getMissingClauses());
		clauseAnalysis.setPresentClauses(dto.getPresentClauses());
		
		clauseAnalysisRepo.save(clauseAnalysis);
	}
}
