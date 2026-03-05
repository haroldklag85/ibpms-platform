package com.ibpms.core.sac.dto;

import lombok.Data;

@Data
public class CreateSacMailboxDTO {
    private String alias;
    private String tenantId;
    private String clientId;
    private String clientSecret; // Este se enviará al KeyVault, NO se guarda en DB.
    private String protocol;
    private String defaultBpmnProcessId;
}
