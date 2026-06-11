package com.smit.compliq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.dto.VendorDTO;
import com.smit.compliq.entity.Vendor;
import com.smit.compliq.exception.VendorNotFoundException;
import com.smit.compliq.service.VendorService;

import java.util.*;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {
	
	private final VendorService vendorService;
	
	public VendorController(VendorService vendorService) {
		this.vendorService = vendorService;
	}
	
	@PostMapping("/")
	public ResponseEntity<?> postVendor(@RequestBody VendorDTO vendorDTO) {
		try {
			Vendor vendor = vendorService.postVendor(vendorDTO);
			
			return ResponseEntity.ok(vendor);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	
	@GetMapping("/")
	public ResponseEntity<?> getAllVendors() {
		try {
			List<Vendor> vendors = vendorService.getAllVendors();
			
			if(vendors.size()==0) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No Vendor present in Database");
			}
			
			return ResponseEntity.ok(vendors);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getOneVendor(@PathVariable long id) {
		try {
			Vendor vendor = vendorService.getOneVendor(id);
			
			if(vendor==null) throw new VendorNotFoundException("Vendor Not Found with id: "+id);
			
			return ResponseEntity.ok(vendor);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteVendor(@PathVariable long id) {
		try {
			Vendor vendor = vendorService.deleteVendor(id);
			
			return ResponseEntity.ok(vendor);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	
//	/api/users/123/status?status=APPROVED
	@PatchMapping("/{id}/status")
	public ResponseEntity<?> updateStatus(
				@PathVariable long id,
				@RequestParam String status
			) {
		try {
			Vendor vendor = vendorService.updateVendorStatus(id, status);
			
			return ResponseEntity.ok(vendor);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}
