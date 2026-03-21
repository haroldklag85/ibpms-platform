package com.ibpms.poc.infrastructure.web.ai;

import com.ibpms.poc.application.usecase.ai.ForensicIsoOverrideUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CA-09: Endpoint Pozo-Negro (Write-Only) para Trazabilidad Legal ISO 9001.
 */
@RestController
@RequestMapping("/api/v1/forensics")
public class ForensicController {

    private final ForensicIsoOverrideUseCase forensicUseCase;

    public ForensicController(ForensicIsoOverrideUseCase forensicUseCase) {
        this.forensicUseCase = forensicUseCase;
    }

    /**
     * Absorbe de forma inmutable la responsabilidad del arquitecto
     * al ignorar advertencias de calidad emitidas por la IA.
     */
    @PostMapping("/iso-override")
    @PreAuthorize("hasAuthority('ROLE_PROCESS_ARCHITECT')")
    public ResponseEntity<Void> acceptLiabilityForOverride(@RequestBody Map<String, Object> payload) {
        // En V1 la Identidad del infractor es extraída rigurosamente del JWT (No del Body)
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userId == null || userId.isBlank()) {
            userId = "ANONYMOUS_ARCHITECT";
        }

        forensicUseCase.recordInfraction(userId, payload);
        return ResponseEntity.ok().build();
    }
}
