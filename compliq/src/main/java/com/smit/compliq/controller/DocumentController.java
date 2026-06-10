package com.smit.compliq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import com.smit.compliq.dto.DocumentDTO;
import com.smit.compliq.entity.Document;
import com.smit.compliq.enums.DocumentType;
import com.smit.compliq.exception.DocumentNotFoundException;
import com.smit.compliq.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
	
	private final DocumentService documentService;
	
	public DocumentController(DocumentService documentService) {
		super();
		this.documentService = documentService;
	}
	
	@PostMapping("/upload")
	public ResponseEntity<?> postDocument(
				@RequestParam("file") MultipartFile file,
		        @RequestParam("documentType") DocumentType documentType,
		        @RequestParam("uploadedByUserId") Integer uploadedByUserId
			) {
		try {
			 Document doc = documentService.postDocument(
		                file,
		                documentType,
		                uploadedByUserId
		        );
			
			return ResponseEntity.status(HttpStatus.CREATED).body(doc);
		} catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	    }
	}
	
	@GetMapping("/")
	public ResponseEntity<?> getDocuments() {
		try {
			List<Document> docs = documentService.getAllDocuments();
			if(docs==null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No Documents in Database");
			}
			return ResponseEntity.ok(docs);
		} catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	    }
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getOneDocument(@PathVariable long id) {
		try {
			Document doc = documentService.getOneDocument(id);
			if(doc==null) {
				throw new DocumentNotFoundException("Document Not Found with id: "+id);
			}
			return ResponseEntity.ok(doc);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteDocument(@PathVariable long id) {
		try {
			Document doc = documentService.deleteDocument(id);
			
			return ResponseEntity.ok(doc);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}
