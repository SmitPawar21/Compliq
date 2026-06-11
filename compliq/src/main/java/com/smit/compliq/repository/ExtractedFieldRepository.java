package com.smit.compliq.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.ExtractedField;

public interface ExtractedFieldRepository extends JpaRepository<ExtractedField, Long>  {
	List<ExtractedField> findByDocument_DocId(long doc_id);
}
