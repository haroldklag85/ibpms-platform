package com.ibpms.poc.domain.model.agile;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Timebox (Sprint) dentro de un AgileProject.
 * Representa una ventana temporal de trabajo con fechas de inicio y cierre.
 */
@Entity
@Table(name = "ibpms_agile_timeboxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgileTimebox {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "goal", length = 500)
    private String goal;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
        if (this.status == null) this.status = "PLANNING";
    }
}
