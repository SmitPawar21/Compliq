package com.smit.compliq.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.compliq.dto.chatbot.ChatRequestDTO;
import com.smit.compliq.dto.chatbot.ChatResponseDTO;
import com.smit.compliq.entity.ChatMessage;
import com.smit.compliq.entity.ChatSession;
import com.smit.compliq.entity.User;
import com.smit.compliq.entity.UserMemory;
import com.smit.compliq.enums.MemoryCategory;
import com.smit.compliq.enums.MessageRole;
import com.smit.compliq.repository.ChatMessageRepository;
import com.smit.compliq.repository.ChatSessionRepository;
import com.smit.compliq.service.tools.ChunkRetrievalTool;
import com.smit.compliq.service.tools.ToolExecutor;

@ExtendWith(MockitoExtension.class)
class ChatbotAgentServiceTest {

    @Mock private AIService aiService;
    @Mock private MemoryService memoryService;
    @Mock private ChatbotTracingService tracingService;
    @Mock private ContextAssembler contextAssembler;
    @Mock private QueryUnderstandingService queryUnderstandingService;
    @Mock private RerankingService rerankingService;
    @Mock private ChunkRetrievalTool chunkRetrievalTool;
    @Mock private ToolExecutor toolExecutor;
    @Mock private ChatSessionRepository sessionRepository;
    @Mock private ChatMessageRepository messageRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private ChatbotAgentService chatbotAgentService;

    private User testUser;
    private User otherUser;
    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@compliq.com");

        otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@compliq.com");

