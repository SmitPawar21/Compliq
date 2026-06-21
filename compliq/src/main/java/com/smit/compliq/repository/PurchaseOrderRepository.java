package com.smit.compliq.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.PurchaseOrder;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String>{
	Optional<PurchaseOrder> findByDocument_DocId(long poDocId);
}
