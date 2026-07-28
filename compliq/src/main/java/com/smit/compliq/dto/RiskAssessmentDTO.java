package com.smit.compliq.dto;

import java.util.*;

public class RiskAssessmentDTO {
	private String riskLevel;
	private int confidenceScore;
	private List<String> risks;
	private List<String> recommendations;
	
	public int getConfidenceScore() {
		return confidenceScore;
	}
	public void setConfidenceScore(int confidenceScore) {
		this.confidenceScore = confidenceScore;
	}
	
	public String getRiskLevel() {
		return riskLevel;
	}
	public void setRiskLevel(String riskLevel) {
		this.riskLevel = riskLevel;
	}
	public List<String> getRisks() {
		return risks;
	}
	public void setRisks(List<String> risks) {
		this.risks = risks;
	}
	public List<String> getRecommendations() {
		return recommendations;
	}
	public void setRecommendations(List<String> recommendations) {
		this.recommendations = recommendations;
	}
	
	
}
