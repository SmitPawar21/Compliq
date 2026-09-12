package com.smit.compliq.service.tools;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.User;
import com.smit.compliq.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Tool: SEARCH_DOCUMENTS
 * Searches documents uploaded by the authenticated user only.
 * Authorization is enforced at the data-access layer — the query is always scoped to the user.
 */
@Service
@RequiredArgsConstructor
public class DocumentSearchTool {

    private static final Logger log = LoggerFactory.getLogger(DocumentSearchTool.class);

    private final DocumentRepository documentRepository;

    /**
     * Search for documents belonging to the given user, optionally filtering by query.
     * @param user The authenticated user (authorization boundary)
     * @param query Search term to match against file name or title
     * @return Formatted string of matching documents
     */
    public String searchDocuments(User user, String query) {
        List<Document> userDocs = documentRepository.findByUploadedBy(user);

        if (userDocs == null || userDocs.isEmpty()) {
            return "No documents found for the current user.";
        }

        // Filter by query if provided
        List<Document> filtered = userDocs;
        if (query != null && !query.isBlank()) {
            String lowerQuery = query.toLowerCase();
            filtered = userDocs.stream()
                .filter(doc -> {
                    String name = doc.getFileName() != null ? doc.getFileName().toLowerCase() : "";
                    String title = doc.getTitle() != null ? doc.getTitle().toLowerCase() : "";
                    String type = doc.getDocumentType() != null ? doc.getDocumentType().name().toLowerCase() : "";
                    return name.contains(lowerQuery) || title.contains(lowerQuery) || type.contains(lowerQuery);
                })
                .collect(Collectors.toList());
        }

        if (filtered.isEmpty()) {
            return "No documents matching '" + query + "' found for the current user.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(filtered.size()).append(" document(s):\n");
        for (Document doc : filtered) {
            sb.append("- ID: ").append(doc.getDoc_id())
              .append(", Name: ").append(doc.getFileName())
              .append(", Type: ").append(doc.getDocumentType())
              .append(", Uploaded: ").append(doc.getUploadDate())
              .append("\n");
        }
        return sb.toString();
    }
}
