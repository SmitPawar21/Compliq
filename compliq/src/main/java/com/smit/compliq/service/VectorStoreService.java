package com.smit.compliq.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void storeDocuments(List<Document> chunks) {
        vectorStore.add(chunks);
    }

    public List<Document> similaritySearch(String query, Long organizationId) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(10)
                .filterExpression("organizationId == " + organizationId)
                .build();
        return vectorStore.similaritySearch(request);
    }

    private List<Document> keywordSearch(String query, Long organizationId) {
        String sql = """
            SELECT id, content, metadata 
            FROM vector_store 
            WHERE metadata->>'organizationId' = ? 
            AND to_tsvector('english', content) @@ plainto_tsquery('english', ?) 
            ORDER BY ts_rank(to_tsvector('english', content), plainto_tsquery('english', ?)) DESC 
            LIMIT 10
        """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String content = rs.getString("content");
            String metadataJson = rs.getString("metadata");
            
            java.util.Map<String, Object> metadata = new java.util.HashMap<>();
            try {
                if (metadataJson != null && !metadataJson.isEmpty()) {
                    metadata = objectMapper.readValue(metadataJson, new TypeReference<java.util.Map<String, Object>>() {});
                }
            } catch (Exception e) {
                // ignore
            }
            
            return new Document(content, metadata);
        }, organizationId.toString(), query, query);
    }

    public List<Document> hybridSearch(String query, Long organizationId) {
        StopWatch sw = new StopWatch();
        sw.start("Hybrid Retrieval");
        
        List<Document> vectorResults = similaritySearch(query, organizationId);
        List<Document> keywordResults = keywordSearch(query, organizationId);
        
        List<Document> results = reciprocalRankFusion(vectorResults, keywordResults);
        
        sw.stop();
        DiagnosticContextHolder.getContext().addRetrieval(sw.getTotalTimeMillis(), results.size());
        
        return results;
    }
    
    private List<Document> reciprocalRankFusion(List<Document> vectorResults, List<Document> keywordResults) {
        java.util.Map<String, Double> rrfScores = new java.util.HashMap<>();
        java.util.Map<String, Document> documentMap = new java.util.HashMap<>();
        
        int k = 60;
        
        for (int i = 0; i < vectorResults.size(); i++) {
            Document doc = vectorResults.get(i);
            String hash = String.valueOf(doc.getText().hashCode());
            documentMap.put(hash, doc);
            rrfScores.put(hash, 1.0 / (k + i + 1));
        }
        
        for (int i = 0; i < keywordResults.size(); i++) {
            Document doc = keywordResults.get(i);
            String hash = String.valueOf(doc.getText().hashCode());
            documentMap.putIfAbsent(hash, doc);
            rrfScores.put(hash, rrfScores.getOrDefault(hash, 0.0) + (1.0 / (k + i + 1)));
        }
        
        return rrfScores.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .map(entry -> documentMap.get(entry.getKey()))
                .toList();
    }
}
