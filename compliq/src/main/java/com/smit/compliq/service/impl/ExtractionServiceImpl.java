package com.smit.compliq.service.impl;

import org.springframework.stereotype.Service;

import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.ExtractedDocument;
import com.smit.compliq.entity.ExtractedField;
import com.smit.compliq.enums.ProcessingStatus;
import com.smit.compliq.exception.DocumentNotFoundException;
import com.smit.compliq.repository.DocumentRepository;
import com.smit.compliq.repository.ExtractedDocumentRepository;
import com.smit.compliq.repository.ExtractedFieldRepository;
import com.smit.compliq.service.ExtractionService;
import com.smit.compliq.service.TextractService;

import java.util.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExtractionServiceImpl implements ExtractionService {
	private final DocumentRepository documentRepository;

    private final ExtractedDocumentRepository extractedDocumentRepository;

    private final ExtractedFieldRepository extractedFieldRepository;

    private final TextractService textractService;

    public ExtractionServiceImpl(DocumentRepository documentRepository,
			ExtractedDocumentRepository extractedDocumentRepository, ExtractedFieldRepository extractedFieldRepository,
			TextractService textractService) {
		super();
		this.documentRepository = documentRepository;
		this.extractedDocumentRepository = extractedDocumentRepository;
		this.extractedFieldRepository = extractedFieldRepository;
		this.textractService = textractService;
	}

	@Override
    @Transactional
    public void processDocument(long documentId) {

        Document document = documentRepository.findById(documentId);
        
        if(document==null) throw new DocumentNotFoundException("Document not found with id: "+documentId);

        try {

            document.setProcessingStatus(
                    ProcessingStatus.PROCESSING);

            documentRepository.save(document);

            String extractedText =
                    textractService.extractText(document);

            ExtractedDocument extractedDocument =
                    new ExtractedDocument();

            extractedDocument.setDocument(document);
            extractedDocument.setExtractedText(extractedText);
            extractedDocument.setExtractedAt(new Date());

            extractedDocumentRepository.save(
                    extractedDocument);

            extractFields(
                    document,
                    extractedText);

            document.setProcessingStatus(
                    ProcessingStatus.COMPLETED);

            documentRepository.save(document);

        } catch (Exception e) {

            document.setProcessingStatus(
                    ProcessingStatus.FAILED);

            documentRepository.save(document);

            throw e;
        }
    }

    private void extractFields(
            Document document,
            String text) {

        switch (document.getDocumentType()) {

            case INVOICE ->
                    extractInvoiceFields(
                            document,
                            text);

            case PURCHASE_ORDER ->
                    extractPOFields(
                            document,
                            text);

            case CONTRACT ->
                    extractContractFields(
                            document,
                            text);
        }
    }

    private void extractInvoiceFields(
            Document document,
            String text) {

        saveField(
                document,
                "RAW_TEXT",
                text);
    }

    private void extractPOFields(
            Document document,
            String text) {

        saveField(
                document,
                "RAW_TEXT",
                text);
    }

    private void extractContractFields(
            Document document,
            String text) {

        saveField(
                document,
                "RAW_TEXT",
                text);
    }

    private void saveField(
            Document document,
            String fieldName,
            String fieldValue) {

        ExtractedField field =
                new ExtractedField();

        field.setDocument(document);
        field.setFieldName(fieldName);
        field.setFieldValue(fieldValue);

        extractedFieldRepository.save(field);
    }
}
