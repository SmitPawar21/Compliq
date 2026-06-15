package com.smit.compliq.dto;

import jakarta.validation.constraints.NotBlank;

public class VendorDTO {
	@NotBlank
	private String vendorName;
	
	@NotBlank
	private String gstNumber;
	
	@NotBlank
	private String panNumber;
	
	@NotBlank
	private String email;
	
	@NotBlank
	private long phone;

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getGstNumber() {
		return gstNumber;
	}

	public void setGstNumber(String gstNumber) {
		this.gstNumber = gstNumber;
	}

	public String getPanNumber() {
		return panNumber;
	}

	public void setPanNumber(String panNumber) {
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
	
}
