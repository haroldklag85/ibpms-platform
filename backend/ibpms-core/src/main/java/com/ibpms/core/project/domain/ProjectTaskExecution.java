package com.ibpms.core.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ib_project_task_execution")
@Getter
@Setter
public class ProjectTaskExecution {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private String id = UUID.randomUUID().toString();

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "wbs_task_template_id", nullable = false)
    private String wbsTaskTemplateId;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, IN_PROGRESS, DONE, BLOCKED

    @Column(name = "assignee_user_id")
    private String assigneeUserId;

    @Column(name = "actual_budget")
    private BigDecimal actualBudget;

    @Column(name = "start_date_plan")
    private LocalDateTime startDatePlan;

    @Column(name = "end_date_plan")
    private LocalDateTime endDatePlan;

    @Column(name = "start_date_actual")
    private LocalDateTime startDateActual;

    @Column(name = "end_date_actual")
    private LocalDateTime endDateActual;

    @Column(name = "camunda_process_instance_id")
    private String camundaProcessInstanceId;
}
