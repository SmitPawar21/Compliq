package com.smit.compliq.entity;

import java.util.Date;

import com.smit.compliq.enums.DocumentType;
import com.smit.compliq.enums.EmbeddingStatus;
import com.smit.compliq.enums.ProcessingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="document")
public class Document {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long docId;
	
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@Column
	private String title;
	
	@Column
	private String fileName;
	
	@Column
	private DocumentType documentType;
	
	@Column
	private String fileUrl;
	
	@Column
	private String s3Key;
	
	@Enumerated(EnumType.STRING)
	@Column
	private EmbeddingStatus embeddingStatus;

	@Column(columnDefinition = "TEXT")
	private String metadata;
	
	@ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User uploadedBy;
	
	@Column
	private Date uploadDate;
	
	@Column
	private ProcessingStatus processingStatus;
	
	public Document() {}

	public Document(Organization organization, String title, String fileName, DocumentType documentType, String fileUrl, String s3Key,
			EmbeddingStatus embeddingStatus, String metadata, User uploadedBy, Date uploadDate, ProcessingStatus processingStatus) {
		super();
		this.organization = organization;
		this.title = title;
		this.fileName = fileName;
		this.documentType = documentType;
		this.fileUrl = fileUrl;
		this.s3Key = s3Key;
		this.embeddingStatus = embeddingStatus;
		this.metadata = metadata;
		this.uploadedBy = uploadedBy;
		this.uploadDate = uploadDate;
		this.processingStatus = processingStatus;
	}

	public long getDoc_id() {
		return docId;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	public EmbeddingStatus getEmbeddingStatus() {
		return embeddingStatus;
	}

	public void setEmbeddingStatus(EmbeddingStatus embeddingStatus) {
		this.embeddingStatus = embeddingStatus;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
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
}
