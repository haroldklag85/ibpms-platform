package com.ibpms.poc.application.dto;

import lombok.Data;

@Data
public class FormFieldMetadataDTO {
    private String camundaVariable;
    private String type;
    private String zodRule;
    private Integer columns;     // CA-55: Layout Grid Span
    private String timerMode;    // CA-58: Configuración focal o stop-watch
}
