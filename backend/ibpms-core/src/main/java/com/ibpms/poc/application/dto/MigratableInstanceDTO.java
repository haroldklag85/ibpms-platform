package com.ibpms.poc.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MigratableInstanceDTO {
    private String instanceId;
    private boolean isMigratable;
    private String reason;
}
