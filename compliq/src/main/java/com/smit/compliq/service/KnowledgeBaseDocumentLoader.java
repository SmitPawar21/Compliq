package com.smit.compliq.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import com.smit.compliq.entity.Organization;

@Service
public class KnowledgeBaseDocumentLoader {

    public List<Document> loadPdfAndExtractMetadata(InputStream pdfStream, Organization org, Long documentId, String documentType) throws Exception {
        // Save stream to temp file because PagePdfDocumentReader needs a Resource
        File tempFile = File.createTempFile("temp-doc-", ".pdf");
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            pdfStream.transferTo(out);
        }

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(new FileSystemResource(tempFile));
        List<Document> documents = pdfReader.get();

        // Inject custom metadata
        for (Document doc : documents) {
            Map<String, Object> metadata = doc.getMetadata();
            if (org != null) {
                metadata.put("organizationId", org.getId());
                metadata.put("organizationName", org.getName());
            }
            metadata.put("documentId", documentId);
            metadata.put("category", documentType);
        }

        tempFile.delete();
        return documents;
    }
}
