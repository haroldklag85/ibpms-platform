// @Traceability: US-003 - ADR-001
package com.ibpms.poc.domain.model.agile;

import lombok.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Timebox (Sprint) dentro de un AgileProject.
 * Representa una ventana temporal de trabajo con fechas de inicio y cierre.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgileTimebox {
    private UUID id;
    private UUID projectId;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String createdBy;
    private ZonedDateTime createdAt;
}
