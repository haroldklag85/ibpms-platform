package com.ibpms.poc.application.dto;

import java.util.Map;

/**
 * DTO para la transferencia de metadatos al iniciar un servicio manualmente
 * (Intake).
 */
public class ManualStartDTO {
    private String definitionKey;
    private String businessKey;
    private String type;
    private Map<String, Object> initialVariables;

    // Getters y Setters
    public String getDefinitionKey() {
        return definitionKey;
    }

    public void setDefinitionKey(String definitionKey) {
        this.definitionKey = definitionKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getInitialVariables() {
        return initialVariables;
    }

    public void setInitialVariables(Map<String, Object> initialVariables) {
        this.initialVariables = initialVariables;
    }
}
