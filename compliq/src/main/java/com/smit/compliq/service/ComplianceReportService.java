package com.smit.compliq.service;

import org.springframework.stereotype.Service;

import com.smit.compliq.dto.ClauseAnalysisDTO;
import com.smit.compliq.dto.ComplianceReportDTO;
import com.smit.compliq.dto.ComplianceReportRequest;
import com.smit.compliq.dto.ContractSummaryDTO;
import com.smit.compliq.dto.RiskAssessmentDTO;
import com.smit.compliq.dto.ValidationResultDTO;
import com.smit.compliq.dto.WorkflowResultDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplianceReportService {
	private final ValidationService validationService;
    private final AIWorkflowService aiWorkflowService;
    
    
	public ComplianceReportDTO generateReport(ComplianceReportRequest request) {
		
		WorkflowResultDTO result = aiWorkflowService.runWorkflow(
			request.getContractDocumentId(),
			request.getInvoiceDocumentId(),
			request.getPoDocumentId()
		);
		
		ComplianceReportDTO report = new ComplianceReportDTO();

        report.setValidationResult(result.getValidation());
        report.setContractSummary(result.getSummary());
        report.setClauseAnalysis(result.getClauses());
        report.setRiskAssessment(result.getRisk());
        
        return report;
	}
}
