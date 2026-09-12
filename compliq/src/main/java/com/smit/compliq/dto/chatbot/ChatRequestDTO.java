package com.smit.compliq.dto.chatbot;

import jakarta.validation.constraints.NotBlank;

public class ChatRequestDTO {

    private Long sessionId;

    @NotBlank(message = "Message cannot be empty")
    private String message;

    private String approvalToken;

    public ChatRequestDTO() {}

    public ChatRequestDTO(String message) {
        this.message = message;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getApprovalToken() {
        return approvalToken;
    }

    public void setApprovalToken(String approvalToken) {
        this.approvalToken = approvalToken;
    }
}
