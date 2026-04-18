package com.ibpms.poc.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Forensic record for rejected/malformed/quarantined payloads (US-004 CA-3, CA-11).
 */
@Entity
@Table(name = "ibpms_orphan_payloads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrphanPayload {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayload;

    @Column(name = "error_type", nullable = false)
    private String errorType;

    @Column(name = "file_hash_sha256")
    private String fileHashSha256;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "sender_email")
    private String senderEmail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
}
