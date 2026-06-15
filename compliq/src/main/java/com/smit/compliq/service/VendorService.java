package com.smit.compliq.service;

import org.springframework.stereotype.Service;

import com.smit.compliq.dto.VendorDTO;
import com.smit.compliq.entity.Vendor;
import com.smit.compliq.enums.VendorStatus;
import com.smit.compliq.exception.IncorrectVendorStatusException;
import com.smit.compliq.exception.VendorNotFoundException;
import com.smit.compliq.repository.VendorRepostiory;

import java.util.*;

@Service
public class VendorService {
	private final VendorRepostiory vendorRepository;
	
	public VendorService(VendorRepostiory vendorRepository) {
		this.vendorRepository = vendorRepository;
	}
	
	public Vendor postVendor(VendorDTO dto) {
		Vendor ven = new Vendor();
		
		ven.setEmail(dto.getEmail());
		ven.setGstNumber(dto.getGstNumber());
		ven.setPanNumber(dto.getPanNumber());
		ven.setPhone(dto.getPhone());
		ven.setVendorName(dto.getVendorName());
		ven.setVendorStatus(VendorStatus.ACTIVE);
		
		return vendorRepository.save(ven);
//		return ven;
	}
	
	public List<Vendor> getAllVendors() {
		return vendorRepository.findAll();
	}
	
	public Vendor getOneVendor(long vendor_id) {
		return vendorRepository.findById(vendor_id);
	}
	
	public Vendor deleteVendor(long vendor_id) {
		Vendor vendor = vendorRepository.findById(vendor_id);
		
		if(vendor==null) throw new VendorNotFoundException("Vendor Not Found with id: "+vendor_id);
		vendorRepository.delete(vendor);
		
		return vendor;
	}
	
	public Vendor updateVendorStatus(long vendor_id, String status) {
		Vendor vendor = vendorRepository.findById(vendor_id);
		
		if(vendor==null) throw new VendorNotFoundException("Vendor Not Found with id: "+vendor_id);
		
		if(status.equals("active") || status.equals("Active") || status.equals("ACTIVE")) {
			vendor.setVendorStatus(VendorStatus.ACTIVE);
		} else if(status.equals("inactive") || status.equals("Inactive") || status.equals("INACTIVE")) {
			vendor.setVendorStatus(VendorStatus.INACTIVE);
		} else {
			throw new IncorrectVendorStatusException("incorrect vendor status");
		}
		
		return vendorRepository.save(vendor);
	}
}
