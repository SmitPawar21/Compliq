package com.smit.compliq.dto;

import com.smit.compliq.enums.VendorStatus;

import jakarta.validation.constraints.NotBlank;

public class VendorDTO {
	@NotBlank
	private String vendorName;
	
	@NotBlank
	private long gstNumber;
	
	@NotBlank
	private long panNumber;
	
	@NotBlank
	private String email;
	
	@NotBlank
	private long phone;
	
	private VendorStatus vendorStatus;

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public long getGstNumber() {
		return gstNumber;
	}

	public void setGstNumber(long gstNumber) {
		this.gstNumber = gstNumber;
	}

	public long getPanNumber() {
		return panNumber;
	}

	public void setPanNumber(long panNumber) {
		this.panNumber = panNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getPhone() {
		return phone;
	}

	public void setPhone(long phone) {
		this.phone = phone;
	}

	public VendorStatus getVendorStatus() {
		return vendorStatus;
	}

	public void setVendorStatus(VendorStatus vendorStatus) {
		this.vendorStatus = vendorStatus;
	}
	
}
