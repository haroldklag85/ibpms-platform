package com.ibpms.poc.application.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class WorkdeskResponseDTO {
    private boolean degraded;
    private Page<WorkdeskGlobalItemDTO> content;

    private DelegationContextDTO delegationContext;

    public WorkdeskResponseDTO(boolean degraded, Page<WorkdeskGlobalItemDTO> content) {
        this.degraded = degraded;
        this.content = content;
    }

    public WorkdeskResponseDTO(boolean degraded, Page<WorkdeskGlobalItemDTO> content, DelegationContextDTO delegationContext) {
        this.degraded = degraded;
        this.content = content;
        this.delegationContext = delegationContext;
    }
}
