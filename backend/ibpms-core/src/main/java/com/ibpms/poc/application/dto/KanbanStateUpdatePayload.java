package com.ibpms.poc.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KanbanStateUpdatePayload {
    @JsonProperty("new_status")
    private String newStatus;
    
    private String assignee;
    private String reason;
}
