package com.ibpms.poc.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Immutable transaction record for each incoming webhook (US-004 CA-1).
 * The unique constraint on message_id enforces idempotency.
 */
@Entity
@Table(name = "ibpms_webhook_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookTransaction {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;

    @Column(name = "sender_email", nullable = false)
    private String senderEmail;

    @Column(name = "sender_domain", nullable = false)
    private String senderDomain;

    @Column(name = "subject")
    private String subject;

    @Column(name = "payload_hash")
    private String payloadHash;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "camunda_process_instance_id")
    private String camundaProcessInstanceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
}
