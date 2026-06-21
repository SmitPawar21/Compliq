package com.smit.compliq.dto;

public class ComplianceReportDTO {
	private ValidationResultDTO validationResult;

    private ContractSummaryDTO contractSummary;

    private ClauseAnalysisDTO clauseAnalysis;

    private RiskAssessmentDTO riskAssessment;
    
    public ComplianceReportDTO() {}

	public ValidationResultDTO getValidationResult() {
		return validationResult;
	}

	public void setValidationResult(ValidationResultDTO validationResult) {
		this.validationResult = validationResult;
	}

	public ContractSummaryDTO getContractSummary() {
		return contractSummary;
	}

	public void setContractSummary(ContractSummaryDTO contractSummary) {
		this.contractSummary = contractSummary;
	}

	public ClauseAnalysisDTO getClauseAnalysis() {
		return clauseAnalysis;
	}

	public void setClauseAnalysis(ClauseAnalysisDTO clauseAnalysis) {
		this.clauseAnalysis = clauseAnalysis;
	}

	public RiskAssessmentDTO getRiskAssessment() {
		return riskAssessment;
	}

	public void setRiskAssessment(RiskAssessmentDTO riskAssessment) {
		this.riskAssessment = riskAssessment;
	}
}
