package com.smit.compliq.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smit.compliq.service.S3Service;

import java.util.*;
import java.io.*;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3ServiceImpl implements S3Service {
	@Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final S3Client s3Client;

    public S3ServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }
    
    @Override
    public String uploadFile(MultipartFile file) {

        String key =
                UUID.randomUUID()
                + "_"
                + file.getOriginalFilename();

        try {

            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            return key;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file");
        }
    }

    @Override
    public byte[] downloadFile(String key) {

        GetObjectRequest request =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

        ResponseBytes<GetObjectResponse> object =
                s3Client.getObjectAsBytes(request);

        return object.asByteArray();
    }

    @Override
    public void deleteFile(String key) {

        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

        s3Client.deleteObject(request);
    }

    @Override
    public String getFileUrl(String key) {

        return String.format(
                "https://%s.s3.amazonaws.com/%s",
                bucketName,
                key
        );
    }
}
