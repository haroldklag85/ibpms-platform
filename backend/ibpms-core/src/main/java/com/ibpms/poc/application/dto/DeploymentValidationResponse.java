package com.ibpms.poc.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentValidationResponse {
    private boolean valid;
    private String processId;
    private List<ValidationError> errors = new ArrayList<>();
    private List<ValidationError> warnings = new ArrayList<>();
    private List<String> generatedRoles = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String node;
        private String message;
    }

    public void addError(String node, String message) {
        this.errors.add(new ValidationError(node, message));
        this.valid = false;
    }

    public void addWarning(String node, String message) {
        this.warnings.add(new ValidationError(node, message));
    }
}
