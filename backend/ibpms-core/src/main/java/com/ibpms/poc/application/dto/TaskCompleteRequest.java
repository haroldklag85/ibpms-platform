package com.ibpms.poc.application.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ibpms.poc.infrastructure.web.validator.StrictPrimitiveTyping;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Objeto de Data Transfer para orquestación de Complete Task (CA-47, CA-50).
 */
@JsonIgnoreProperties(ignoreUnknown = true) // CA-47: Ignorar unknown properties de métricas del frontend
public class TaskCompleteRequest {

    @NotNull(message = "El bloque de variables no puede ser nulo")
    @StrictPrimitiveTyping // CA-50: API estrictamente tipada
    private Map<String, Object> variables = new HashMap<>();

    private Map<String, Object> hiddenMetadata = new HashMap<>();

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    // CA-47: Los campos encriptados o de metadata ("sys_origin") se absorben aquí silenciosamente
    @JsonAnySetter
    public void setHiddenMetadata(String key, Object value) {
        this.hiddenMetadata.put(key, value);
    }

    public Map<String, Object> getHiddenMetadata() {
        return hiddenMetadata;
    }
}
