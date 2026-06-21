package com.smit.compliq.service;

import org.springframework.stereotype.Service;

import com.smit.compliq.dto.ClauseAnalysisDTO;
import com.smit.compliq.dto.ComplianceReportDTO;
import com.smit.compliq.dto.ComplianceReportRequest;
import com.smit.compliq.dto.ContractSummaryDTO;
import com.smit.compliq.dto.RiskAssessmentDTO;
import com.smit.compliq.dto.ValidationResultDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplianceReportService {
	private final ValidationService validationService;
    private final AIAnalysisService aiAnalysisService;
    
	public ComplianceReportService(ValidationService validationService, AIAnalysisService aiAnalysisService) {
		super();
		this.validationService = validationService;
		this.aiAnalysisService = aiAnalysisService;
	}
    
	public ComplianceReportDTO generateReport(ComplianceReportRequest request) {
		
		// 1. Run Drools for getting violations
		ValidationResultDTO validationResult = validationService.validateDocument(
            request.getInvoiceDocumentId(),
            request.getPoDocumentId(),
            request.getContractDocumentId()
        );
		
		// 2. Contract Summary
		ContractSummaryDTO contractSummary = aiAnalysisService.getAISummaryOfContract(request.getContractDocumentId());
		
		
		// 3. Clause Analysis
		ClauseAnalysisDTO clauseAnalysis = aiAnalysisService.getAIClauseAnalysis(request.getContractDocumentId());
		
		// 4. Risk Assessment
		RiskAssessmentDTO riskAssessment = aiAnalysisService.getRiskAssessment(validationResult, clauseAnalysis, contractSummary);
		
		ComplianceReportDTO report = new ComplianceReportDTO();

        report.setValidationResult(validationResult);
        report.setContractSummary(contractSummary);
        report.setClauseAnalysis(clauseAnalysis);
        report.setRiskAssessment(riskAssessment);
        
        return report;
	}
}
