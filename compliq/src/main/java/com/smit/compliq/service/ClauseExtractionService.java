package com.smit.compliq.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.compliq.dto.ClauseAnalysisDTO;
import com.smit.compliq.entity.ClauseAnalysis;
import com.smit.compliq.entity.Document;
import com.smit.compliq.exception.DocumentNotFoundException;
import com.smit.compliq.exception.ObjectMappingException;
import com.smit.compliq.repository.ClauseAnalysisRepository;
import com.smit.compliq.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClauseExtractionService {

	private final DocumentRepository docRepo;
	private final ClauseAnalysisRepository clauseAnalysisRepo;
	private final AIService aiService;
	private final ObjectMapper objectMapper;
	private final VectorStoreService vectorStoreService;
	private final ContextAssembler contextAssembler;

	public ClauseAnalysisDTO getAIClauseAnalysis(long docId) {
		Document doc = docRepo.findById(docId)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found with this id: "+docId));

		String query = "liability, indemnification, warranty, confidentiality, dispute resolution, governing law";
		java.util.List<org.springframework.ai.document.Document> relevantChunks = vectorStoreService.similaritySearch(query, doc.getOrganization().getId());
		String assembledContext = contextAssembler.assembleContext(relevantChunks);
		
		String prompt = com.smit.compliq.prompts.AIPrompts.clauseAnalysisPrompt.concat("\n\n").concat(assembledContext);
		
		try {			
			String jsonResponse = aiService.generateResponse(prompt);
			ClauseAnalysisDTO response = objectMapper.readValue(jsonResponse, ClauseAnalysisDTO.class);
			
			saveClauseAnalysis(doc, response);
			return response;
		} catch (JsonProcessingException e) {
			throw new ObjectMappingException("Object Mapping Error: "+ e.getMessage());
		}	
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
