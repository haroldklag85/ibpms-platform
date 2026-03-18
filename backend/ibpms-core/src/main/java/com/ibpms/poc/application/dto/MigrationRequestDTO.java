package com.ibpms.poc.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * CA-10: Payload protegido contra Data-Patching. 
 * Ignora agresivamente cualquier mapeo de "variables" que intente modificar el estado del proceso en pleno vuelo.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MigrationRequestDTO {

    @NotEmpty(message = "Debe proveer una lista de identificadores de instancia a migrar.")
    private List<String> instanceIds;
    
    private String sourceProcessDefinitionId;
    private String targetProcessDefinitionId;
}
