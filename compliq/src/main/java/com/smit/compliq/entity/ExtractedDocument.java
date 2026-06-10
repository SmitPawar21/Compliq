package com.smit.compliq.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;

@Entity
public class ExtractedDocument {
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private Document document;

    @Lob
    private String extractedText;

    private Date extractedAt;

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getExtractedText() {
		return extractedText;
	}

	public void setExtractedText(String extractedText) {
		this.extractedText = extractedText;
	}

	public Date getExtractedAt() {
		return extractedAt;
	}

	public void setExtractedAt(Date extractedAt) {
		this.extractedAt = extractedAt;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
