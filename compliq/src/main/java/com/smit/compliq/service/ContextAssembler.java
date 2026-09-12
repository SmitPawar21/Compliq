package com.smit.compliq.service;

import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import com.smit.compliq.entity.ChatMessage;
import com.smit.compliq.entity.UserMemory;

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

    /**
     * Assemble a comprehensive chat context with document chunks, user memories, and conversation history.
     * Used by the chatbot agent for grounded response generation.
     */
    public String assembleChatContext(List<Document> chunks, List<UserMemory> memories,
                                      List<ChatMessage> conversationHistory) {
        StringBuilder ctx = new StringBuilder();

        // Section 1: Conversation History
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            ctx.append("--- CONVERSATION HISTORY ---\n");
            // Include last 10 messages max
            int start = Math.max(0, conversationHistory.size() - 10);
            for (int i = start; i < conversationHistory.size(); i++) {
                ChatMessage msg = conversationHistory.get(i);
                ctx.append("[").append(msg.getRole()).append("]: ")
                   .append(truncate(msg.getContent(), 300)).append("\n");
            }
            ctx.append("\n");
        }

        // Section 2: User Memories
        if (memories != null && !memories.isEmpty()) {
            ctx.append("--- USER MEMORIES ---\n");
            for (UserMemory mem : memories) {
                ctx.append("- [").append(mem.getCategory()).append("] ")
                   .append(mem.getMemoryKey()).append(": ")
                   .append(mem.getMemoryValue()).append("\n");
            }
            ctx.append("\n");
        }

        // Section 3: Retrieved Document Chunks
        if (chunks != null && !chunks.isEmpty()) {
            ctx.append("--- RETRIEVED DOCUMENT CONTEXT ---\n\n");
            for (int i = 0; i < chunks.size(); i++) {
                Document doc = chunks.get(i);
                String category = (String) doc.getMetadata().getOrDefault("category", "UNKNOWN");
                Object pageNum = doc.getMetadata().get("page_number");
                Object docId = doc.getMetadata().get("documentId");

                ctx.append("[Chunk ").append(i + 1).append("]");
                ctx.append(" (Category: ").append(category);
                if (docId != null) ctx.append(", DocID: ").append(docId);
                if (pageNum != null) ctx.append(", Page: ").append(pageNum);
                ctx.append(")\n");
                ctx.append(doc.getText()).append("\n\n");
            }
        } else {
            ctx.append("--- No relevant document context was found. ---\n");
        }

        return ctx.toString();
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
