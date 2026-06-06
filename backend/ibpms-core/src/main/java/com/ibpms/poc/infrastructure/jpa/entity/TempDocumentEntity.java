package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_temp_documents")
@Data
public class TempDocumentEntity {

    @Id
    private UUID id;

    private String taskId;
    private String userId;
    private String filename;
    private Long sizeBytes;
    private String mimeType;
    private String storagePath;
    private String status; // UPLOADED, CONFIRMED
    private ZonedDateTime uploadedAt;
}
