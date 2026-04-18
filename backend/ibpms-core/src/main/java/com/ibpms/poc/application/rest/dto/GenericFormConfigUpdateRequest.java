package com.ibpms.poc.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Request to update Generic Form whitelist config")
public class GenericFormConfigUpdateRequest {

    @Schema(description = "Whitelist of variables to extract for prefillData", maxLength = 10)
    @Size(max = 10, message = "Whitelist cannot exceed 10 variables")
    private List<String> whitelist;

    public GenericFormConfigUpdateRequest() {}

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }
}
