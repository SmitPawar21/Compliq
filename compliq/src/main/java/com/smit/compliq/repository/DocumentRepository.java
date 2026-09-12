package com.smit.compliq.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.User;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUploadedBy(User user);

    Optional<Document> findByDocIdAndUploadedBy(long docId, User user);
}
