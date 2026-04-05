package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad Plana de Solo Lectura para la arquitectura CQRS (Workdesk Global Inbox).
 */
@Entity
@Table(name = "ibpms_workdesk_projection")
@FilterDef(name = "assigneeSecurityFilter", parameters = {@ParamDef(name = "currentUserId", type = String.class)})
@Filter(name = "assigneeSecurityFilter", condition = "assignee = :currentUserId OR assignee IS NULL")
@Getter
@Setter
public class WorkdeskProjectionEntity {

    @Id
    private String id; // Hash unificado

    @Column(name = "source_system", nullable = false)
    private String sourceSystem; // BPMN o KANBAN

    @Column(name = "original_task_id", nullable = false)
    private String originalTaskId;

    @Column(nullable = false)
    private String title;

    @Column
    private String assignee;

    @Column(name = "candidate_group")
    private String candidateGroup;

    @Column(name = "sla_expiration_date")
    private LocalDateTime slaExpirationDate;

    @Column(nullable = false)
    private String status;

    @Column(name = "payload_metadata", columnDefinition = "JSON")
    private String payloadMetadata; // Soporte JSON nativo
}
