package com.ibpms.poc.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "ibpms.claim")
public class ClaimProperties {
    
    private int ghostTimeout = 240; // Default 240 min (4 horas)
    private Map<String, Integer> tenantOverrides = new HashMap<>();

    public int getGhostTimeout() {
        return ghostTimeout;
    }

    public void setGhostTimeout(int ghostTimeout) {
        this.ghostTimeout = ghostTimeout;
    }

    public Map<String, Integer> getTenantOverrides() {
        return tenantOverrides;
    }

    public void setTenantOverrides(Map<String, Integer> tenantOverrides) {
        this.tenantOverrides = tenantOverrides;
    }
    
    public int getTimeoutForTenant(String tenantId) {
        return tenantOverrides.getOrDefault(tenantId, ghostTimeout);
    }
}
