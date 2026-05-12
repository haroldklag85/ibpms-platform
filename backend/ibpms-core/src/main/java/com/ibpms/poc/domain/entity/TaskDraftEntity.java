package com.ibpms.poc.domain.entity;

import com.ibpms.poc.crosscutting.annotations.Traceability;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity representing a saved draft of a task form.
 * Allows users to save partial progress on a task without completing it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ibpms_task_draft")
@Traceability(US = "US-017", CA = {"CA-06"})
public class TaskDraftEntity {
    @Id
    @Column(name = "draft_id")
    private UUID draftId;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    private String payloadJson;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
