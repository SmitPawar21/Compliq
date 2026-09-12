package com.smit.compliq.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smit.compliq.entity.ApprovalAuditLog;
import com.smit.compliq.entity.User;

@Repository
public interface ApprovalAuditLogRepository extends JpaRepository<ApprovalAuditLog, Long> {

    Optional<ApprovalAuditLog> findByApprovalToken(String approvalToken);

    List<ApprovalAuditLog> findByUserOrderByCreatedAtDesc(User user);
}
