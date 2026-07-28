package com.smit.compliq.service;

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

    private final ValidationService validationService;
    private final SummaryService summaryService;
    private final ClauseExtractionService clauseExtractionService;
    private final RiskAnalysisService riskAnalysisService;

    public WorkflowResultDTO runWorkflow(long contractDocId, long invoiceDocId, long poDocId) {
        // Step 1: Validation (Drools)
        ValidationResultDTO validation = validationService.validateDocument(invoiceDocId, poDocId, contractDocId);

        // Step 2: RAG Summary
        ContractSummaryDTO summary = summaryService.getAISummaryOfContract(contractDocId);

        // Step 3: RAG Clause Extraction
        ClauseAnalysisDTO clauses = clauseExtractionService.getAIClauseAnalysis(contractDocId);

        // Step 4: Final Risk Analysis
        RiskAssessmentDTO risk = riskAnalysisService.getRiskAssessment(validation, clauses, summary);

        // Aggregate results
        return new WorkflowResultDTO(summary, clauses, validation, risk);
    }
}
