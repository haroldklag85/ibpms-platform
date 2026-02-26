package com.ibpms.poc.application.dto;

public class DmnTranslationResponseDTO {
    private String dmnXml;

    public DmnTranslationResponseDTO(String dmnXml) {
        this.dmnXml = dmnXml;
    }

    public String getDmnXml() {
        return dmnXml;
    }

    public void setDmnXml(String dmnXml) {
        this.dmnXml = dmnXml;
    }
}
