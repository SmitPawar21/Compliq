package com.smit.compliq.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.compliq.entity.ChatbotEvaluation;
import com.smit.compliq.entity.ChatbotTrace;
import com.smit.compliq.entity.User;
import com.smit.compliq.repository.ChatbotEvaluationRepository;
import com.smit.compliq.repository.ChatbotTraceRepository;

import lombok.RequiredArgsConstructor;

/**
 * Custom evaluation service that uses the LLM (Gemini) as a judge (LLM-as-a-judge pattern)
 * to evaluate the chatbot's answers for groundedness, answer correctness, and hallucination.
 */
@Service
@RequiredArgsConstructor
public class ChatbotEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotEvaluationService.class);

    private final AIService aiService;
    private final ChatbotEvaluationRepository evaluationRepository;
    private final ChatbotTraceRepository traceRepository;
    private final ObjectMapper objectMapper;

    private static final String EVALUATION_PROMPT = """
        You are an expert AI evaluator. You are evaluating a compliance chatbot's response.
        Given the original prompt/request, the context (retrieved chunks), the tool calls made, and the final response,
        provide a JSON evaluation with the following integer scores (0-10) and a boolean for hallucination, along with a rationale.
        
        {
          "retrievalQualityScore": 0,
          "groundednessScore": 0,
          "answerCorrectnessScore": 0,
          "toolSelectionScore": 0,
          "hallucinationDetected": false,
          "rationale": "Explanation for the scores..."
        }

        Definitions:
        - retrievalQualityScore (0-10): Did the retrieved context contain the information needed to answer the question? (Rate 5 if no context was retrieved but the question didn't need it. Rate 0 if needed but not retrieved).
        - groundednessScore (0-10): Is the final response fully supported by the retrieved context? (Rate 10 if fully supported or if no context was needed and it correctly states it).
        - answerCorrectnessScore (0-10): Is the final answer correct and helpful for the user's request?
        - toolSelectionScore (0-10): Did the agent select the right tools for the job?
        - hallucinationDetected (boolean): Did the agent make up facts not present in the context?

        Return ONLY valid JSON.
        
        --- INPUT DATA ---
        Request ID: %s
        User Message / Prompt Version: %s
        Retrieved Document IDs: %s
        Tool Calls: %s
        Final Response: %s
        """;

    /**
     * Trigger an evaluation for a specific trace.
     */
    @Transactional
    public ChatbotEvaluation evaluateTrace(long traceId, User requestingUser) {
        Optional<ChatbotTrace> traceOpt = traceRepository.findById(traceId);
        
        if (traceOpt.isEmpty()) {
            throw new IllegalArgumentException("Trace not found: " + traceId);
        }
        
        ChatbotTrace trace = traceOpt.get();
        
        // Data isolation check: Ensure the user evaluating owns the trace
        if (trace.getUser().getId() != requestingUser.getId()) {
            throw new SecurityException("You do not have permission to evaluate this trace.");
        }
        
        // Check if already evaluated
        ChatbotEvaluation existingEval = evaluationRepository.findByTraceTraceId(traceId);
        if (existingEval != null) {
            return existingEval;
        }

        try {
            // Build the prompt
            String prompt = String.format(EVALUATION_PROMPT, 
                trace.getRequestId(),
                trace.getPromptVersion(),
                trace.getRetrievedDocumentIds() != null ? trace.getRetrievedDocumentIds() : "None",
                trace.getToolCalls() != null ? trace.getToolCalls() : "None",
                trace.getFinalResponsePreview() != null ? trace.getFinalResponsePreview() : "None"
            );

            // Call the LLM Judge
            String response = aiService.generateResponse(prompt);
            
            // Clean markdown
            if (response.startsWith("```json")) response = response.substring(7);
            if (response.startsWith("```")) response = response.substring(3);
            if (response.endsWith("```")) response = response.substring(0, response.length() - 3);
            response = response.trim();

            JsonNode json = objectMapper.readTree(response);

            ChatbotEvaluation evaluation = new ChatbotEvaluation();
            evaluation.setTrace(trace);
            evaluation.setUser(requestingUser);
            evaluation.setRetrievalQualityScore(json.path("retrievalQualityScore").asInt(0));
            evaluation.setGroundednessScore(json.path("groundednessScore").asInt(0));
            evaluation.setAnswerCorrectnessScore(json.path("answerCorrectnessScore").asInt(0));
            evaluation.setToolSelectionScore(json.path("toolSelectionScore").asInt(0));
            evaluation.setHallucinationDetected(json.path("hallucinationDetected").asBoolean(true));
            evaluation.setRationale(json.path("rationale").asText("No rationale provided."));
            evaluation.setEvaluatedAt(new Date());

            return evaluationRepository.save(evaluation);
            
        } catch (Exception e) {
            log.error("Failed to evaluate trace {}: {}", traceId, e.getMessage());
            throw new RuntimeException("Evaluation failed: " + e.getMessage());
        }
    }
    
    /**
     * Get evaluations for a specific user.
     */
    public List<ChatbotEvaluation> getUserEvaluations(User user) {
        return evaluationRepository.findByUserOrderByEvaluatedAtDesc(user);
    }
}
