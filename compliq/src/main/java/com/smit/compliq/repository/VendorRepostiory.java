package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.Vendor;

public interface VendorRepostiory extends JpaRepository<Vendor, Long> {
	Vendor findById(long vendor_id);
}
