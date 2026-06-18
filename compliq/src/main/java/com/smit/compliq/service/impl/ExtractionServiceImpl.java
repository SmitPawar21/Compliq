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
import com.smit.compliq.service.DocumentDataPersistenceService;
import com.smit.compliq.service.ExtractionService;
import com.smit.compliq.service.OCRService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.transaction.Transactional;

@Service
public class ExtractionServiceImpl implements ExtractionService {
	private final DocumentRepository documentRepository;

    private final ExtractedDocumentRepository extractedDocumentRepository;

    private final ExtractedFieldRepository extractedFieldRepository;

    private final OCRService ocrService;
    
    private final DocumentDataPersistenceService docDataPersistantService;

    public ExtractionServiceImpl(DocumentRepository documentRepository,
			ExtractedDocumentRepository extractedDocumentRepository, ExtractedFieldRepository extractedFieldRepository,
			OCRService ocrService, DocumentDataPersistenceService docDataPersistantService) {
		super();
		this.documentRepository = documentRepository;
		this.extractedDocumentRepository = extractedDocumentRepository;
		this.extractedFieldRepository = extractedFieldRepository;
		this.ocrService = ocrService;
		this.docDataPersistantService = docDataPersistantService;
	}

	@Override
    @Transactional
    public void processDocument(long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                new DocumentNotFoundException(
                        "Document not found with ID: " + documentId));
        
        try {

            document.setProcessingStatus(
                    ProcessingStatus.PROCESSING);

            documentRepository.save(document);

            String extractedText =
            		ocrService.extractText(document);

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
            
            docDataPersistantService.persistExtractedData(document);

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
                    extractInvoiceFields(document, text);

            case PURCHASE_ORDER ->
                    extractPOFields(document, text);

            case CONTRACT ->
                    extractContractFields(document, text);
        }
    }

    private void extractInvoiceFields(Document document, String text) {
        saveField(document, "INVOICE_NUMBER", extractInvoiceNumber(text));
        saveField(document, "VENDOR_NAME", extractVendorName(text));
        saveField(document, "TOTAL_AMOUNT", extractAmount(text));
    }

    private void extractPOFields(Document document, String text) {
    	saveField(document,"PO_NUMBER",extractPONumber(text));
        saveField(document,"VENDOR_NAME",extractVendorName(text));
        saveField(document,"TOTAL_AMOUNT",extractAmount(text));
    }

    private void extractContractFields(Document document,String text) {
    	saveField(document,"CONTRACT_NUMBER",extractContractNumber(text));
        saveField(document,"VENDOR_NAME",extractVendorName(text));
        saveField(document,"START_DATE",extractStartDate(text));
        saveField(document,"END_DATE", extractEndDate(text));
    }

    private void saveField(Document document,String fieldName,String fieldValue) {

        ExtractedField field = new ExtractedField();

        field.setDocument(document);
        field.setFieldName(fieldName);
        field.setFieldValue(fieldValue);

        extractedFieldRepository.save(field);
    }
    
    // simple regex parsers
    private String extractInvoiceNumber(String text) {

        return extractValue(
                text,
                "Invoice\\s*(?:Number|No|#)\\s*[:\\-]?\\s*([A-Za-z0-9\\-/]+)"
        );
    }

    private String extractVendorName(String text) {

        return extractValue(
                text,
                "Vendor\\s*(?:Name)?\\s*[:\\-]?\\s*([A-Za-z0-9 &.,()-]+)"
        );
    }

    private String extractAmount(String text) {

        return extractValue(
                text,
                "(?:Total Amount|Invoice Amount|Amount|Total)\\s*[:\\-]?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)"
        );
    }

    private String extractPONumber(String text) {

        return extractValue(
                text,
                "(?:PO Number|Purchase Order Number|PO No)\\s*[:\\-]?\\s*([A-Za-z0-9\\-/]+)"
        );
    }

    private String extractContractNumber(String text) {

        return extractValue(
                text,
                "Contract\\s*(?:Number|No|#)\\s*[:\\-]?\\s*([A-Za-z0-9\\-/]+)"
        );
    }

    private String extractStartDate(String text) {

        return extractValue(
                text,
                "Start Date\\s*[:\\-]?\\s*([0-9/\\-]+)"
        );
    }

    private String extractEndDate(String text) {

        return extractValue(
                text,
                "End Date\\s*[:\\-]?\\s*([0-9/\\-]+)"
        );
    }

    private String extractValue(String text, String regex) {

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }
    
}
