package com.smit.compliq.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smit.compliq.dto.chatbot.ToolCallDTO;
import com.smit.compliq.entity.ChatSession;
import com.smit.compliq.entity.ChatbotTrace;
import com.smit.compliq.entity.User;
import com.smit.compliq.enums.ChatToolName;
import com.smit.compliq.repository.ChatbotTraceRepository;

@ExtendWith(MockitoExtension.class)
class ChatbotTracingServiceTest {

    @Mock private ChatbotTraceRepository traceRepository;

    @InjectMocks
    private ChatbotTracingService tracingService;

    private User testUser;
    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");

        testSession = new ChatSession();
        testSession.setSessionId(1L);
        testSession.setUser(testUser);

        // Set model name via reflection since @Value won't be injected in unit tests
        ReflectionTestUtils.setField(tracingService, "modelName", "gemini-2.5-flash");
        // Inject a real ObjectMapper
        ReflectionTestUtils.setField(tracingService, "objectMapper", new ObjectMapper());
    }

    @Nested
    @DisplayName("Trace Creation Tests")
    class TraceCreationTests {

        @Test
        @DisplayName("Should create trace with correct initial values")
        void shouldCreateTraceWithCorrectValues() {
            ChatbotTrace trace = tracingService.startTrace(testUser, testSession);

            assertNotNull(trace);
            assertNotNull(trace.getRequestId());
            assertEquals(testUser, trace.getUser());
            assertEquals(testSession, trace.getSession());
            assertEquals("gemini-2.5-flash", trace.getModel());
            assertEquals("1.0", trace.getPromptVersion());
            assertNotNull(trace.getCreatedAt());
        }

        @Test
        @DisplayName("Should generate unique request IDs")
        void shouldGenerateUniqueRequestIds() {
            ChatbotTrace trace1 = tracingService.startTrace(testUser, testSession);
            ChatbotTrace trace2 = tracingService.startTrace(testUser, testSession);

            assertNotEquals(trace1.getRequestId(), trace2.getRequestId());
        }
    }

    @Nested
    @DisplayName("Cost Calculation Tests")
    class CostCalculationTests {

        @Test
        @DisplayName("Should calculate cost correctly based on token usage")
        void shouldCalculateCostCorrectly() {
            ChatbotTrace trace = tracingService.startTrace(testUser, testSession);

            tracingService.finalizeTrace(trace, List.of(), List.of(),
                500, 1000, 200, List.of(), "Test response");

            // Cost = (1000 * 0.00000015) + (200 * 0.0000006) = 0.00015 + 0.00012 = 0.00027
            ArgumentCaptor<ChatbotTrace> captor = ArgumentCaptor.forClass(ChatbotTrace.class);
            verify(traceRepository).save(captor.capture());

            ChatbotTrace saved = captor.getValue();
            assertEquals(1000, saved.getPromptTokens());
            assertEquals(200, saved.getCompletionTokens());
            assertEquals(0.00027, saved.getTotalCost(), 0.00001);
        }

        @Test
        @DisplayName("Should handle zero tokens")
        void shouldHandleZeroTokens() {
            ChatbotTrace trace = tracingService.startTrace(testUser, testSession);

            tracingService.finalizeTrace(trace, List.of(), List.of(),
                100, 0, 0, List.of(), "Test");

            ArgumentCaptor<ChatbotTrace> captor = ArgumentCaptor.forClass(ChatbotTrace.class);
            verify(traceRepository).save(captor.capture());

            assertEquals(0.0, captor.getValue().getTotalCost());
        }
    }

    @Nested
    @DisplayName("Secret Sanitization Tests")
    class SanitizationTests {

        @Test
        @DisplayName("Should redact JWT tokens from final response preview")
        void shouldRedactJwtTokens() {
            ChatbotTrace trace = tracingService.startTrace(testUser, testSession);

            String responseWithJwt = "Token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.HMAC_SIGNATURE_HERE";
            tracingService.finalizeTrace(trace, List.of(), List.of(),
                100, 100, 50, List.of(), responseWithJwt);

            ArgumentCaptor<ChatbotTrace> captor = ArgumentCaptor.forClass(ChatbotTrace.class);
            verify(traceRepository).save(captor.capture());

            String preview = captor.getValue().getFinalResponsePreview();
            assertFalse(preview.contains("eyJhbGciOiJIUzI1NiJ9"));
            assertTrue(preview.contains("[REDACTED_JWT]"));
        }

        @Test
        @DisplayName("Should redact API keys from failure logs")
        void shouldRedactApiKeys() {
            ChatbotTrace trace = tracingService.startTrace(testUser, testSession);

            List<String> failures = List.of("api_key=AKIAQYUKZZHPTUHELGQZ connection failed");
            tracingService.finalizeTrace(trace, List.of(), List.of(),
                100, 100, 50, failures, "Error occurred");

            ArgumentCaptor<ChatbotTrace> captor = ArgumentCaptor.forClass(ChatbotTrace.class);
            verify(traceRepository).save(captor.capture());

            String failureLog = captor.getValue().getFailures();
            assertFalse(failureLog.contains("AKIAQYUKZZHPTUHELGQZ"));
        }

        @Test
        @DisplayName("Should truncate final response to 500 chars")
        void shouldTruncateFinalResponse() {
            ChatbotTrace trace = tracingService.startTrace(testUser, testSession);

            String longResponse = "A".repeat(1000);
            tracingService.finalizeTrace(trace, List.of(), List.of(),
                100, 100, 50, List.of(), longResponse);

            ArgumentCaptor<ChatbotTrace> captor = ArgumentCaptor.forClass(ChatbotTrace.class);
            verify(traceRepository).save(captor.capture());

            assertTrue(captor.getValue().getFinalResponsePreview().length() <= 500);
        }
    }

    @Nested
    @DisplayName("Trace Persistence Tests")
    class PersistenceTests {

        @Test
        @DisplayName("Should persist trace with all fields")
        void shouldPersistAllFields() {
            ChatbotTrace trace = tracingService.startTrace(testUser, testSession);

            List<ToolCallDTO> toolCalls = List.of(
                ToolCallDTO.success(ChatToolName.SEARCH_DOCUMENTS, java.util.Map.of("query", "test"), "result", 50)
            );
            List<String> docIds = List.of("1", "2", "3");
            List<String> failures = List.of("minor warning");

            tracingService.finalizeTrace(trace, toolCalls, docIds,
                500, 1000, 200, failures, "Final answer");

            ArgumentCaptor<ChatbotTrace> captor = ArgumentCaptor.forClass(ChatbotTrace.class);
            verify(traceRepository).save(captor.capture());

            ChatbotTrace saved = captor.getValue();
            assertEquals(500, saved.getLatencyMs());
            assertEquals(1000, saved.getPromptTokens());
            assertEquals(200, saved.getCompletionTokens());
            assertNotNull(saved.getToolCalls());
            assertNotNull(saved.getRetrievedDocumentIds());
            assertNotNull(saved.getFailures());
            assertNotNull(saved.getFinalResponsePreview());
        }

        @Test
        @DisplayName("Should not crash if traceRepository.save throws")
        void shouldNotCrashOnPersistenceFailure() {
            ChatbotTrace trace = tracingService.startTrace(testUser, testSession);
            doThrow(new RuntimeException("DB error")).when(traceRepository).save(any());

            // Should not throw
            assertDoesNotThrow(() ->
                tracingService.finalizeTrace(trace, List.of(), List.of(),
                    100, 0, 0, List.of(), "test")
            );
        }
    }
}
