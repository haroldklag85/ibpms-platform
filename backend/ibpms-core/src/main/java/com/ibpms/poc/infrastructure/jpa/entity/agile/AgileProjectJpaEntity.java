// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.entity.agile;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_agile_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgileProjectJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "methodology", nullable = false)
    private String methodology;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "closed_at")
    private ZonedDateTime closedAt;

    @Column(name = "closed_by")
    private String closedBy;

    @Column(name = "max_active_tasks", nullable = false)
    private Integer maxActiveTasks;

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
        if (this.methodology == null) this.methodology = "KANBAN_CONTINUOUS";
        if (this.maxActiveTasks == null) this.maxActiveTasks = 500;
    }
}
