package com.smit.compliq.service.tools;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.smit.compliq.dto.chatbot.ToolCallDTO;
import com.smit.compliq.entity.User;
import com.smit.compliq.enums.ChatToolName;

import lombok.RequiredArgsConstructor;

/**
 * Central tool dispatcher for the chatbot agent.
 * Routes tool calls to the correct service, wraps each in try/catch for failure recovery,
 * records timing and success/failure, and sanitizes outputs.
 */
@Service
@RequiredArgsConstructor
public class ToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutor.class);

    private final DocumentSearchTool documentSearchTool;
    private final ChunkRetrievalTool chunkRetrievalTool;
    private final DocumentMetadataTool documentMetadataTool;
    private final MemoryTool memoryTool;
    private final ComplianceReportTool complianceReportTool;

    /**
     * Execute a tool call and return a structured ToolCallDTO result.
     * All failures are caught and recorded — the agent loop never crashes from a tool error.
     *
     * @param user The authenticated user (authorization boundary)
     * @param toolName The tool to execute
     * @param arguments Tool-specific arguments
     * @return ToolCallDTO with result or error
     */
    public ToolCallDTO executeTool(User user, ChatToolName toolName, Map<String, Object> arguments) {
        StopWatch sw = new StopWatch();
        sw.start(toolName.name());

        try {
            String result = switch (toolName) {
                case SEARCH_DOCUMENTS -> {
                    String query = getStringArg(arguments, "query", "");
                    yield documentSearchTool.searchDocuments(user, query);
                }
                case RETRIEVE_CHUNKS -> {
                    String query = getStringArg(arguments, "query", "");
                    String docType = getStringArg(arguments, "documentType", null);
                    yield chunkRetrievalTool.retrieveChunks(user, query, docType);
                }
                case INSPECT_METADATA -> {
                    long docId = getLongArg(arguments, "documentId");
                    yield documentMetadataTool.inspectMetadata(user, docId);
                }
                case RECALL_MEMORIES -> {
                    String query = getStringArg(arguments, "query", "");
                    yield memoryTool.recallMemories(user, query);
                }
                case STORE_MEMORY -> {
                    String key = getStringArg(arguments, "key", "");
                    String value = getStringArg(arguments, "value", "");
                    String category = getStringArg(arguments, "category", "FACT");
                    yield memoryTool.storeMemory(user, key, value, category);
                }
                case RUN_COMPLIANCE_REPORT -> {
                    // This tool returns a special result — handled by the agent service
                    long contractId = getLongArg(arguments, "contractDocId");
                    long invoiceId = getLongArg(arguments, "invoiceDocId");
                    long poId = getLongArg(arguments, "poDocId");
                    ComplianceReportTool.ReportToolResult reportResult =
                        complianceReportTool.requestReport(user, contractId, invoiceId, poId);
                    yield "REQUIRES_APPROVAL: " + reportResult.getDescription()
                        + "\n[APPROVAL_TOKEN:" + reportResult.getApprovalToken() + "]";
                }
            };

            sw.stop();
            String sanitizedResult = sanitizeOutput(result);
            log.debug("Tool {} completed in {}ms", toolName, sw.getTotalTimeMillis());
            return ToolCallDTO.success(toolName, sanitizeArguments(arguments), sanitizedResult, sw.getTotalTimeMillis());

        } catch (Exception e) {
            if (sw.isRunning()) sw.stop();
            log.error("Tool {} failed: {}", toolName, e.getMessage());
            return ToolCallDTO.failure(toolName, sanitizeArguments(arguments),
                "Tool execution failed: " + e.getMessage(), sw.getTotalTimeMillis());
        }
    }

    /**
     * Execute a compliance report after approval.
     */
    public String executeApprovedReport(User user, long contractDocId, long invoiceDocId, long poDocId) {
        return complianceReportTool.executeReport(user, contractDocId, invoiceDocId, poDocId);
    }

    private String getStringArg(Map<String, Object> args, String key, String defaultValue) {
        Object val = args.get(key);
        if (val == null) return defaultValue;
        String strVal = val.toString();
        if (strVal.length() > 500) {
            throw new IllegalArgumentException("Argument '" + key + "' exceeds maximum length of 500 characters.");
        }
        return strVal;
    }

    private long getLongArg(Map<String, Object> args, String key) {
        Object val = args.get(key);
        if (val == null) throw new IllegalArgumentException("Missing required argument: " + key);
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    /**
     * Remove any secrets or sensitive data patterns from tool output.
     */
    private String sanitizeOutput(String output) {
        if (output == null) return null;
        // Redact JWT-like tokens
        output = output.replaceAll("eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}", "[REDACTED_TOKEN]");
        // Redact API key patterns
        output = output.replaceAll("(?i)(api[_-]?key|secret|password)\\s*[:=]\\s*\\S+", "$1=[REDACTED]");
        // Redact S3 keys
        output = output.replaceAll("s3://[^\\s]+", "[REDACTED_S3_PATH]");
        return output;
    }

    /**
     * Sanitize arguments map before recording in trace.
     */
    private Map<String, Object> sanitizeArguments(Map<String, Object> args) {
        if (args == null) return java.util.Collections.emptyMap();
        Map<String, Object> sanitized = new java.util.HashMap<>();
        args.forEach((k, v) -> {
            if (v instanceof String) {
                sanitized.put(k, sanitizeOutput((String) v));
            } else {
                sanitized.put(k, v);
            }
        });
        return sanitized;
    }
}
