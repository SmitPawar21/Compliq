package com.smit.compliq.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiagnosticContextHolder {

    private static final ThreadLocal<DiagnosticLog> contextHolder = ThreadLocal.withInitial(DiagnosticLog::new);

    public static class DiagnosticLog {
        private String requestId = UUID.randomUUID().toString();
        private long totalRetrievalTimeMs = 0;
        private int totalRetrievedChunks = 0;
        private long totalLlmTimeMs = 0;
        private long totalPromptTokens = 0;
        private long totalGenerationTokens = 0;
        private List<String> operations = new ArrayList<>();

        public void addRetrieval(long timeMs, int chunks) {
            this.totalRetrievalTimeMs += timeMs;
            this.totalRetrievedChunks += chunks;
            this.operations.add(String.format("Retrieval: %d ms, %d chunks", timeMs, chunks));
        }

        public void addLlmCall(long timeMs, long promptTokens, long genTokens) {
            this.totalLlmTimeMs += timeMs;
            this.totalPromptTokens += promptTokens;
            this.totalGenerationTokens += genTokens;
            this.operations.add(String.format("LLM: %d ms, Input: %d, Output: %d", timeMs, promptTokens, genTokens));
        }

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n--- AI Workflow Diagnostic Log ---\n");
            sb.append("Request ID: ").append(requestId).append("\n");
            for (String op : operations) {
                sb.append(" -> ").append(op).append("\n");
            }
            sb.append("Total Retrieval Time: ").append(totalRetrievalTimeMs).append(" ms\n");
            sb.append("Total Retrieved Chunks: ").append(totalRetrievedChunks).append("\n");
            sb.append("Total LLM Time: ").append(totalLlmTimeMs).append(" ms\n");
            sb.append("Total Tokens: Input ").append(totalPromptTokens)
              .append(" / Output ").append(totalGenerationTokens).append("\n");
            sb.append("----------------------------------");
            return sb.toString();
        }
    }

    public static DiagnosticLog getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}
