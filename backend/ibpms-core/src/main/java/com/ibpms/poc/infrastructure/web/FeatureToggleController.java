package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.FeatureToggleService;
import com.ibpms.poc.application.util.SecurityContextUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/toggles")
public class FeatureToggleController {

    private final FeatureToggleService featureToggleService;

    public FeatureToggleController(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> getToggle(@PathVariable String key) {
        String tenantId = SecurityContextUtils.getTenantId();
        boolean isEnabled = featureToggleService.isFeatureEnabled(tenantId, key);
        return ResponseEntity.ok(Map.of("enabled", isEnabled));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> setToggle(@PathVariable String key, @RequestBody Map<String, Boolean> payload) {
        String tenantId = SecurityContextUtils.getTenantId();
        String username = SecurityContextUtils.getAssignee();
        
        Boolean enabled = payload.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().build();
        }

        featureToggleService.setFeatureToggle(tenantId, key, enabled, username);
        return ResponseEntity.noContent().build();
    }
}
