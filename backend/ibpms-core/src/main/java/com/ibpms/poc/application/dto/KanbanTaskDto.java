package com.ibpms.poc.application.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KanbanTaskDto {
    private String id;
    private String originalTaskId;
    private String title;
    private String state;
    private String assignee;
    private LocalDateTime slaExpirationDate;
    private String blockedReason;
}
