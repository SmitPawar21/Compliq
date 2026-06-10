package com.smit.compliq.dto;

import java.util.Date;

import com.smit.compliq.enums.DocumentType;
import com.smit.compliq.enums.ProcessingStatus;

import jakarta.validation.constraints.NotBlank;

public class DocumentDTO {
private String fileName;
	
	@NotBlank
	private DocumentType documentType;
	
//	@NotBlank
//	private String fileUrl;
	
	@NotBlank
    private long uploadedByUserId;
	
	@NotBlank
	private Date uploadDate;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public DocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(DocumentType documentType) {
		this.documentType = documentType;
	}

//	public String getFileUrl() {
//		return fileUrl;
//	}

//	public void setFileUrl(String fileUrl) {
//		this.fileUrl = fileUrl;
//	}

	public long getUploadedByUserId() {
		return uploadedByUserId;
	}

	public void setUploadedByUserId(long uploadedByUserId) {
		this.uploadedByUserId = uploadedByUserId;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}
}
