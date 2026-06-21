package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.ClauseAnalysis;
import com.smit.compliq.entity.Document;

import java.util.*;

public interface ClauseAnalysisRepository extends JpaRepository<ClauseAnalysis, Long> {
	Optional<ClauseAnalysis> findByDocument(Document doc);
}
