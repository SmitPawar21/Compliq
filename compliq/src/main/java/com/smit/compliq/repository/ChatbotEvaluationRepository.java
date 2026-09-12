package com.smit.compliq.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smit.compliq.entity.ChatbotEvaluation;
import com.smit.compliq.entity.User;

@Repository
public interface ChatbotEvaluationRepository extends JpaRepository<ChatbotEvaluation, Long> {

    ChatbotEvaluation findByTraceTraceId(long traceId);

    List<ChatbotEvaluation> findByUserOrderByEvaluatedAtDesc(User user);
}
