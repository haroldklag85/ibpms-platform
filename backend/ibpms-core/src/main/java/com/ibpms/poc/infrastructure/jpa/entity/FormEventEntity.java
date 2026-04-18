package com.ibpms.poc.infrastructure.jpa.entity;

import com.ibpms.poc.domain.model.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "form_event_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormEventEntity {
    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Column(name = "user_id")
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    private String payloadJson;

    @Column(name = "idempotency_key", unique = true)
    private UUID idempotencyKey;

    @Column(name = "schema_version", nullable = false)
    private String schemaVersion;

    @Column(name = "original_event_id")
    private UUID originalEventId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.ZonedDateTime createdAt;
}
