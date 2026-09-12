package com.smit.compliq.service.tools;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.User;
import com.smit.compliq.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Tool: INSPECT_METADATA
 * Inspects metadata of a document, verifying ownership first.
 * Authorization enforced: query is scoped to the authenticated user.
 */
@Service
@RequiredArgsConstructor
public class DocumentMetadataTool {

    private static final Logger log = LoggerFactory.getLogger(DocumentMetadataTool.class);

    private final DocumentRepository documentRepository;

    /**
     * Inspect metadata for a specific document owned by the user.
     * @param user The authenticated user (authorization boundary)
     * @param documentId The document ID to inspect
     * @return Formatted metadata string
     */
    public String inspectMetadata(User user, long documentId) {
        Optional<Document> docOpt = documentRepository.findByDocIdAndUploadedBy(documentId, user);

        if (docOpt.isEmpty()) {
            return "Document not found or you do not have access to document ID: " + documentId;
        }

        Document doc = docOpt.get();
        StringBuilder sb = new StringBuilder();
        sb.append("Document Metadata:\n");
        sb.append("- ID: ").append(doc.getDoc_id()).append("\n");
        sb.append("- File Name: ").append(doc.getFileName()).append("\n");
        sb.append("- Title: ").append(doc.getTitle() != null ? doc.getTitle() : "N/A").append("\n");
        sb.append("- Type: ").append(doc.getDocumentType()).append("\n");
        sb.append("- Upload Date: ").append(doc.getUploadDate()).append("\n");
        sb.append("- Processing Status: ").append(doc.getProcessingStatus()).append("\n");
        sb.append("- Embedding Status: ").append(doc.getEmbeddingStatus()).append("\n");

        if (doc.getOrganization() != null) {
            sb.append("- Organization: ").append(doc.getOrganization().getName()).append("\n");
        }

        // Redact S3 key and file URL from tool output
        sb.append("- Has File URL: ").append(doc.getFileUrl() != null ? "Yes" : "No").append("\n");

        return sb.toString();
    }
}
