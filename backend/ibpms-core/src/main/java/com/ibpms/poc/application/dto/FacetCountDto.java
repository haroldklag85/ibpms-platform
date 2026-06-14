package com.ibpms.poc.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacetCountDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String status;
    private Long count;

    public String getStatusName() {
        if ("ACTIVE".equalsIgnoreCase(status)) return "Activos";
        if ("COMPLETED".equalsIgnoreCase(status)) return "Completados";
        if ("DRAFT".equalsIgnoreCase(status)) return "Borradores";
        if ("SUSPENDED".equalsIgnoreCase(status)) return "Suspendidos";
        return status;
    }
}
