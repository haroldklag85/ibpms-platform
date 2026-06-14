package com.ibpms.poc.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkipReasonDTO(
    @NotBlank String taskId,
    @NotBlank String skipReason,
    @Size(min = 10, message = "El detalle debe tener al menos 10 caracteres") String skipReasonDetail
) {}
