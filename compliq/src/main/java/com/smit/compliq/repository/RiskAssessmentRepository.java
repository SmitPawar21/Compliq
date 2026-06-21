package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.RiskAssessment;
import java.util.*;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
	Optional<RiskAssessment> findByDocument(Document doc);
}
