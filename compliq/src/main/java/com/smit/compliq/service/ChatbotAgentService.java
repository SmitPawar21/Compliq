package com.smit.compliq.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.compliq.dto.chatbot.ChatRequestDTO;
import com.smit.compliq.dto.chatbot.ChatResponseDTO;
import com.smit.compliq.dto.chatbot.CitationDTO;
import com.smit.compliq.dto.chatbot.ToolCallDTO;
import com.smit.compliq.entity.ChatMessage;
import com.smit.compliq.entity.ChatSession;
import com.smit.compliq.entity.ChatbotTrace;
import com.smit.compliq.entity.User;
import com.smit.compliq.entity.UserMemory;
import com.smit.compliq.enums.ChatToolName;
import com.smit.compliq.enums.MessageRole;
import com.smit.compliq.prompts.ChatbotPrompts;
import com.smit.compliq.repository.ChatMessageRepository;
import com.smit.compliq.repository.ChatSessionRepository;
import com.smit.compliq.service.tools.ChunkRetrievalTool;
import com.smit.compliq.service.tools.ToolExecutor;

import lombok.RequiredArgsConstructor;

/**
 * The core agentic chatbot orchestrator implementing a ReAct-style loop:
 * 1. Receive user message + session context
 * 2. Query understanding → extract intent
 * 3. Build system prompt with tools, conversation history, memories
 * 4. Agent loop (max 5 iterations): LLM → parse tool calls → execute → observe → repeat
 * 5. Extract citations, persist messages/trace, return structured response
 *
 * Failure recovery is built into every step with safe fallback responses.
 */
