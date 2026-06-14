package com.ibpms.poc.application.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class WorkdeskResponseDTO {
    private boolean degraded;
    private java.util.List<WorkdeskGlobalItemDTO> data;
    private PaginationDTO pagination;
    private DelegationContextDTO delegationContext;

    // @Traceability: US-001, CA-29 Contadores de Facetas
    private java.util.Map<String, java.util.Map<String, Long>> facets;

    public WorkdeskResponseDTO(boolean degraded, Page<WorkdeskGlobalItemDTO> pageData) {
        this.degraded = degraded;
        this.data = pageData.getContent();
        this.pagination = new PaginationDTO(
            pageData.getTotalElements(),
            pageData.getTotalPages(),
            pageData.getSize(),
            pageData.getNumber()
        );
    }

    public WorkdeskResponseDTO(boolean degraded, Page<WorkdeskGlobalItemDTO> pageData, DelegationContextDTO delegationContext) {
        this(degraded, pageData);
        this.delegationContext = delegationContext;
    }

    @Data
    public static class PaginationDTO {
        private long totalElements;
        private int totalPages;
        private int size;
        private int page;

        public PaginationDTO(long totalElements, int totalPages, int size, int page) {
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.size = size;
            this.page = page;
        }
    }
}
