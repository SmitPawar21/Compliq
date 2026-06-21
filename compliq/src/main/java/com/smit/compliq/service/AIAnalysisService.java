package com.smit.compliq.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.compliq.AIPrompts;
import com.smit.compliq.dto.ContractSummaryDTO;
import com.smit.compliq.entity.ContractSummary;
import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.ExtractedDocument;
import com.smit.compliq.exception.AIGenerationException;
import com.smit.compliq.exception.DocumentNotFoundException;
import com.smit.compliq.exception.ObjectMappingException;
import com.smit.compliq.repository.ContractSummaryRepository;
import com.smit.compliq.repository.DocumentRepository;
import com.smit.compliq.repository.ExtractedDocumentRepository;

@Service
public class AIAnalysisService {
	
	private DocumentRepository docRepo;
	private ExtractedDocumentRepository extractedDocRepo;
	private ContractSummaryRepository conSumRepo;
	private AIService aiService;
	private final ObjectMapper objectMapper;
	
	public AIAnalysisService(DocumentRepository docRepo, ExtractedDocumentRepository extractedDocRepo, AIService aiService, ObjectMapper objectMapper, ContractSummaryRepository conSumRepo) {
		this.docRepo = docRepo;
		this.extractedDocRepo = extractedDocRepo;
		this.aiService = aiService;
		this.objectMapper = objectMapper;
		this.conSumRepo = conSumRepo;
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
		
		String prompt = AIPrompts.contractSummaryPrompt.concat(contractExtractedText);
					
		try {			
			String jsonResponse = aiService.generateResponse(prompt);
			ContractSummaryDTO response = objectMapper.readValue(jsonResponse, ContractSummaryDTO.class);
			
			saveSummary(doc, response);
			return response;
		} catch (JsonProcessingException e) {
			throw new ObjectMappingException("Object Mapping Error: "+ e.getMessage());
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
}