@Service
@RequiredArgsConstructor
public class ChatbotAgentService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotAgentService.class);

    private static final int MAX_AGENT_ITERATIONS = 5;
    private static final int MAX_LLM_RETRIES = 2;
    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
        "```tool_call\\s*\\n?\\s*(\\{.*?})\\s*\\n?\\s*```", Pattern.DOTALL);
    private static final Pattern APPROVAL_TOKEN_PATTERN = Pattern.compile(
        "\\[APPROVAL_TOKEN:([a-f0-9-]+)]");

    private final AIService aiService;
    private final MemoryService memoryService;
    private final ChatbotTracingService tracingService;
    private final ContextAssembler contextAssembler;
    private final QueryUnderstandingService queryUnderstandingService;
    private final RerankingService rerankingService;
    private final ChunkRetrievalTool chunkRetrievalTool;
    private final ToolExecutor toolExecutor;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    /**
     * Main entry point: process a user chat message and return a response.
     */
    public ChatResponseDTO processMessage(User user, ChatRequestDTO request) {
        StopWatch totalTimer = new StopWatch();
        totalTimer.start("ChatbotRequest");

        // Tracking variables for tracing
        List<ToolCallDTO> allToolCalls = new ArrayList<>();
        List<String> retrievedDocIds = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        long totalPromptTokens = 0;
        long totalCompletionTokens = 0;

        ChatSession session = null;
        ChatbotTrace trace = null;

        try {
            // Step 1: Resolve or create session
            session = resolveSession(user, request);
            trace = tracingService.startTrace(user, session);

            // Step 2: Check for approval flow
            if (request.getApprovalToken() != null && !request.getApprovalToken().isBlank()) {
                return handleApproval(user, session, request.getApprovalToken(), trace, totalTimer);
            }

            // Step 3: Persist user message
            ChatMessage userMessage = persistMessage(session, MessageRole.USER, request.getMessage());

            // Step 4: Query understanding
            QueryUnderstandingService.QueryAnalysis queryAnalysis;
            try {
                queryAnalysis = queryUnderstandingService.analyzeQuery(request.getMessage());
            } catch (Exception e) {
                log.warn("Query understanding failed: {}", e.getMessage());
                failures.add("Query understanding failed: " + e.getMessage());
                queryAnalysis = QueryUnderstandingService.QueryAnalysis.fallback(request.getMessage());
            }

            // Step 5: Retrieve conversation history + memories
            List<ChatMessage> conversationHistory = memoryService.getEpisodicMemory(session);
            List<UserMemory> memories = memoryService.getAllMemories(user);

            // Step 6: Retrieve relevant document chunks (for context)
            List<org.springframework.ai.document.Document> chunks = List.of();
            if (!"GENERAL".equals(queryAnalysis.getIntent())) {
                try {
                    chunks = chunkRetrievalTool.retrieveRawChunks(user, queryAnalysis.getExpandedQuery());
                    // Rerank
                    chunks = rerankingService.rerank(queryAnalysis.getExpandedQuery(), chunks);
                    // Track retrieved doc IDs
                    for (var chunk : chunks) {
                        Object docId = chunk.getMetadata().get("documentId");
                        if (docId != null) retrievedDocIds.add(docId.toString());
                    }
                } catch (Exception e) {
                    log.warn("Retrieval failed: {}", e.getMessage());
                    failures.add("Retrieval failed: " + e.getMessage());
                }
            }

            // Step 7: Assemble full context
            String fullContext = contextAssembler.assembleChatContext(chunks, memories, conversationHistory);

            // Step 8: Build initial prompt
            String systemPrompt = ChatbotPrompts.SYSTEM_PROMPT;
            StringBuilder conversationPrompt = new StringBuilder();
            conversationPrompt.append(fullContext).append("\n\n");
            conversationPrompt.append("User's question: ").append(request.getMessage());

            // Step 9: Agent loop
            String finalAnswer = null;
            String pendingApprovalDesc = null;
            String pendingApprovalToken = null;

            for (int iteration = 0; iteration < MAX_AGENT_ITERATIONS; iteration++) {
                // Call LLM
                String llmResponse;
                try {
                    llmResponse = callLlmWithRetry(systemPrompt + "\n\n" + conversationPrompt.toString());
                } catch (Exception e) {
                    log.error("LLM call failed after retries: {}", e.getMessage());
                    failures.add("LLM failure: " + e.getMessage());
                    break; // Fall through to fallback
                }

                // Check for tool calls
                Matcher toolMatcher = TOOL_CALL_PATTERN.matcher(llmResponse);
                if (toolMatcher.find()) {
                    String toolCallJson = toolMatcher.group(1);

                    try {
                        JsonNode toolNode = objectMapper.readTree(toolCallJson);
                        String toolName = toolNode.get("tool").asText();
                        ChatToolName tool = ChatToolName.valueOf(toolName);

                        Map<String, Object> arguments = new HashMap<>();
                        JsonNode argsNode = toolNode.get("arguments");
                        if (argsNode != null) {
                            argsNode.fields().forEachRemaining(entry ->
                                arguments.put(entry.getKey(), parseJsonValue(entry.getValue())));
                        }

                        // Execute tool
                        ToolCallDTO result = toolExecutor.executeTool(user, tool, arguments);
                        allToolCalls.add(result);

                        // Check for approval requirement
                        if (result.isSuccess() && result.getResult() != null
                            && result.getResult().startsWith("REQUIRES_APPROVAL:")) {

                            pendingApprovalDesc = result.getResult().replace("REQUIRES_APPROVAL: ", "");
                            Matcher tokenMatcher = APPROVAL_TOKEN_PATTERN.matcher(result.getResult());
                            if (tokenMatcher.find()) {
                                pendingApprovalToken = tokenMatcher.group(1);
                                pendingApprovalDesc = pendingApprovalDesc
                                    .replaceAll("\\[APPROVAL_TOKEN:[a-f0-9-]+]", "").trim();
                            }
                            break; // Exit loop — need user approval
                        }

                        // Append observation to conversation
                        String observation = result.isSuccess()
                            ? "Tool " + toolName + " result:\n" + result.getResult()
                            : "Tool " + toolName + " failed: " + result.getErrorMessage();

                        conversationPrompt.append("\n\n").append(observation);
                        conversationPrompt.append("\n\nContinue reasoning. If you have enough information, provide your final answer directly (no tool call).");

                    } catch (IllegalArgumentException e) {
                        failures.add("Unknown tool: " + toolCallJson);
                        conversationPrompt.append("\n\nError: Unknown tool. Available tools: ")
                            .append(java.util.Arrays.toString(ChatToolName.values()));
                    } catch (Exception e) {
                        failures.add("Tool call parse error: " + e.getMessage());
                        conversationPrompt.append("\n\nError parsing tool call. Please use the exact format: ")
                            .append("```tool_call\n{\"tool\": \"TOOL_NAME\", \"arguments\": {}}\n```");
                    }
                } else {
                    // No tool call — this is the final answer
                    finalAnswer = llmResponse.replace(ChatbotPrompts.FINAL_ANSWER_PREFIX, "").trim();
                    break;
                }
            }

            // Step 10: Handle approval case
            if (pendingApprovalToken != null) {
                ChatMessage aiMsg = persistMessage(session, MessageRole.AI, pendingApprovalDesc);
                totalTimer.stop();

                tracingService.finalizeTrace(trace, allToolCalls, retrievedDocIds,
                    totalTimer.getTotalTimeMillis(), totalPromptTokens, totalCompletionTokens,
                    failures, pendingApprovalDesc);

                return ChatResponseDTO.approval(session.getSessionId(), aiMsg.getMessageId(),
                    pendingApprovalDesc, pendingApprovalToken, trace.getRequestId());
            }

            // Step 11: Construct final response
            if (finalAnswer == null || finalAnswer.isBlank()) {
                finalAnswer = "I'm sorry, I encountered an issue processing your request. Please try again.";
                failures.add("No final answer produced after agent loop");
            }

            // Extract citations from retrieved chunks
            List<CitationDTO> citations = extractCitations(chunks);

            // Persist AI response
            ChatMessage aiMessage = persistMessage(session, MessageRole.AI, finalAnswer);

            totalTimer.stop();

            // Finalize trace
            tracingService.finalizeTrace(trace, allToolCalls, retrievedDocIds,
                totalTimer.getTotalTimeMillis(), totalPromptTokens, totalCompletionTokens,
                failures, finalAnswer);

            return ChatResponseDTO.success(session.getSessionId(), aiMessage.getMessageId(),
                finalAnswer, citations, trace.getRequestId());

        } catch (Exception e) {
            log.error("ChatbotAgentService critical failure: {}", e.getMessage(), e);
            failures.add("Critical failure: " + e.getMessage());

            if (totalTimer.isRunning()) totalTimer.stop();

            if (trace != null) {
                tracingService.finalizeTrace(trace, allToolCalls, retrievedDocIds,
                    totalTimer.getTotalTimeMillis(), totalPromptTokens, totalCompletionTokens,
                    failures, null);
            }

            long sessionId = session != null ? session.getSessionId() : 0;
            String traceId = trace != null ? trace.getRequestId() : UUID.randomUUID().toString();
            return ChatResponseDTO.fallback(sessionId, traceId);
        }
    }

    /**
     * Resolve or create a chat session.
     */
    private ChatSession resolveSession(User user, ChatRequestDTO request) {
        if (request.getSessionId() != null) {
            return sessionRepository.findBySessionIdAndUser(request.getSessionId(), user)
                .orElseGet(() -> createSession(user, request.getMessage()));
        }
        return createSession(user, request.getMessage());
    }

    private ChatSession createSession(User user, String firstMessage) {
        String title = firstMessage.length() > 50
            ? firstMessage.substring(0, 50) + "..."
            : firstMessage;

        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setTitle(title);
        session.setCreatedAt(new Date());
        session.setUpdatedAt(new Date());
        return sessionRepository.save(session);
    }

    /**
     * Persist a chat message in the database.
     */
    private ChatMessage persistMessage(ChatSession session, MessageRole role, String content) {
        ChatMessage message = new ChatMessage();
        message.setChatSession(session);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(new Date());

        // Update session timestamp
        session.setUpdatedAt(new Date());
        sessionRepository.save(session);

        return messageRepository.save(message);
    }

    /**
     * Call the LLM with retry logic for transient failures.
     */
    private String callLlmWithRetry(String prompt) {
        Exception lastException = null;

        for (int attempt = 0; attempt < MAX_LLM_RETRIES; attempt++) {
            try {
                return aiService.generateResponse(prompt);
            } catch (Exception e) {
                lastException = e;
                log.warn("LLM call attempt {} failed: {}", attempt + 1, e.getMessage());

                if (attempt < MAX_LLM_RETRIES - 1) {
                    try {
                        Thread.sleep(2000L * (attempt + 1)); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new RuntimeException("LLM call failed after " + MAX_LLM_RETRIES + " attempts", lastException);
    }

    /**
     * Handle an approval confirmation from the user.
     */
    private ChatResponseDTO handleApproval(User user, ChatSession session,
                                            String approvalToken, ChatbotTrace trace, StopWatch timer) {
        // For now, search recent messages for the approval context
        // In a production system, you'd store pending approvals in a separate table
        List<ChatMessage> history = memoryService.getEpisodicMemory(session);

        // Find the approval-related message containing document IDs
        // This is a simplified implementation — a production system would track pending actions
        String result = "Approval processed. However, I need the document IDs to execute the report. " +
                        "Please ask me to run the compliance report again, and I'll guide you through the process.";

        ChatMessage aiMsg = persistMessage(session, MessageRole.AI, result);

        if (timer.isRunning()) timer.stop();

        tracingService.finalizeTrace(trace, List.of(), List.of(),
            timer.getTotalTimeMillis(), 0, 0, List.of(), result);

        return ChatResponseDTO.success(session.getSessionId(), aiMsg.getMessageId(),
            result, List.of(), trace.getRequestId());
    }

    /**
     * Extract citation DTOs from retrieved document chunks.
     */
    private List<CitationDTO> extractCitations(List<org.springframework.ai.document.Document> chunks) {
        if (chunks == null || chunks.isEmpty()) return List.of();

        return chunks.stream()
            .map(chunk -> {
                Map<String, Object> meta = chunk.getMetadata();
                long docId = 0;
                Object docIdObj = meta.get("documentId");
                if (docIdObj instanceof Number) docId = ((Number) docIdObj).longValue();

                String docName = "Document " + docId;
                Integer pageNum = null;
                Object pageObj = meta.get("page_number");
                if (pageObj instanceof Number) pageNum = ((Number) pageObj).intValue();

                String preview = chunk.getText();
                if (preview != null && preview.length() > 200) {
                    preview = preview.substring(0, 200) + "...";
                }

                return new CitationDTO(docId, docName, preview, pageNum);
            })
            .distinct()
            .limit(5)
            .toList();
    }

    /**
     * Parse a JsonNode value into a Java object for tool arguments.
     */
    private Object parseJsonValue(JsonNode node) {
        if (node.isInt()) return node.asInt();
        if (node.isLong()) return node.asLong();
        if (node.isDouble()) return node.asDouble();
        if (node.isBoolean()) return node.asBoolean();
        return node.asText();
    }
}
