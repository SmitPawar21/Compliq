package com.smit.compliq.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Analyzes user queries to extract intent, expand for better retrieval,
 * and identify metadata filters (document type, date range).
 * Falls back to the original query if LLM analysis fails.
 */
@Service
@RequiredArgsConstructor
public class QueryUnderstandingService {

    private static final Logger log = LoggerFactory.getLogger(QueryUnderstandingService.class);

    private final AIService aiService;
    private final ObjectMapper objectMapper;

    /**
     * Holds the result of query analysis.
     */
    public static class QueryAnalysis {
        private final String intent;
        private final String expandedQuery;
        private final String documentTypeFilter;
        private final String originalQuery;

        public QueryAnalysis(String intent, String expandedQuery, String documentTypeFilter, String originalQuery) {
            this.intent = intent;
            this.expandedQuery = expandedQuery;
            this.documentTypeFilter = documentTypeFilter;
            this.originalQuery = originalQuery;
        }

        public String getIntent() { return intent; }
        public String getExpandedQuery() { return expandedQuery; }
        public String getDocumentTypeFilter() { return documentTypeFilter; }
        public String getOriginalQuery() { return originalQuery; }

        public static QueryAnalysis fallback(String originalQuery) {
            return new QueryAnalysis("QUESTION", originalQuery, null, originalQuery);
        }
    }

    private static final String QUERY_ANALYSIS_PROMPT = """
        Analyze the following user question and return ONLY valid JSON.
        {
          "intent": "",
          "expandedQuery": "",
          "documentTypeFilter": ""
        }

        Rules:
        - intent: One of QUESTION, REPORT_REQUEST, DOCUMENT_SEARCH, GENERAL
          - QUESTION: The user is asking about compliance, contracts, clauses, legal terms.
          - REPORT_REQUEST: The user wants to generate or run a compliance report.
          - DOCUMENT_SEARCH: The user wants to find or list their uploaded documents.
          - GENERAL: Greetings, help requests, or unrelated queries.
        - expandedQuery: Rewrite the query to improve retrieval. Add synonyms, related legal terms.
          Example: "What are the payment terms?" -> "payment terms, billing schedule, invoicing frequency, payment due date, net payment days"
        - documentTypeFilter: If the user mentions a specific document type (CONTRACT, INVOICE, PURCHASE_ORDER), return it. Otherwise return empty string.
        - Return JSON only. No markdown. No explanations.

        User question:
        """;

    /**
     * Analyze a user query to extract intent and expand for better retrieval.
     * Falls back to the original query if LLM analysis fails.
     */
    public QueryAnalysis analyzeQuery(String userQuery) {
        try {
            String prompt = QUERY_ANALYSIS_PROMPT + userQuery;
            String response = aiService.generateResponse(prompt);

            // Clean markdown wrapping
            if (response.startsWith("```json")) {
                response = response.substring(7);
            }
            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3);
            }
            response = response.trim();

            JsonNode json = objectMapper.readTree(response);

            String intent = json.has("intent") ? json.get("intent").asText("QUESTION") : "QUESTION";
            String expanded = json.has("expandedQuery") ? json.get("expandedQuery").asText(userQuery) : userQuery;
            String docFilter = json.has("documentTypeFilter") ? json.get("documentTypeFilter").asText("") : "";

            if (docFilter.isBlank()) docFilter = null;

            return new QueryAnalysis(intent, expanded, docFilter, userQuery);

        } catch (Exception e) {
            log.warn("Query understanding failed, using fallback: {}", e.getMessage());
            return QueryAnalysis.fallback(userQuery);
        }
    }
}
