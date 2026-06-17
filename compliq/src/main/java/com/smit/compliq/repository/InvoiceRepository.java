package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

}
