package com.smit.compliq.drools;

import java.util.*;

public class ValidationResult {
	
	private List<ValidationIssue> violations = new ArrayList<>();

    public void addViolation(ValidationIssue violation) {
        violations.add(violation);
    }

    public List<ValidationIssue> getViolations() {
        return violations;
    }

    public boolean hasViolations() {
        return !violations.isEmpty();
    }
    
}
