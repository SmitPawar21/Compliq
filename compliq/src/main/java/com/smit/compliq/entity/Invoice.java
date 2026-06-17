package com.smit.compliq.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Invoice {
	@Id
	private String invoiceNumber;
	
	@ManyToOne
	@JoinColumn(name = "vendorId", nullable = false)
    private Vendor vendor;
	
	@Column
	private double totalAmount;
    
	@Column
	private Date invoiceDate;
	
	@OneToOne
	@JoinColumn(name="docId", nullable=false)
	private Document document;
	
	public Invoice() {}

	public Invoice(String invoiceNumber, Vendor vendor, double totalAmount, Date invoiceDate, Document document) {
		super();
		this.invoiceNumber = invoiceNumber;
		this.vendor = vendor;
		this.totalAmount = totalAmount;
		this.invoiceDate = invoiceDate;
		this.document = document;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
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

	public Date getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
    
}
