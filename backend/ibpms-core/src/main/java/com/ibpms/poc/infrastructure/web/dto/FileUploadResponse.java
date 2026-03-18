package com.ibpms.poc.infrastructure.web.dto;

import java.util.UUID;

public class FileUploadResponse {
    private UUID documentId;
    private String filePath;

    public FileUploadResponse() {}

    public FileUploadResponse(UUID documentId, String filePath) {
        this.documentId = documentId;
        this.filePath = filePath;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
