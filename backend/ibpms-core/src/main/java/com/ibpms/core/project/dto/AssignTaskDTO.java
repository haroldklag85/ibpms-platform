package com.ibpms.core.project.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AssignTaskDTO {
    private String assigneeUserId;
    private BigDecimal actualBudget;
}
