package com.smit.compliq.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.smit.compliq.dto.ClauseAnalysisDTO;
import com.smit.compliq.dto.ContractSummaryDTO;
import com.smit.compliq.dto.RiskAssessmentDTO;
import com.smit.compliq.dto.ValidationResultDTO;
import com.smit.compliq.dto.WorkflowResultDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(AIWorkflowService.class);

    private final ValidationService validationService;
    private final SummaryService summaryService;
    private final ClauseExtractionService clauseExtractionService;
    private final RiskAnalysisService riskAnalysisService;

    @Cacheable(value = "aiWorkflow", key = "#contractDocId")
    public WorkflowResultDTO runWorkflow(long contractDocId, long invoiceDocId, long poDocId) {
        DiagnosticContextHolder.clearContext();
        
        try {
            // Step 1: Validation (Drools)
            ValidationResultDTO validation = validationService.validateDocument(invoiceDocId, poDocId, contractDocId);

        // Step 2: RAG Summary
        ContractSummaryDTO summary = summaryService.getAISummaryOfContract(contractDocId);

        // Step 3: RAG Clause Extraction
        ClauseAnalysisDTO clauses = clauseExtractionService.getAIClauseAnalysis(contractDocId);

        // Step 4: Final Risk Analysis
        RiskAssessmentDTO risk = riskAnalysisService.getRiskAssessment(validation, clauses, summary);

        // Aggregate results
        WorkflowResultDTO result = new WorkflowResultDTO(summary, clauses, validation, risk);
        
        log.info(DiagnosticContextHolder.getContext().getSummary());
        return result;
        } finally {
            DiagnosticContextHolder.clearContext();
        }
    }
}
