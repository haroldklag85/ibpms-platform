// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.entity.agile;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_agile_sla_changelog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgileSlaChangelogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "previous_value")
    private ZonedDateTime previousValue;

    @Column(name = "new_value")
    private ZonedDateTime newValue;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private ZonedDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = ZonedDateTime.now();
    }
}
