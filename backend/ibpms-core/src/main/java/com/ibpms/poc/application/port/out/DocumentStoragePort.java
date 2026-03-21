package com.ibpms.poc.application.port.out;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

import java.io.InputStream;

public interface DocumentStoragePort {
    String saveDocument(UUID documentId, MultipartFile file);
    String uploadFile(String fileName, InputStream data, long length, String mimeType);
    InputStream downloadFile(String blobUri);
    String generateSecureUrl(String blobUri);
}
