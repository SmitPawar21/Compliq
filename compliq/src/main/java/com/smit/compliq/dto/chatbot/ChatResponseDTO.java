package com.smit.compliq.dto.chatbot;

import java.util.List;

public class ChatResponseDTO {

    private long sessionId;
    private long messageId;
    private String response;
    private List<CitationDTO> citations;
    private boolean requiresApproval;
    private String approvalDescription;
    private String approvalToken;
    private String traceId;

    public ChatResponseDTO() {}

    public static ChatResponseDTO success(long sessionId, long messageId, String response,
                                          List<CitationDTO> citations, String traceId) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.sessionId = sessionId;
        dto.messageId = messageId;
        dto.response = response;
        dto.citations = citations;
        dto.requiresApproval = false;
        dto.traceId = traceId;
        return dto;
    }

    public static ChatResponseDTO approval(long sessionId, long messageId, String description,
                                           String approvalToken, String traceId) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.sessionId = sessionId;
        dto.messageId = messageId;
        dto.response = description;
        dto.requiresApproval = true;
        dto.approvalDescription = description;
        dto.approvalToken = approvalToken;
        dto.traceId = traceId;
        return dto;
    }

    public static ChatResponseDTO fallback(long sessionId, String traceId) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.sessionId = sessionId;
        dto.response = "I'm sorry, I encountered an issue processing your request. Please try again.";
        dto.requiresApproval = false;
        dto.traceId = traceId;
        return dto;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<CitationDTO> getCitations() {
        return citations;
    }

    public void setCitations(List<CitationDTO> citations) {
        this.citations = citations;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public String getApprovalDescription() {
        return approvalDescription;
    }

    public void setApprovalDescription(String approvalDescription) {
        this.approvalDescription = approvalDescription;
    }

    public String getApprovalToken() {
        return approvalToken;
    }

    public void setApprovalToken(String approvalToken) {
        this.approvalToken = approvalToken;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
