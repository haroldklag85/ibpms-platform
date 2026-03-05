package com.ibpms.core.project.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProjectTaskExecutionDTO {
    private String id;
    private String projectId;
    private String wbsTaskTemplateId;
    private String status;
    private String assigneeUserId;
    private BigDecimal actualBudget;
    private LocalDateTime startDatePlan;
    private LocalDateTime endDatePlan;
}
