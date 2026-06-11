package com.smit.compliq.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.smit.compliq.entity.Document;
import com.smit.compliq.service.OCRService;
import com.smit.compliq.service.S3Service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;

@Service
@RequiredArgsConstructor
public class TesseractOCRService implements OCRService {
	@Value("${tesseract.data.path}")
    private String tessDataPath;

    private final S3Service s3Service;

    public TesseractOCRService(S3Service s3Service) {
		super();
		this.s3Service = s3Service;
	}

	@Override
    public String extractText(Document document) {

        try {

            byte[] fileBytes =
                    s3Service.downloadFile(
                            document.getS3Key());

            File tempFile =
                    File.createTempFile(
                            "ocr",
                            document.getFileName());

            Files.write(
                    tempFile.toPath(),
                    fileBytes);

            Tesseract tesseract =
                    new Tesseract();

            tesseract.setDatapath(
                    tessDataPath);

            String text =
                    tesseract.doOCR(
                            tempFile);

            tempFile.delete();

            return text;

        } catch (Exception e) {

            throw new RuntimeException(
                    "OCR processing failed",
                    e);
        }
    }
}

