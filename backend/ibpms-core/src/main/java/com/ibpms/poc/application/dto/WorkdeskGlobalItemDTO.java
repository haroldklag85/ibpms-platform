package com.ibpms.poc.application.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkdeskGlobalItemDTO {
    private String unifiedId;
    private String sourceSystem;
    private String originalTaskId;
    private String title;
    private LocalDateTime slaExpirationDate;
    private String status;
    private String assignee;
    private Integer impactLevel;
    
    // El frontend espera este DTO unificado y paginado.
}
