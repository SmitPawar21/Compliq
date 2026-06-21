package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

import com.smit.compliq.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
	Optional<Invoice> findByDocument_DocId(long invoiceDocId);
}
