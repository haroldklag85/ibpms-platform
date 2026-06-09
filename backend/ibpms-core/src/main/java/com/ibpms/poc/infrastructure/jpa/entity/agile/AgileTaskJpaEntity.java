// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.entity.agile;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ibpms_agile_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgileTaskJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "team_id")
    private String teamId;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "effort_estimated")
    private BigDecimal effortEstimated;

    @Column(name = "effort_actual")
    private BigDecimal effortActual;

    @Column(name = "notes")
    private String notes;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "draft_payload", columnDefinition = "TEXT")
    private String draftPayload;

    @Column(name = "draft_payload_hash", length = 64)
    private String draftPayloadHash;

    @Column(name = "draft_expires_at")
    private ZonedDateTime draftExpiresAt;

    @Column(name = "sla_deadline")
    private ZonedDateTime slaDeadline;

    @Column(name = "last_activity_at", nullable = false)
    private ZonedDateTime lastActivityAt;

    @Column(name = "timeout_extensions")
    @Builder.Default
    private Integer timeoutExtensions = 0;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ibpms_agile_task_assignees", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "user_id")
    private Set<String> assigneeIds;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ibpms_agile_task_tags", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "tag")
    private Set<String> tags;

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
        this.lastActivityAt = ZonedDateTime.now();
        if (this.status == null) this.status = "TODO";
        if (this.position == null) this.position = 0;
    }

    /**
     * ⚠️ DEUDA TÉCNICA (Sprint PM-01): Este @PreUpdate resetea lastActivityAt en CADA save(),
     * lo que interfiere con la lógica de ghost timeout del CA-15. Pendiente refactorizar
     * para que solo se actualice cuando hay una acción registrable real del usuario.
     * Ref: approval_request_BACKEND.md — Decisión #4
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
        this.lastActivityAt = ZonedDateTime.now();
    }
}
