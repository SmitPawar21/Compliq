package com.smit.compliq.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;

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
}
