package com.ibpms.core.sac.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SacMailboxDTO {
    private String id;
    private String alias;
    private String tenantId;
    private String clientId;
    private String protocol;
    private boolean isActive;
    private String defaultBpmnProcessId;
    private LocalDateTime createdAt;
}
