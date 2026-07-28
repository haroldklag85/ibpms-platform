/* // @Traceability: US-005, CA-42 - Activity Timeline */
package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
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

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "original_filename", nullable = false, length = 500)
    private String filename;

    @Column(name = "file_size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "content_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "storage_path", nullable = false, length = 1000)
    private String storagePath;

    @Column(name = "status", length = 50)
    private String status; // UPLOADED, CONFIRMED

    @Column(name = "uploaded_at")
    private ZonedDateTime uploadedAt;
}
