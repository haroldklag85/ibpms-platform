package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.infrastructure.jpa.entity.FeatureToggleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FeatureToggleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/workdesk/feature-toggles")
@Traceability(US = "US-001", CA = {"CA-15"})
public class FeatureToggleController {

    private final FeatureToggleRepository featureToggleRepository;

    public FeatureToggleController(FeatureToggleRepository featureToggleRepository) {
        this.featureToggleRepository = featureToggleRepository;
    }

    @GetMapping("/{key}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFeatureToggle(@PathVariable String key, Authentication authentication) {
        // Fallback or mapping for POC tenant
        String tenantId = (authentication != null && authentication.getName() != null) ? authentication.getName() : "default";

        Optional<FeatureToggleEntity> toggle = featureToggleRepository.findByTenantIdAndToggleKey(tenantId, key);
        
        if (toggle.isPresent()) {
            return ResponseEntity.ok(Map.of("enabled", toggle.get().getEnabled()));
        }

        // Return a default disabled object instead of throwing 404 to avoid frontend dispatch errors
        return ResponseEntity.ok(Map.of("enabled", false));
    }
}