        testSession = new ChatSession();
        testSession.setSessionId(1L);
        testSession.setUser(testUser);
        testSession.setTitle("Test Session");
        testSession.setCreatedAt(new Date());
        testSession.setUpdatedAt(new Date());
    }

    @Nested
    @DisplayName("Retrieval Pipeline Tests")
    class RetrievalTests {

        @Test
        @DisplayName("Should call hybrid search with correct userId")
        void shouldCallHybridSearchWithCorrectUserId() {
            // Arrange
            ChatRequestDTO request = new ChatRequestDTO("What are the payment terms?");

            when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);
            when(tracingService.startTrace(any(), any())).thenReturn(new com.smit.compliq.entity.ChatbotTrace("req-123", testUser, "model", "1.0"));
            when(queryUnderstandingService.analyzeQuery(anyString()))
                .thenReturn(new QueryUnderstandingService.QueryAnalysis("QUESTION", "payment terms", null, "What are the payment terms?"));
            when(memoryService.getEpisodicMemory(any())).thenReturn(List.of());
            when(memoryService.getAllMemories(any())).thenReturn(List.of());
            when(chunkRetrievalTool.retrieveRawChunks(eq(testUser), anyString())).thenReturn(List.of());
            when(rerankingService.rerank(anyString(), anyList())).thenReturn(List.of());
            when(contextAssembler.assembleChatContext(anyList(), anyList(), anyList())).thenReturn("context");
            when(aiService.generateResponse(anyString())).thenReturn("The payment terms are net 30 days.");
            when(messageRepository.save(any())).thenReturn(new ChatMessage());

            // Act
            chatbotAgentService.processMessage(testUser, request);

            // Assert — verify chunks retrieved with correct user
            verify(chunkRetrievalTool).retrieveRawChunks(eq(testUser), anyString());
        }

        @Test
        @DisplayName("Should apply metadata filters from query understanding")
        void shouldApplyMetadataFilters() {
            ChatRequestDTO request = new ChatRequestDTO("Show me contract clauses");

            when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);
            when(tracingService.startTrace(any(), any())).thenReturn(new com.smit.compliq.entity.ChatbotTrace("req-123", testUser, "model", "1.0"));
            when(queryUnderstandingService.analyzeQuery(anyString()))
                .thenReturn(new QueryUnderstandingService.QueryAnalysis("QUESTION", "contract clauses", "CONTRACT", "Show me contract clauses"));
            when(memoryService.getEpisodicMemory(any())).thenReturn(List.of());
            when(memoryService.getAllMemories(any())).thenReturn(List.of());
            when(chunkRetrievalTool.retrieveRawChunks(any(), anyString())).thenReturn(List.of());
            when(rerankingService.rerank(anyString(), anyList())).thenReturn(List.of());
            when(contextAssembler.assembleChatContext(anyList(), anyList(), anyList())).thenReturn("context");
            when(aiService.generateResponse(anyString())).thenReturn("Here are the contract clauses.");
            when(messageRepository.save(any())).thenReturn(new ChatMessage());

            chatbotAgentService.processMessage(testUser, request);

            verify(queryUnderstandingService).analyzeQuery("Show me contract clauses");
        }
    }

    @Nested
    @DisplayName("Failure Recovery Tests")
    class FailureRecoveryTests {

        @Test
        @DisplayName("Should return fallback response when LLM fails")
        void shouldReturnFallbackOnLlmFailure() {
            ChatRequestDTO request = new ChatRequestDTO("What are the payment terms?");

            when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);
            when(tracingService.startTrace(any(), any())).thenReturn(new com.smit.compliq.entity.ChatbotTrace("req-123", testUser, "model", "1.0"));
            when(queryUnderstandingService.analyzeQuery(anyString()))
                .thenReturn(QueryUnderstandingService.QueryAnalysis.fallback("What are the payment terms?"));
            when(memoryService.getEpisodicMemory(any())).thenReturn(List.of());
            when(memoryService.getAllMemories(any())).thenReturn(List.of());
            when(chunkRetrievalTool.retrieveRawChunks(any(), anyString())).thenReturn(List.of());
            when(rerankingService.rerank(anyString(), anyList())).thenReturn(List.of());
            when(contextAssembler.assembleChatContext(anyList(), anyList(), anyList())).thenReturn("context");
            when(aiService.generateResponse(anyString())).thenThrow(new RuntimeException("LLM timeout"));
            when(messageRepository.save(any())).thenReturn(new ChatMessage());

            ChatResponseDTO response = chatbotAgentService.processMessage(testUser, request);

            assertNotNull(response);
            // Should not throw — should return gracefully
            assertNotNull(response.getTraceId());
        }

        @Test
        @DisplayName("Should handle query understanding failure gracefully")
        void shouldHandleQueryUnderstandingFailure() {
            ChatRequestDTO request = new ChatRequestDTO("What are the clauses?");

            when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);
            when(tracingService.startTrace(any(), any())).thenReturn(new com.smit.compliq.entity.ChatbotTrace("req-123", testUser, "model", "1.0"));
            when(queryUnderstandingService.analyzeQuery(anyString()))
                .thenThrow(new RuntimeException("Parse error"));
            when(memoryService.getEpisodicMemory(any())).thenReturn(List.of());
            when(memoryService.getAllMemories(any())).thenReturn(List.of());
            when(chunkRetrievalTool.retrieveRawChunks(any(), anyString())).thenReturn(List.of());
            when(rerankingService.rerank(anyString(), anyList())).thenReturn(List.of());
            when(contextAssembler.assembleChatContext(anyList(), anyList(), anyList())).thenReturn("context");
            when(aiService.generateResponse(anyString())).thenReturn("Here's what I found.");
            when(messageRepository.save(any())).thenReturn(new ChatMessage());

            // Should not throw — should use fallback query analysis
            ChatResponseDTO response = chatbotAgentService.processMessage(testUser, request);
            assertNotNull(response);
        }

        @Test
        @DisplayName("Should handle retrieval failure gracefully")
        void shouldHandleRetrievalFailure() {
            ChatRequestDTO request = new ChatRequestDTO("Find payment info");

            when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);
            when(tracingService.startTrace(any(), any())).thenReturn(new com.smit.compliq.entity.ChatbotTrace("req-123", testUser, "model", "1.0"));
            when(queryUnderstandingService.analyzeQuery(anyString()))
                .thenReturn(QueryUnderstandingService.QueryAnalysis.fallback("Find payment info"));
            when(memoryService.getEpisodicMemory(any())).thenReturn(List.of());
            when(memoryService.getAllMemories(any())).thenReturn(List.of());
            when(chunkRetrievalTool.retrieveRawChunks(any(), anyString()))
                .thenThrow(new RuntimeException("Vector DB connection failed"));
            when(contextAssembler.assembleChatContext(anyList(), anyList(), anyList())).thenReturn("context");
            when(aiService.generateResponse(anyString())).thenReturn("I couldn't retrieve documents.");
            when(messageRepository.save(any())).thenReturn(new ChatMessage());

            ChatResponseDTO response = chatbotAgentService.processMessage(testUser, request);
            assertNotNull(response);
        }
    }

    @Nested
    @DisplayName("Structured Output Tests")
    class StructuredOutputTests {

        @Test
        @DisplayName("Should return properly structured ChatResponseDTO")
        void shouldReturnStructuredResponse() {
            ChatRequestDTO request = new ChatRequestDTO("Hello");

            when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);
            when(tracingService.startTrace(any(), any())).thenReturn(new com.smit.compliq.entity.ChatbotTrace("req-123", testUser, "model", "1.0"));
            when(queryUnderstandingService.analyzeQuery(anyString()))
                .thenReturn(new QueryUnderstandingService.QueryAnalysis("GENERAL", "Hello", null, "Hello"));
            when(memoryService.getEpisodicMemory(any())).thenReturn(List.of());
            when(memoryService.getAllMemories(any())).thenReturn(List.of());
            when(contextAssembler.assembleChatContext(anyList(), anyList(), anyList())).thenReturn("context");
            when(aiService.generateResponse(anyString())).thenReturn("Hello! How can I help you with compliance today?");
            when(messageRepository.save(any())).thenReturn(new ChatMessage());

            ChatResponseDTO response = chatbotAgentService.processMessage(testUser, request);

            assertNotNull(response);
            assertEquals(testSession.getSessionId(), response.getSessionId());
            assertNotNull(response.getResponse());
            assertFalse(response.isRequiresApproval());
            assertNotNull(response.getTraceId());
        }
    }

    @Nested
    @DisplayName("Session Management Tests")
    class SessionTests {

        @Test
        @DisplayName("Should create new session when sessionId is null")
        void shouldCreateNewSession() {
            ChatRequestDTO request = new ChatRequestDTO("Hello");
            request.setSessionId(null);

            when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);
            when(tracingService.startTrace(any(), any())).thenReturn(new com.smit.compliq.entity.ChatbotTrace("req-123", testUser, "model", "1.0"));
            when(queryUnderstandingService.analyzeQuery(anyString()))
                .thenReturn(new QueryUnderstandingService.QueryAnalysis("GENERAL", "Hello", null, "Hello"));
            when(memoryService.getEpisodicMemory(any())).thenReturn(List.of());
            when(memoryService.getAllMemories(any())).thenReturn(List.of());
            when(contextAssembler.assembleChatContext(anyList(), anyList(), anyList())).thenReturn("context");
            when(aiService.generateResponse(anyString())).thenReturn("Hello!");
            when(messageRepository.save(any())).thenReturn(new ChatMessage());

            chatbotAgentService.processMessage(testUser, request);

            verify(sessionRepository, atLeastOnce()).save(any(ChatSession.class));
        }

        @Test
        @DisplayName("Should reuse existing session when sessionId is provided")
        void shouldReuseExistingSession() {
            ChatRequestDTO request = new ChatRequestDTO("Follow up question");
            request.setSessionId(1L);

            when(sessionRepository.findBySessionIdAndUser(1L, testUser)).thenReturn(Optional.of(testSession));
            when(sessionRepository.save(any(ChatSession.class))).thenReturn(testSession);
            when(tracingService.startTrace(any(), any())).thenReturn(new com.smit.compliq.entity.ChatbotTrace("req-123", testUser, "model", "1.0"));
            when(queryUnderstandingService.analyzeQuery(anyString()))
                .thenReturn(new QueryUnderstandingService.QueryAnalysis("QUESTION", "Follow up question", null, "Follow up question"));
            when(memoryService.getEpisodicMemory(any())).thenReturn(List.of());
            when(memoryService.getAllMemories(any())).thenReturn(List.of());
            when(chunkRetrievalTool.retrieveRawChunks(any(), anyString())).thenReturn(List.of());
            when(rerankingService.rerank(anyString(), anyList())).thenReturn(List.of());
            when(contextAssembler.assembleChatContext(anyList(), anyList(), anyList())).thenReturn("context");
            when(aiService.generateResponse(anyString())).thenReturn("Here's the follow up.");
            when(messageRepository.save(any())).thenReturn(new ChatMessage());

            chatbotAgentService.processMessage(testUser, request);

            verify(sessionRepository).findBySessionIdAndUser(1L, testUser);
        }
    }
}
