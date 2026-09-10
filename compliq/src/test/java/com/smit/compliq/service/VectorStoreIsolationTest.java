package com.smit.compliq.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class VectorStoreIsolationTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private VectorStoreService vectorStoreService;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void testSimilaritySearchIsolatesByUser() {
        Long userId = 123L;
        String query = "test query";

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
            .thenReturn(Collections.emptyList());

        List<Document> results = vectorStoreService.similaritySearch(query, userId);

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(requestCaptor.capture());

        SearchRequest request = requestCaptor.getValue();
        assertThat(request.getQuery()).isEqualTo(query);
        // Ensure the filter expression enforces user isolation
        assertThat(request.getFilterExpression().toString()).contains("userId").contains(String.valueOf(userId));
    }
}
