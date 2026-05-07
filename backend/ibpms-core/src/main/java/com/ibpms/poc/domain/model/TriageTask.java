package com.ibpms.poc.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_triage_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriageTask {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "camunda_process_instance_id", nullable = false)
    private String camundaProcessInstanceId;

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(name = "sender_email", nullable = false)
    private String senderEmail;

    @Column(name = "subject")
    private String subject;

    @Column(name = "attachment_count")
    private Integer attachmentCount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "sla_deadline", nullable = false)
    private ZonedDateTime slaDeadline;

    @Column(name = "scan_status")
    private String scanStatus;

    @Column(name = "file_sha256_hash")
    private String fileSha256Hash;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }
}
