package com.smit.compliq.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.compliq.dto.chatbot.ToolCallDTO;
import com.smit.compliq.entity.ChatSession;
import com.smit.compliq.entity.ChatbotTrace;
import com.smit.compliq.entity.User;
import com.smit.compliq.repository.ChatbotTraceRepository;

import lombok.RequiredArgsConstructor;

/**
 * Creates and persists execution traces for every chatbot interaction.
 * Includes cost calculation, secret sanitization, and comprehensive metric tracking.
 */
@Service
@RequiredArgsConstructor
public class ChatbotTracingService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotTracingService.class);

    // Gemini 2.5 Flash pricing (free plan: costs are tracked but $0 for billing)
    private static final double INPUT_COST_PER_TOKEN = 0.00000015;   // $0.15 per 1M tokens
    private static final double OUTPUT_COST_PER_TOKEN = 0.0000006;   // $0.60 per 1M tokens

    private final ChatbotTraceRepository traceRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini-model:gemini-2.5-flash}")
    private String modelName;

    /**
     * Build a trace object at the start of a request.
     */
    public ChatbotTrace startTrace(User user, ChatSession session) {
        ChatbotTrace trace = new ChatbotTrace();
        trace.setRequestId(UUID.randomUUID().toString());
        trace.setUser(user);
        trace.setSession(session);
        trace.setModel(modelName);
        trace.setPromptVersion(com.smit.compliq.prompts.ChatbotPrompts.PROMPT_VERSION);
        trace.setCreatedAt(new Date());
        return trace;
    }

    /**
     * Finalize and persist the trace after the request completes.
     */
    public void finalizeTrace(ChatbotTrace trace, List<ToolCallDTO> toolCalls,
                               List<String> retrievedDocIds, long latencyMs,
                               long promptTokens, long completionTokens,
                               List<String> failures, String finalResponse) {
        try {
            trace.setLatencyMs(latencyMs);
            trace.setPromptTokens(promptTokens);
            trace.setCompletionTokens(completionTokens);

            // Calculate cost
            double cost = (promptTokens * INPUT_COST_PER_TOKEN) + (completionTokens * OUTPUT_COST_PER_TOKEN);
            trace.setTotalCost(cost);

            // Serialize tool calls (sanitized)
            if (toolCalls != null && !toolCalls.isEmpty()) {
                trace.setToolCalls(sanitize(objectMapper.writeValueAsString(toolCalls)));
            }

            // Serialize retrieved doc IDs
            if (retrievedDocIds != null && !retrievedDocIds.isEmpty()) {
                trace.setRetrievedDocumentIds(objectMapper.writeValueAsString(retrievedDocIds));
            }

            // Serialize failures
            if (failures != null && !failures.isEmpty()) {
                trace.setFailures(sanitize(objectMapper.writeValueAsString(failures)));
            }

            // Truncate final response preview
            if (finalResponse != null) {
                String preview = finalResponse.length() > 500
                    ? finalResponse.substring(0, 500)
                    : finalResponse;
                trace.setFinalResponsePreview(sanitize(preview));
            }

            traceRepository.save(trace);

            // Log a clean summary (no secrets)
            log.info("Chatbot Trace | RequestID: {} | Session: {} | User: {} | Latency: {}ms | Tokens: {}/{} | Cost: ${} | Tools: {} | Failures: {}",
                trace.getRequestId(),
                trace.getSession() != null ? trace.getSession().getSessionId() : "N/A",
                trace.getUser().getId(),
                latencyMs,
                promptTokens, completionTokens,
                String.format("%.6f", cost),
                toolCalls != null ? toolCalls.size() : 0,
                failures != null ? failures.size() : 0);

        } catch (Exception e) {
            log.error("Failed to persist chatbot trace: {}", e.getMessage());
        }
    }

    /**
     * Remove secrets, tokens, and sensitive data from trace strings.
     */
    private String sanitize(String input) {
        if (input == null) return null;
        // Redact JWT tokens
        input = input.replaceAll("eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}", "[REDACTED_JWT]");
        // Redact API keys
        input = input.replaceAll("(?i)(api[_-]?key|secret|password|aws[_-]?access)\\s*[:=]\\s*\"?[^\"\\s,}]+", "$1=[REDACTED]");
        // Redact S3 paths
        input = input.replaceAll("s3://[^\\s\"]+", "[REDACTED_S3]");
        return input;
    }
}
