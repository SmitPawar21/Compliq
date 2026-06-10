package com.smit.compliq.entity;

import java.util.Date;

import com.smit.compliq.enums.DocumentType;
import com.smit.compliq.enums.ProcessingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="document")
public class Document {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long doc_id;
	
	@Column
	private String fileName;
	
	@Column
	private DocumentType documentType;
	
	@Column
	private String fileUrl;
	
	@Column
	private String s3Key;
	
	@ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User uploadedBy;
	
	@Column
	private Date uploadDate;
	
	@Column
	private ProcessingStatus processingStatus;
	
	public Document() {}

	public Document(String fileName, DocumentType documentType, String fileUrl, User uploadedBy, Date uploadDate,
			ProcessingStatus processingStatus) {
		super();
		this.fileName = fileName;
		this.documentType = documentType;
		this.fileUrl = fileUrl;
		this.uploadedBy = uploadedBy;
		this.uploadDate = uploadDate;
		this.processingStatus = processingStatus;
	}

	public long getDoc_id() {
		return doc_id;
	}

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

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public User getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(User uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}

	public ProcessingStatus getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(ProcessingStatus processingStatus) {
		this.processingStatus = processingStatus;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}	
}
