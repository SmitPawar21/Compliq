package com.smit.compliq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowResultDTO {
    private ContractSummaryDTO summary;
    private ClauseAnalysisDTO clauses;
    private ValidationResultDTO validation;
    private RiskAssessmentDTO risk;
}
