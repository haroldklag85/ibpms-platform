// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.ibpms.poc.crosscutting.annotations.Traceability;

// @Traceability: US-003 - ADR-001 - Persistencia de Borradores
@Entity
@Table(name = "task_drafts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Traceability(US = "US-003", CA = {"CA-91"})
public class TaskDraftJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "current_step")
    private Integer currentStep;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "partial_data", nullable = false, columnDefinition = "jsonb")
    private String partialData;

    @Column(name = "schema_version", nullable = false)
    private String schemaVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
