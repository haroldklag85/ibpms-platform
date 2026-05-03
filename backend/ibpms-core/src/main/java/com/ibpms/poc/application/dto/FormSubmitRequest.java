package com.ibpms.poc.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmitRequest {
    @NotNull(message = "El payload principal no puede ser nulo")
    private Map<String, Object> payload;
    
    @NotBlank(message = "Se requiere un schemaVersion")
    private String schemaVersion;
    
    private UUID idempotencyKey;
    
    private Map<String, Object> gatewayVariables;

    @JsonProperty("_visibleFields")
    private List<String> visibleFields;
}
