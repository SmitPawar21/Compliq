package com.smit.compliq.dto;

import com.smit.compliq.enums.Severity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationIssueDTO {
	private String ruleName;

    private String message;

    private Severity severity;
    
    public ValidationIssueDTO() {}

	public ValidationIssueDTO(String ruleName, String message, Severity severity) {
		super();
		this.ruleName = ruleName;
		this.message = message;
		this.severity = severity;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}
    
    
}
