package com.ibpms.poc.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "Response payload for Generic Form Context")
public class GenericFormContextResponse {

    @Schema(description = "Fixed JSON Schema with observations, attachments, and management_result")
    private Map<String, Object> schema;

    @Schema(description = "Prefill Data extracted from Camunda Variables with Whitelist mapping")
    private Map<String, Object> prefillData;

    @Schema(description = "Allowed management results (e.g., APPROVED, REJECTED)")
    private List<String> allowedResults;

    public GenericFormContextResponse() {}

    public GenericFormContextResponse(Map<String, Object> schema, Map<String, Object> prefillData, List<String> allowedResults) {
        this.schema = schema;
        this.prefillData = prefillData;
        this.allowedResults = allowedResults;
    }

    public Map<String, Object> getSchema() {
        return schema;
    }

    public void setSchema(Map<String, Object> schema) {
        this.schema = schema;
    }

    public Map<String, Object> getPrefillData() {
        return prefillData;
    }

    public void setPrefillData(Map<String, Object> prefillData) {
        this.prefillData = prefillData;
    }

    public List<String> getAllowedResults() {
        return allowedResults;
    }

    public void setAllowedResults(List<String> allowedResults) {
        this.allowedResults = allowedResults;
    }
}
