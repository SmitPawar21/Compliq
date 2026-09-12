package com.smit.compliq.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.smit.compliq.entity.ChatMessage;
import com.smit.compliq.entity.ChatSession;
import com.smit.compliq.entity.User;
import com.smit.compliq.entity.UserMemory;
import com.smit.compliq.repository.ChatMessageRepository;
import com.smit.compliq.repository.UserMemoryRepository;

import lombok.RequiredArgsConstructor;

/**
 * Manages the three layers of chatbot memory:
 * 1. Episodic Memory: Recent chat messages from the current session
 * 2. Session Memory: Maintained in-memory during the agent loop (not persisted here)
 * 3. Long-term Memory: UserMemory entities stored across sessions
 */
@Service
@RequiredArgsConstructor
public class MemoryService {

    private static final Logger log = LoggerFactory.getLogger(MemoryService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final UserMemoryRepository userMemoryRepository;

    /**
     * Retrieve episodic memory: recent messages from the current session.
     * Returns last 10 messages ordered by creation time.
     */
    public List<ChatMessage> getEpisodicMemory(ChatSession session) {
        if (session == null) return List.of();

        try {
            List<ChatMessage> messages = chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session);
            if (messages == null) return List.of();

            // Return last 10 messages
            int start = Math.max(0, messages.size() - 10);
            return messages.subList(start, messages.size());
        } catch (Exception e) {
            log.warn("Failed to retrieve episodic memory for session {}: {}", session.getSessionId(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Retrieve long-term memories for a user, optionally filtered by query.
     * Returns top 5 most relevant memories.
     */
    public List<UserMemory> getLongTermMemories(User user, String query) {
        try {
            List<UserMemory> memories;

            if (query == null || query.isBlank()) {
                memories = userMemoryRepository.findByUserOrderByUpdatedAtDesc(user);
            } else {
                memories = userMemoryRepository.searchByKeyword(user, query);
            }

            if (memories == null) return List.of();

            // Return top 5
            return memories.stream().limit(5).toList();
        } catch (Exception e) {
            log.warn("Failed to retrieve long-term memories for user {}: {}", user.getId(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Get all long-term memories for a user (for context building).
     */
    public List<UserMemory> getAllMemories(User user) {
        try {
            List<UserMemory> memories = userMemoryRepository.findByUserOrderByUpdatedAtDesc(user);
            return memories != null ? memories.stream().limit(10).toList() : List.of();
        } catch (Exception e) {
            log.warn("Failed to retrieve all memories for user {}: {}", user.getId(), e.getMessage());
            return List.of();
        }
    }
}
