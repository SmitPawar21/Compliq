package com.smit.compliq.dto;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractSummaryDTO {
	private String contractPurpose;

    private List<String> keyObligations;

    private String paymentTerms;

    private String contractDuration;

    private List<String> importantClauses;

    private List<String> summary;

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
