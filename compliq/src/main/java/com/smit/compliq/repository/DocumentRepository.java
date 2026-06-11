package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
