package com.smit.compliq.service;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.User;
import com.smit.compliq.enums.DocumentType;
import com.smit.compliq.enums.ProcessingStatus;
import com.smit.compliq.exception.DocumentNotFoundException;
import com.smit.compliq.exception.UserNotFoundException;
import com.smit.compliq.repository.DocumentRepository;
import com.smit.compliq.repository.UserRepository;

import java.util.*;

@Service
public class DocumentService {
	private final DocumentRepository documentRepository;
	private final UserRepository userRepository;
	private final S3Service s3Service;
	
	public DocumentService (DocumentRepository documentRepository, UserRepository userRepository, S3Service s3Service) {
		this.documentRepository = documentRepository;
		this.userRepository = userRepository;
		this.s3Service = s3Service;
	}
	
	private static final long MAX_FILE_SIZE =
	        10 * 1024 * 1024;

	private static final Set<String> ALLOWED_CONTENT_TYPES =
	        Set.of(
	                "application/pdf",
	                "image/png",
	                "image/jpeg"
	        );
	
	private void validateFile(MultipartFile file) {

	    if (file == null || file.isEmpty()) {
	        throw new IllegalArgumentException("File cannot be empty");
	    }

	    if (file.getSize() > MAX_FILE_SIZE) {
	        throw new IllegalArgumentException(
	                "File size cannot exceed 10 MB");
	    }

	    if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
	        throw new IllegalArgumentException(
	                "Only PDF, PNG, JPG and JPEG files are allowed");
	    }
	}
	
	public Document postDocument(
	        MultipartFile file,
	        DocumentType documentType,
	        long uploadedByUserId) {
		
		validateFile(file);

	    User user = userRepository.findById(uploadedByUserId);

	    if (user == null) {
	        throw new UserNotFoundException(
	                "User not found with ID: " + uploadedByUserId);
	    }

	    String key = s3Service.uploadFile(file);

	    String fileUrl = s3Service.getFileUrl(key);

	    Document newDoc = new Document();

	    newDoc.setDocumentType(documentType);
	    newDoc.setFileName(file.getOriginalFilename());

	    newDoc.setS3Key(key);
	    newDoc.setFileUrl(fileUrl);

	    newDoc.setProcessingStatus(ProcessingStatus.UPLOADED);
	    newDoc.setUploadDate(new Date());
	    newDoc.setUploadedBy(user);

	    return documentRepository.save(newDoc);
	}
	
	public List<Document> getAllDocuments() {
		List<Document> docs = documentRepository.findAll();
		
		if(docs.size()==0) return null;
		return docs;
	}
	
	public Document getOneDocument(long doc_id) {
		return documentRepository.findById(doc_id)
		        .orElseThrow(() ->
                new DocumentNotFoundException(
                        "Document not found with ID: " + doc_id));
	}
	
	public Document deleteDocument(long doc_id) {
		Document doc = documentRepository.findById(doc_id)
		        .orElseThrow(() ->
                new DocumentNotFoundException(
                        "Document not found with ID: " + doc_id));
		
		s3Service.deleteFile(doc.getS3Key());
		documentRepository.delete(doc);
		
		return doc;
	}
}
