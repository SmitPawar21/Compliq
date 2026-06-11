package com.smit.compliq.service;

import com.smit.compliq.entity.Document;

public interface OCRService {
	String extractText(Document document);
}
