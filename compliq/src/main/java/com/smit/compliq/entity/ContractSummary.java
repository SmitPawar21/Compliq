package com.smit.compliq.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

public class ContractSummary {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@OneToOne
	private Document document;
	
	@Column
	private String contractPurpose;

	@ElementCollection
    private List<String> keyObligations;

	@Column
    private String paymentTerms;

	@Column
    private String contractDuration;

	@ElementCollection
    private List<String> importantClauses;

	@ElementCollection
    private List<String> summary;
	
	public ContractSummary() {}

	public ContractSummary(long id, Document document, String contractPurpose, List<String> keyObligations,
			String paymentTerms, String contractDuration, List<String> importantClauses, List<String> summary) {
		super();
		this.id = id;
		this.document = document;
		this.contractPurpose = contractPurpose;
		this.keyObligations = keyObligations;
		this.paymentTerms = paymentTerms;
		this.contractDuration = contractDuration;
		this.importantClauses = importantClauses;
		this.summary = summary;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getContractPurpose() {
		return contractPurpose;
	}

	public void setContractPurpose(String contractPurpose) {
		this.contractPurpose = contractPurpose;
	}

	public List<String> getKeyObligations() {
		return keyObligations;
	}

	public void setKeyObligations(List<String> keyObligations) {
		this.keyObligations = keyObligations;
	}

	public String getPaymentTerms() {
		return paymentTerms;
	}

	public void setPaymentTerms(String paymentTerms) {
		this.paymentTerms = paymentTerms;
	}

	public String getContractDuration() {
		return contractDuration;
	}

	public void setContractDuration(String contractDuration) {
		this.contractDuration = contractDuration;
	}

	public List<String> getImportantClauses() {
		return importantClauses;
	}

	public void setImportantClauses(List<String> importantClauses) {
		this.importantClauses = importantClauses;
	}

	public List<String> getSummary() {
		return summary;
	}

	public void setSummary(List<String> summary) {
		this.summary = summary;
	}
	
	
}
