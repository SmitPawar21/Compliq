package com.smit.compliq.service;

import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

@Service
public class ContextAssembler {

    public String assembleContext(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "No relevant context found.";
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("--- RELEVANT CONTEXT ---\n\n");

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String category = (String) doc.getMetadata().getOrDefault("category", "UNKNOWN");
            
            contextBuilder.append("--- Document Chunk (Category: ").append(category).append(") ---\n");
            contextBuilder.append(doc.getText()).append("\n\n");
        }

        return contextBuilder.toString();
    }
}
