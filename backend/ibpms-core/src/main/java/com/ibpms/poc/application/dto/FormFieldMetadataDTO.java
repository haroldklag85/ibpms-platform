package com.ibpms.poc.application.dto;

import lombok.Data;

@Data
public class FormFieldMetadataDTO {
    private String camundaVariable;
    private String type;
    private String zodRule;
    private Integer columns;     // CA-55: Layout Grid Span
    private String timerMode;    // CA-58: Configuración focal o stop-watch
    
    // @Traceability: US-005, CA-39
    private String stage;
}
