package com.ibpms.poc.domain.model.agile;

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
public class AgileTask {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

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

    @Column(name = "sla_deadline")
    private ZonedDateTime slaDeadline;

    @Column(name = "last_activity_at", nullable = false)
    private ZonedDateTime lastActivityAt;

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

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
        this.lastActivityAt = ZonedDateTime.now();
        if (this.status == null) this.status = "TODO";
        if (this.position == null) this.position = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
        this.lastActivityAt = ZonedDateTime.now();
    }
}
