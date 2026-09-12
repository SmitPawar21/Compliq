package com.smit.compliq.service.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.smit.compliq.dto.chatbot.ToolCallDTO;
import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.User;
import com.smit.compliq.enums.ChatToolName;
import com.smit.compliq.enums.DocumentType;
import com.smit.compliq.repository.DocumentRepository;
import com.smit.compliq.service.ContextAssembler;
import com.smit.compliq.service.VectorStoreService;

@ExtendWith(MockitoExtension.class)
class ToolExecutorTest {

    @Mock private DocumentSearchTool documentSearchTool;
    @Mock private ChunkRetrievalTool chunkRetrievalTool;
    @Mock private DocumentMetadataTool documentMetadataTool;
    @Mock private MemoryTool memoryTool;
    @Mock private ComplianceReportTool complianceReportTool;

    @InjectMocks
    private ToolExecutor toolExecutor;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = new User();
        userA.setUsername("userA");
        userA.setEmail("a@compliq.com");

        userB = new User();
        userB.setUsername("userB");
        userB.setEmail("b@compliq.com");
    }

    @Nested
    @DisplayName("Tool Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("SEARCH_DOCUMENTS should be called with the authenticated user")
        void searchDocumentsShouldUseAuthenticatedUser() {
            when(documentSearchTool.searchDocuments(eq(userA), anyString()))
                .thenReturn("Found 2 documents");

            ToolCallDTO result = toolExecutor.executeTool(
                userA, ChatToolName.SEARCH_DOCUMENTS, Map.of("query", "contract"));

            assertTrue(result.isSuccess());
            verify(documentSearchTool).searchDocuments(eq(userA), eq("contract"));
            // Verify userB was NOT used
            verify(documentSearchTool, never()).searchDocuments(eq(userB), anyString());
        }

        @Test
        @DisplayName("INSPECT_METADATA should be called with the authenticated user")
        void inspectMetadataShouldUseAuthenticatedUser() {
            when(documentMetadataTool.inspectMetadata(eq(userA), eq(1L)))
                .thenReturn("Document Metadata:\n- ID: 1");

            ToolCallDTO result = toolExecutor.executeTool(
                userA, ChatToolName.INSPECT_METADATA, Map.of("documentId", 1));

            assertTrue(result.isSuccess());
            verify(documentMetadataTool).inspectMetadata(eq(userA), eq(1L));
        }

        @Test
        @DisplayName("RETRIEVE_CHUNKS should be called with the authenticated user")
        void retrieveChunksShouldUseAuthenticatedUser() {
            when(chunkRetrievalTool.retrieveChunks(eq(userA), anyString(), any()))
                .thenReturn("Chunk content");

            ToolCallDTO result = toolExecutor.executeTool(
                userA, ChatToolName.RETRIEVE_CHUNKS, Map.of("query", "payment terms"));

            assertTrue(result.isSuccess());
            verify(chunkRetrievalTool).retrieveChunks(eq(userA), eq("payment terms"), isNull());
        }
    }

    @Nested
    @DisplayName("Cross-User Data Isolation Tests")
    class CrossUserIsolationTests {

        @Test
        @DisplayName("User A's tool call should never invoke with User B's context")
        void userAShouldNotAccessUserBData() {
            when(documentSearchTool.searchDocuments(eq(userA), anyString()))
                .thenReturn("User A's documents");

            toolExecutor.executeTool(userA, ChatToolName.SEARCH_DOCUMENTS, Map.of("query", "all"));

            // Verify only userA was passed, never userB
            verify(documentSearchTool).searchDocuments(eq(userA), anyString());
            verify(documentSearchTool, never()).searchDocuments(eq(userB), anyString());
        }

        @Test
        @DisplayName("Memory tool should scope to authenticated user only")
        void memoryToolShouldScopeToUser() {
            when(memoryTool.recallMemories(eq(userA), anyString()))
                .thenReturn("User A memories");

            toolExecutor.executeTool(userA, ChatToolName.RECALL_MEMORIES, Map.of("query", "preferences"));

            verify(memoryTool).recallMemories(eq(userA), eq("preferences"));
            verify(memoryTool, never()).recallMemories(eq(userB), anyString());
        }
    }

    @Nested
    @DisplayName("Failure Recovery Tests")
    class FailureRecoveryTests {

        @Test
        @DisplayName("Should return failure ToolCallDTO when tool throws exception")
        void shouldReturnFailureOnException() {
            when(documentSearchTool.searchDocuments(any(), anyString()))
                .thenThrow(new RuntimeException("Database connection lost"));

            ToolCallDTO result = toolExecutor.executeTool(
                userA, ChatToolName.SEARCH_DOCUMENTS, Map.of("query", "test"));

            assertFalse(result.isSuccess());
            assertNotNull(result.getErrorMessage());
            assertTrue(result.getErrorMessage().contains("Database connection lost"));
        }

        @Test
        @DisplayName("Should handle missing required arguments gracefully")
        void shouldHandleMissingArguments() {
            ToolCallDTO result = toolExecutor.executeTool(
                userA, ChatToolName.INSPECT_METADATA, Map.of());

            assertFalse(result.isSuccess());
            assertNotNull(result.getErrorMessage());
        }

        @Test
        @DisplayName("Should record latency even on failure")
        void shouldRecordLatencyOnFailure() {
            when(documentSearchTool.searchDocuments(any(), anyString()))
                .thenThrow(new RuntimeException("timeout"));

            ToolCallDTO result = toolExecutor.executeTool(
                userA, ChatToolName.SEARCH_DOCUMENTS, Map.of("query", "test"));

            assertFalse(result.isSuccess());
            assertTrue(result.getLatencyMs() >= 0);
        }
    }

    @Nested
    @DisplayName("Approval Flow Tests")
    class ApprovalFlowTests {

        @Test
        @DisplayName("RUN_COMPLIANCE_REPORT should return REQUIRES_APPROVAL")
        void shouldRequireApprovalForReport() {
            when(complianceReportTool.requestReport(eq(userA), eq(1L), eq(2L), eq(3L)))
                .thenReturn(ComplianceReportTool.ReportToolResult.pendingApproval(
                    "Generate compliance report for documents 1, 2, 3"));

            ToolCallDTO result = toolExecutor.executeTool(
                userA, ChatToolName.RUN_COMPLIANCE_REPORT,
                Map.of("contractDocId", 1, "invoiceDocId", 2, "poDocId", 3));

            assertTrue(result.isSuccess());
            assertTrue(result.getResult().contains("REQUIRES_APPROVAL"));
        }
    }

    @Nested
    @DisplayName("Structured Output Tests")
    class StructuredOutputTests {

        @Test
        @DisplayName("Successful tool call should have correct DTO fields")
        void successfulCallShouldHaveCorrectFields() {
            when(documentSearchTool.searchDocuments(any(), anyString()))
                .thenReturn("Found 3 documents");

            ToolCallDTO result = toolExecutor.executeTool(
                userA, ChatToolName.SEARCH_DOCUMENTS, Map.of("query", "all"));

            assertTrue(result.isSuccess());
            assertEquals(ChatToolName.SEARCH_DOCUMENTS, result.getToolName());
            assertNotNull(result.getResult());
            assertTrue(result.getLatencyMs() >= 0);
            assertNull(result.getErrorMessage());
        }

        @Test
        @DisplayName("Failed tool call should have correct DTO fields")
        void failedCallShouldHaveCorrectFields() {
            when(documentSearchTool.searchDocuments(any(), anyString()))
                .thenThrow(new RuntimeException("DB error"));

            ToolCallDTO result = toolExecutor.executeTool(
                userA, ChatToolName.SEARCH_DOCUMENTS, Map.of("query", "all"));

            assertFalse(result.isSuccess());
            assertEquals(ChatToolName.SEARCH_DOCUMENTS, result.getToolName());
            assertNull(result.getResult());
            assertNotNull(result.getErrorMessage());
        }
    }
}
