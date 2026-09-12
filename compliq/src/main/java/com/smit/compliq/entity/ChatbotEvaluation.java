package com.smit.compliq.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "chatbot_evaluation")
public class ChatbotEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long evaluationId;

    @OneToOne
    @JoinColumn(name = "trace_id", nullable = false, unique = true)
    private ChatbotTrace trace;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int retrievalQualityScore;

    @Column(nullable = false)
    private int groundednessScore;

    @Column(nullable = false)
    private int answerCorrectnessScore;

    @Column(nullable = false)
    private int toolSelectionScore;

    @Column(nullable = false)
    private boolean hallucinationDetected;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date evaluatedAt;

    public ChatbotEvaluation() {}

    public long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public ChatbotTrace getTrace() {
        return trace;
    }

    public void setTrace(ChatbotTrace trace) {
        this.trace = trace;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getRetrievalQualityScore() {
        return retrievalQualityScore;
    }

    public void setRetrievalQualityScore(int retrievalQualityScore) {
        this.retrievalQualityScore = retrievalQualityScore;
    }

    public int getGroundednessScore() {
        return groundednessScore;
    }

    public void setGroundednessScore(int groundednessScore) {
        this.groundednessScore = groundednessScore;
    }

    public int getAnswerCorrectnessScore() {
        return answerCorrectnessScore;
    }

    public void setAnswerCorrectnessScore(int answerCorrectnessScore) {
        this.answerCorrectnessScore = answerCorrectnessScore;
    }

    public int getToolSelectionScore() {
        return toolSelectionScore;
    }

    public void setToolSelectionScore(int toolSelectionScore) {
        this.toolSelectionScore = toolSelectionScore;
    }

    public boolean isHallucinationDetected() {
        return hallucinationDetected;
    }

    public void setHallucinationDetected(boolean hallucinationDetected) {
        this.hallucinationDetected = hallucinationDetected;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public Date getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Date evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
