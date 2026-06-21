package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

import com.smit.compliq.entity.ContractSummary;
import com.smit.compliq.entity.Document;

public interface ContractSummaryRepository extends JpaRepository<ContractSummary, Long> {
	Optional<ContractSummary> findByDocument(Document document);
}
