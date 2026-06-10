package com.smit.compliq.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.smit.compliq.entity.Document;
import com.smit.compliq.service.TextractService;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;

@Service
@RequiredArgsConstructor
public class TextractImpl implements TextractService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final TextractClient textractClient;
    
    public TextractImpl(TextractClient textractClient) {
    	this.textractClient = textractClient;
    }

    @Override
    public String extractText(Document document) {

        S3Object s3Object = S3Object.builder()
                .bucket(bucketName)
                .name(document.getS3Key())
                .build();

        software.amazon.awssdk.services.textract.model.Document textractDocument =
                software.amazon.awssdk.services.textract.model.Document
                        .builder()
                        .s3Object(s3Object)
                        .build();

        DetectDocumentTextRequest request =
                DetectDocumentTextRequest.builder()
                        .document(textractDocument)
                        .build();

        DetectDocumentTextResponse response =
                textractClient.detectDocumentText(request);

        StringBuilder extractedText = new StringBuilder();

        response.blocks().stream()
                .filter(block -> block.blockType() == BlockType.LINE)
                .forEach(block ->
                        extractedText
                                .append(block.text())
                                .append("\n")
                );

        return extractedText.toString();
    }
}

