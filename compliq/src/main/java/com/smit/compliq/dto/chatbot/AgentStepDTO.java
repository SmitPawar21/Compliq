package com.smit.compliq.dto.chatbot;

public class AgentStepDTO {

    private int stepNumber;
    private String thought;
    private ToolCallDTO toolCall;
    private String observation;

    public AgentStepDTO() {}

    public AgentStepDTO(int stepNumber, String thought) {
        this.stepNumber = stepNumber;
        this.thought = thought;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public ToolCallDTO getToolCall() {
        return toolCall;
    }

    public void setToolCall(ToolCallDTO toolCall) {
        this.toolCall = toolCall;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}
