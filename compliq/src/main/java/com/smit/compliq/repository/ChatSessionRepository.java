package com.smit.compliq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smit.compliq.entity.ChatSession;
import com.smit.compliq.entity.User;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
	List<ChatSession> findByUser(User user);
	List<ChatSession> findByUserOrderByUpdatedAtDesc(User user);
	java.util.Optional<ChatSession> findBySessionIdAndUser(long sessionId, User user);
}
