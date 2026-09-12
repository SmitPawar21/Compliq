package com.smit.compliq.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * LLM-based reranker that scores retrieved chunks by relevance to the query.
 * Falls back to the original ordering if LLM reranking fails.
 */
@Service
@RequiredArgsConstructor
public class RerankingService {

    private static final Logger log = LoggerFactory.getLogger(RerankingService.class);
    private static final int RELEVANCE_THRESHOLD = 3;

    private final AIService aiService;
    private final ObjectMapper objectMapper;

    /**
     * Rerank document chunks by LLM-scored relevance to the query.
     * Chunks scoring below the threshold are filtered out.
     * Falls back to original order on failure.
     *
     * @param query The user's query
     * @param chunks The retrieved chunks to rerank
     * @return Reranked (and filtered) list of chunks
     */
    public List<org.springframework.ai.document.Document> rerank(
            String query, List<org.springframework.ai.document.Document> chunks) {

        if (chunks == null || chunks.isEmpty()) {
            return chunks;
        }

        // For small result sets, skip reranking to save LLM calls
        if (chunks.size() <= 3) {
            return chunks;
        }

        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Score the relevance of each document chunk to the query on a scale of 0-10.\n");
            prompt.append("Return ONLY a JSON array of integers, one score per chunk, in the same order.\n");
            prompt.append("Example: [8, 3, 7, 1, 9]\n");
            prompt.append("No markdown. No explanations.\n\n");
            prompt.append("Query: ").append(query).append("\n\n");

            for (int i = 0; i < chunks.size(); i++) {
                String text = chunks.get(i).getText();
                String preview = text.length() > 300 ? text.substring(0, 300) + "..." : text;
                prompt.append("Chunk ").append(i).append(": ").append(preview).append("\n\n");
            }

            String response = aiService.generateResponse(prompt.toString());

            // Clean markdown wrapping
            if (response.startsWith("```json")) {
                response = response.substring(7);
            }
            if (response.startsWith("```")) {
                response = response.substring(3);
            }
            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3);
            }
            response = response.trim();

            JsonNode scoresNode = objectMapper.readTree(response);

            if (!scoresNode.isArray() || scoresNode.size() != chunks.size()) {
                log.warn("Reranking returned unexpected array size, using original order");
                return chunks;
            }

            // Create scored pairs and sort
            List<ScoredChunk> scored = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                int score = scoresNode.get(i).asInt(5); // default to 5 if parsing fails
                scored.add(new ScoredChunk(chunks.get(i), score));
            }

            return scored.stream()
                .filter(sc -> sc.score >= RELEVANCE_THRESHOLD)
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .map(sc -> sc.document)
                .toList();

        } catch (Exception e) {
            log.warn("Reranking failed, returning original order: {}", e.getMessage());
            return chunks;
        }
    }

    private static class ScoredChunk {
        final org.springframework.ai.document.Document document;
        final int score;

        ScoredChunk(org.springframework.ai.document.Document document, int score) {
            this.document = document;
            this.score = score;
        }
    }
}
