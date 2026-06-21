package com.smit.compliq.dto;
import java.util.*;

public class ClauseAnalysisDTO {
	private List<String> presentClauses;

    private List<String> missingClauses;

	public List<String> getPresentClauses() {
		return presentClauses;
	}

	public void setPresentClauses(List<String> presentClauses) {
		this.presentClauses = presentClauses;
	}

	public List<String> getMissingClauses() {
		return missingClauses;
	}

	public void setMissingClauses(List<String> missingClauses) {
		this.missingClauses = missingClauses;
	}
    
    
}
