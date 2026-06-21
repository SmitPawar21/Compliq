package com.smit.compliq.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.*;

@Entity
public class ClauseAnalysis {
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Document document;

    @ElementCollection
    private List<String> presentClauses;

    @ElementCollection
    private List<String> missingClauses;
    
    public ClauseAnalysis() {}

	public ClauseAnalysis(Long id, Document document, List<String> presentClauses, List<String> missingClauses) {
		super();
		this.id = id;
		this.document = document;
		this.presentClauses = presentClauses;
		this.missingClauses = missingClauses;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

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
