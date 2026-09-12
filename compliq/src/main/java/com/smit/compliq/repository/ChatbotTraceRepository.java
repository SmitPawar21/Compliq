package com.smit.compliq.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smit.compliq.entity.ChatbotTrace;
import com.smit.compliq.entity.User;

@Repository
public interface ChatbotTraceRepository extends JpaRepository<ChatbotTrace, Long> {

    List<ChatbotTrace> findBySessionSessionIdOrderByCreatedAtDesc(long sessionId);

    List<ChatbotTrace> findByUserOrderByCreatedAtDesc(User user);

    ChatbotTrace findByRequestId(String requestId);
}
