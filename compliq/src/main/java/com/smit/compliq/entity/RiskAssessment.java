package com.smit.compliq.entity;

import java.util.*;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class RiskAssessment {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String riskLevel;
	
	@ElementCollection
	private List<String> risks;
	
	@ElementCollection
	private List<String> recommendations;
	
	@OneToOne
	private Document document;

	public RiskAssessment() {}

	public RiskAssessment(long id, String riskLevel, List<String> risks, List<String> recommendations, Document document) {
		super();
		this.id = id;
		this.riskLevel = riskLevel;
		this.risks = risks;
		this.recommendations = recommendations;
		this.document = document;
	}
	
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
