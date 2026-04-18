package com.ibpms.poc.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmitResponse {
    private String eventReference;
    private String status; // EJ: SUCCESS, SAGA_COMPENSATION_EXECUTED
    private String message;
}
