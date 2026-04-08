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
    
    // CA-23: Porcentaje de avance determinista (null = N/D)
    private Integer progressPercent;

    // CA-03: Badge textual para la UI ('⚡ Flujo' o '📅 Proyecto')
    private String typeBadge;

    // CA-17: Flag de impacto financiero alto para badge visual
    private boolean financialImpactHigh;
}
