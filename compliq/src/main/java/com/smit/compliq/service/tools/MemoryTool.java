package com.smit.compliq.service.tools;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smit.compliq.entity.User;
import com.smit.compliq.entity.UserMemory;
import com.smit.compliq.enums.MemoryCategory;
import com.smit.compliq.repository.UserMemoryRepository;

import lombok.RequiredArgsConstructor;

/**
 * Tool: RECALL_MEMORIES / STORE_MEMORY
 * Manages user long-term memories with creation policies and sensitive data filtering.
 * Authorization: all queries are scoped to the authenticated user.
 */
@Service
@RequiredArgsConstructor
public class MemoryTool {

    private static final Logger log = LoggerFactory.getLogger(MemoryTool.class);

    private static final int MAX_MEMORIES_PER_USER = 50;
    private static final int MAX_MEMORY_VALUE_LENGTH = 500;

    // Patterns for sensitive data that should never be stored
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b");
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|passwd|pwd)\\s*[:=]\\s*\\S+");

    private final UserMemoryRepository memoryRepository;

    /**
     * Recall memories matching a query keyword for the given user.
     */
    public String recallMemories(User user, String query) {
        List<UserMemory> memories;

        if (query == null || query.isBlank()) {
            memories = memoryRepository.findByUserOrderByUpdatedAtDesc(user);
        } else {
            memories = memoryRepository.searchByKeyword(user, query);
        }

        if (memories == null || memories.isEmpty()) {
            return "No relevant memories found.";
        }

        // Limit to top 5 most relevant
        List<UserMemory> topMemories = memories.stream().limit(5).toList();

        StringBuilder sb = new StringBuilder();
        sb.append("User memories:\n");
        for (UserMemory mem : topMemories) {
            sb.append("- [").append(mem.getCategory()).append("] ")
              .append(mem.getMemoryKey()).append(": ")
              .append(mem.getMemoryValue()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Store a new memory or update an existing one for the user.
     * Enforces: max 50 per user, max 500 chars, no sensitive data.
     */
    @Transactional
    public String storeMemory(User user, String key, String value, String categoryStr) {
        // Parse category
        MemoryCategory category;
        try {
            category = MemoryCategory.valueOf(categoryStr.toUpperCase());
        } catch (Exception e) {
            category = MemoryCategory.FACT;
        }

        // Validate key
        if (key == null || key.isBlank()) {
            return "Memory key cannot be empty.";
        }
        if (key.length() > 255) {
            key = key.substring(0, 255);
        }

        // Validate and sanitize value
        if (value == null || value.isBlank()) {
            return "Memory value cannot be empty.";
        }
        if (containsSensitiveData(value)) {
            return "Cannot store memory: the value appears to contain sensitive data (credit card, SSN, or password).";
        }
        if (value.length() > MAX_MEMORY_VALUE_LENGTH) {
            value = value.substring(0, MAX_MEMORY_VALUE_LENGTH);
        }

        // Check for existing memory with same key — update instead of creating duplicate
        Optional<UserMemory> existing = memoryRepository.findByUserAndMemoryKey(user, key);
        if (existing.isPresent()) {
            UserMemory mem = existing.get();
            mem.setMemoryValue(value);
            mem.setCategory(category);
            mem.setUpdatedAt(new Date());
            memoryRepository.save(mem);
            return "Memory updated: " + key;
        }

        // Enforce max memories per user — evict oldest if at capacity
        long count = memoryRepository.countByUser(user);
        if (count >= MAX_MEMORIES_PER_USER) {
            memoryRepository.deleteOldestByUser(user);
        }

        // Create new memory
        UserMemory memory = new UserMemory(user, key, value, category);
        memoryRepository.save(memory);
        return "Memory stored: " + key;
    }

    private boolean containsSensitiveData(String text) {
        return CREDIT_CARD_PATTERN.matcher(text).find()
            || SSN_PATTERN.matcher(text).find()
            || PASSWORD_PATTERN.matcher(text).find();
    }
}
