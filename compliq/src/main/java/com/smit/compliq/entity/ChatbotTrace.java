package com.smit.compliq.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "chatbot_trace", indexes = {
    @Index(name = "idx_trace_session", columnList = "session_id"),
    @Index(name = "idx_trace_user", columnList = "user_id"),
    @Index(name = "idx_trace_request", columnList = "requestId")
})
public class ChatbotTrace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long traceId;

    @Column(nullable = false, length = 64)
    private String requestId;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private ChatSession session;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 64)
    private String model;

    @Column(length = 32)
    private String promptVersion;

    @Column(columnDefinition = "TEXT")
    private String retrievedDocumentIds;

    @Column(columnDefinition = "TEXT")
    private String rerankResults;

    @Column(columnDefinition = "TEXT")
    private String toolCalls;

    @Column
    private long latencyMs;

    @Column
    private long promptTokens;

    @Column
    private long completionTokens;

    @Column
    private double totalCost;

    @Column(columnDefinition = "TEXT")
    private String failures;

    @Column(length = 500)
    private String finalResponsePreview;

    @Column
    private int retryCount;

    @Column(length = 100)
    private String recoveryState;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    public ChatbotTrace() {}

    public ChatbotTrace(String requestId, User user, String model, String promptVersion) {
        this.requestId = requestId;
        this.user = user;
        this.model = model;
        this.promptVersion = promptVersion;
        this.createdAt = new Date();
    }

    public long getTraceId() {
        return traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession session) {
        this.session = session;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public String getRetrievedDocumentIds() {
        return retrievedDocumentIds;
    }

    public void setRetrievedDocumentIds(String retrievedDocumentIds) {
        this.retrievedDocumentIds = retrievedDocumentIds;
    }

    public String getRerankResults() {
        return rerankResults;
    }

    public void setRerankResults(String rerankResults) {
        this.rerankResults = rerankResults;
    }

    public String getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(String toolCalls) {
        this.toolCalls = toolCalls;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public long getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(long promptTokens) {
        this.promptTokens = promptTokens;
    }

    public long getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(long completionTokens) {
        this.completionTokens = completionTokens;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getFailures() {
        return failures;
    }

    public void setFailures(String failures) {
        this.failures = failures;
    }

    public String getFinalResponsePreview() {
        return finalResponsePreview;
    }

    public void setFinalResponsePreview(String finalResponsePreview) {
        this.finalResponsePreview = finalResponsePreview;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getRecoveryState() {
        return recoveryState;
    }

    public void setRecoveryState(String recoveryState) {
        this.recoveryState = recoveryState;
    }
}
