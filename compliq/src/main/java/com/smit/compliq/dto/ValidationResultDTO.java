package com.smit.compliq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResultDTO {
	private boolean hasViolations;

    private List<ValidationIssueDTO> violations;
    
    public ValidationResultDTO(List<ValidationIssueDTO> violations) {
    	this.violations = violations;
    }

	public boolean isHasViolations() {
		return hasViolations;
	}

	public void setHasViolations(boolean hasViolations) {
		this.hasViolations = hasViolations;
	}

	public List<ValidationIssueDTO> getViolations() {
		return violations;
	}

	public void setViolations(List<ValidationIssueDTO> violations) {
		this.violations = violations;
	}
    
}
