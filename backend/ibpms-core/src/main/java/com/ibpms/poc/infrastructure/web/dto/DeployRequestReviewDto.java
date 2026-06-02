package com.ibpms.poc.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// @Traceability: US-005, CA-34
@Data
public class DeployRequestReviewDto {
    @NotNull(message = "Debe especificar si aprueba o rechaza")
    private Boolean approved;
    
    private String comment;
}
