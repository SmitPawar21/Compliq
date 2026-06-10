package com.smit.compliq.entity;

import com.smit.compliq.enums.VendorStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="vendor")
public class Vendor {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long vendor_id;
	
	@Column
	private String vendorName;
	
	@Column
	private long gstNumber;
	
	@Column
	private long panNumber;
	
	@Column
	private String email;
	
	@Column
	private long phone;
	
	@Column
	private VendorStatus vendorStatus;
	
	public Vendor() {}

	public Vendor(String vendorName, long gstNumber, long panNumber, String email, long phone,
			VendorStatus vendorStatus) {
		super();
		this.vendorName = vendorName;
		this.gstNumber = gstNumber;
		this.panNumber = panNumber;
		this.email = email;
		this.phone = phone;
		this.vendorStatus = vendorStatus;
	}

	public long getVendor_id() {
		return vendor_id;
	}

	public void setVendor_id(long vendor_id) {
		this.vendor_id = vendor_id;
	}

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
