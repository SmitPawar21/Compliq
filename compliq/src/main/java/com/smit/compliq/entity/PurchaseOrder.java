package com.smit.compliq.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class PurchaseOrder {
	@Id
	private String poNumber;
	
	@ManyToOne
	@JoinColumn(name="vendorId", nullable=false)
    private Vendor vendor;
    
	@Column
	private double totalAmount;
    
	@Column
	private Date issueDate;
	
	@OneToOne
	@JoinColumn(name="docId", nullable=false)
	private Document document;
	
	public PurchaseOrder() {}

	public PurchaseOrder(String poNumber, Vendor vendor, double totalAmount, Date issueDate, Document document) {
		super();
		this.poNumber = poNumber;
		this.vendor = vendor;
		this.totalAmount = totalAmount;
		this.issueDate = issueDate;
		this.document = document;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
}
