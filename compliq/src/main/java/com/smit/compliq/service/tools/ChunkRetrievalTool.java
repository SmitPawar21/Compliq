package com.smit.compliq.service.tools;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.smit.compliq.entity.User;
import com.smit.compliq.service.ContextAssembler;
import com.smit.compliq.service.VectorStoreService;

import lombok.RequiredArgsConstructor;

/**
 * Tool: RETRIEVE_CHUNKS
 * Retrieves relevant document chunks via hybrid search, always scoped to the authenticated user.
 * Authorization enforced via userId filter in the vector store query.
 */
@Service
@RequiredArgsConstructor
public class ChunkRetrievalTool {

    private static final Logger log = LoggerFactory.getLogger(ChunkRetrievalTool.class);

    private final VectorStoreService vectorStoreService;
    private final ContextAssembler contextAssembler;

    /**
     * Retrieve relevant chunks for a query, filtered by the authenticated user.
     * @param user The authenticated user (authorization boundary)
     * @param query The search query
     * @param documentType Optional document type filter
     * @return Formatted context from retrieved chunks
     */
    public String retrieveChunks(User user, String query, String documentType) {
        try {
            List<org.springframework.ai.document.Document> chunks;

            if (documentType != null && !documentType.isBlank()) {
                Map<String, Object> filters = Map.of("category", documentType);
                chunks = vectorStoreService.hybridSearchWithFilters(query, user.getId(), filters);
            } else {
                chunks = vectorStoreService.hybridSearch(query, user.getId());
            }

            if (chunks == null || chunks.isEmpty()) {
                return "No relevant document chunks found for query: '" + query + "'";
            }

            return contextAssembler.assembleContext(chunks);
        } catch (Exception e) {
            log.error("Chunk retrieval failed for user {}: {}", user.getId(), e.getMessage());
            return "Failed to retrieve document chunks. Error: " + e.getMessage();
        }
    }

    /**
     * Returns raw chunk documents for citation extraction.
     */
    public List<org.springframework.ai.document.Document> retrieveRawChunks(User user, String query) {
        try {
            return vectorStoreService.hybridSearch(query, user.getId());
        } catch (Exception e) {
            log.error("Raw chunk retrieval failed for user {}: {}", user.getId(), e.getMessage());
            return List.of();
        }
    }
}
