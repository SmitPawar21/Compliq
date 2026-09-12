package com.smit.compliq.dto.chatbot;

import java.util.Map;

import com.smit.compliq.enums.ChatToolName;

public class ToolCallDTO {

    private ChatToolName toolName;
    private Map<String, Object> arguments;
    private String result;
    private long latencyMs;
    private boolean success;
    private String errorMessage;

    public ToolCallDTO() {}

    public static ToolCallDTO success(ChatToolName toolName, Map<String, Object> arguments,
                                       String result, long latencyMs) {
        ToolCallDTO dto = new ToolCallDTO();
        dto.toolName = toolName;
        dto.arguments = arguments;
        dto.result = truncate(result, 2000);
        dto.latencyMs = latencyMs;
        dto.success = true;
        return dto;
    }

    public static ToolCallDTO failure(ChatToolName toolName, Map<String, Object> arguments,
                                       String errorMessage, long latencyMs) {
        ToolCallDTO dto = new ToolCallDTO();
        dto.toolName = toolName;
        dto.arguments = arguments;
        dto.latencyMs = latencyMs;
        dto.success = false;
        dto.errorMessage = errorMessage;
        return dto;
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }

    public ChatToolName getToolName() {
        return toolName;
    }

    public void setToolName(ChatToolName toolName) {
        this.toolName = toolName;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
