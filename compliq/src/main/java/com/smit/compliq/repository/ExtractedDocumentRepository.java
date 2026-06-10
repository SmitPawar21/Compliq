package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.ExtractedDocument;

public interface ExtractedDocumentRepository extends JpaRepository<ExtractedDocument, Long> {
	ExtractedDocument findByDocumentId(long doc_id);
}
