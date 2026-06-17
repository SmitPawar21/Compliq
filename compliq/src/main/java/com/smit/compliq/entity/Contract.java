package com.smit.compliq.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Contract {
	@Id
	private String contractNumber;
	
	@ManyToOne
	@JoinColumn(name="vendorId", nullable=false)
    private Vendor vendor;
	
	@Column
    private Date startDate;
    
	@Column
	private Date endDate;
	
	@OneToOne
	@JoinColumn(name="docId", nullable=false)
	private Document document;
	
	public Contract() {}
    
	public Contract(String contractNumber, Vendor vendor, Date startDate, Date endDate, Document document) {
		super();
		this.contractNumber = contractNumber;
		this.vendor = vendor;
		this.startDate = startDate;
		this.endDate = endDate;
		this.document = document;
	}
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getContractNumber() {
		return contractNumber;
	}
	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}
	public Vendor getVendor() {
		return vendor;
	}
	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
    
    
    
}
