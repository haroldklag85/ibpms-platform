package com.ibpms.poc.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Payload para certificar el diseño de un Formulario y su Schema Zod (US-028 CA-10).
 */
public class FormCertificationRequestDTO {

    @NotBlank(message = "El Zod Schema es obligatorio para la certificación.")
    private String zodSchema;

    @NotNull(message = "Se requiere un Payload Dummy validado para sellar la certificación.")
    private Map<String, Object> dummyPayload;

    public String getZodSchema() {
        return zodSchema;
    }

    public void setZodSchema(String zodSchema) {
        this.zodSchema = zodSchema;
    }

    public Map<String, Object> getDummyPayload() {
        return dummyPayload;
    }

    public void setDummyPayload(Map<String, Object> dummyPayload) {
        this.dummyPayload = dummyPayload;
    }
}
