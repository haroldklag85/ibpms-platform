package com.ibpms.poc.infrastructure.web.inbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la Integración de Robots RPA.
 * Mapea exactamente el Payload de Contrato exigido en el Handoff del Sprint 7.
 * La validación (@NotBlank, @NotNull) blinda el backend contra envíos
 * malformados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpaNotificationDTO {

    @NotBlank(message = "El origen del RPA es obligatorio (Ej: RPA_RAMA_JUDICIAL)")
    private String origen;

    @NotBlank(message = "El ID del Trámite/Radicado es obligatorio")
    private String tramiteId;

    @NotBlank(message = "La descripción de la notificación legal no puede estar vacía")
    private String descripcionNotificacion;

    @NotNull(message = "La lista de partes involucradas es obligatoria")
    private List<String> partes;

    @NotNull(message = "La fecha de publicación no puede ser nula")
    private LocalDateTime fechaPublicacion;

    /**
     * Objeto flexible para que el RPA pueda inyectar datos adicionales
     * como 'tipo_documento', 'riesgo', etc., sin romper el contrato estricto.
     */
    private Map<String, Object> metadataAdicional;
}
