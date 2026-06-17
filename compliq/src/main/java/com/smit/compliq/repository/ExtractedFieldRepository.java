package com.smit.compliq.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.ExtractedField;

public interface ExtractedFieldRepository extends JpaRepository<ExtractedField, Long>  {
	List<ExtractedField> findByDocument_DocId(long doc_id);
	Optional<ExtractedField>
	findByDocumentAndFieldName(Document document, String fieldName);
}
