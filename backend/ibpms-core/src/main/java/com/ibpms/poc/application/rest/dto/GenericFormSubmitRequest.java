package com.ibpms.poc.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Schema(description = "Submit payload for Generic Form with Optional Panic Actions")
public class GenericFormSubmitRequest {

    @Schema(description = "User observations for the step", minLength = 10, maxLength = 2000)
    private String observations;

    @Schema(description = "UUIDs of attachments", nullable = true)
    private List<UUID> attachmentUuids;

    @Schema(description = "Management result of the step", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "managementResult is required")
    private String managementResult;

    @Schema(description = "Panic Action enum (APPROVED, RETURNED, CANCELLED)", nullable = true)
    private String panicAction;

    @Schema(description = "Justification for Panic Action (min 20 chars if panicAction applied)", nullable = true)
    private String panicJustification;

    public GenericFormSubmitRequest() {}

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public List<UUID> getAttachmentUuids() {
        return attachmentUuids;
    }

    public void setAttachmentUuids(List<UUID> attachmentUuids) {
        this.attachmentUuids = attachmentUuids;
    }

    public String getManagementResult() {
        return managementResult;
    }

    public void setManagementResult(String managementResult) {
        this.managementResult = managementResult;
    }

    public String getPanicAction() {
        return panicAction;
    }

    public void setPanicAction(String panicAction) {
        this.panicAction = panicAction;
    }

    public String getPanicJustification() {
        return panicJustification;
    }

    public void setPanicJustification(String panicJustification) {
        this.panicJustification = panicJustification;
    }
}
