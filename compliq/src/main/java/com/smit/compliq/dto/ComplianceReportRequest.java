package com.smit.compliq.dto;

import lombok.Data;

@Data
public class ComplianceReportRequest {
	private long contractDocumentId;

    private long poDocumentId;

    private long invoiceDocumentId;

	public long getContractDocumentId() {
		return contractDocumentId;
	}

	public void setContractDocumentId(long contractDocumentId) {
		this.contractDocumentId = contractDocumentId;
	}

	public long getPoDocumentId() {
		return poDocumentId;
	}

	public void setPoDocumentId(long poDocumentId) {
		this.poDocumentId = poDocumentId;
	}

	public long getInvoiceDocumentId() {
		return invoiceDocumentId;
	}

	public void setInvoiceDocumentId(long invoiceDocumentId) {
		this.invoiceDocumentId = invoiceDocumentId;
	}
}
