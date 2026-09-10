package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smit.compliq.entity.ChatMessage;
import com.smit.compliq.entity.ChatSession;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	List<ChatMessage> findByChatSessionOrderByCreatedAtAsc(ChatSession chatSession);
}
