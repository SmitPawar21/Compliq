package com.smit.compliq.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.compliq.dto.ContractSummaryDTO;
import com.smit.compliq.entity.ContractSummary;
import com.smit.compliq.entity.Document;
import com.smit.compliq.exception.DocumentNotFoundException;
import com.smit.compliq.exception.ObjectMappingException;
import com.smit.compliq.repository.ContractSummaryRepository;
import com.smit.compliq.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SummaryService {

	private final DocumentRepository docRepo;
	private final ContractSummaryRepository conSumRepo;
	private final AIService aiService;
	private final ObjectMapper objectMapper;
	private final VectorStoreService vectorStoreService;
	private final ContextAssembler contextAssembler;

	public ContractSummaryDTO getAISummaryOfContract(long docId) {
		Document doc = docRepo.findById(docId)
				.orElseThrow(() -> new DocumentNotFoundException("Document not found with this id: "+docId));

		String query = "contract obligations, termination clauses, financial terms, payment, SLA, scope of work";
		java.util.List<org.springframework.ai.document.Document> relevantChunks = vectorStoreService.similaritySearch(query, doc.getOrganization().getId());
		String assembledContext = contextAssembler.assembleContext(relevantChunks);
		
		String prompt = com.smit.compliq.prompts.AIPrompts.contractSummaryPrompt.concat("\n\n").concat(assembledContext);
					
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
