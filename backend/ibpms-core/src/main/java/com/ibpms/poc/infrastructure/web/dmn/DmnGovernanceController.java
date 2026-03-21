package com.ibpms.poc.infrastructure.web.dmn;

import com.ibpms.poc.application.usecase.dmn.DmnGovernanceUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Adapter-In (Controller) protegido por la directiva de AppSec para el mantenimiento DMN.
 */
@RestController
@RequestMapping("/api/v1/dmn-models")
public class DmnGovernanceController {

    private final DmnGovernanceUseCase dmnGovernanceUseCase;

    public DmnGovernanceController(DmnGovernanceUseCase dmnGovernanceUseCase) {
        this.dmnGovernanceUseCase = dmnGovernanceUseCase;
    }

    /**
     * CA-06: Enforcing Rol `ROLE_PROCESS_ARCHITECT`. Restringe la edición agresivamente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_PROCESS_ARCHITECT')")
    public ResponseEntity<?> overrideDmnDraft(@PathVariable String id, @RequestBody Map<String, String> payload) {
        // En V1 extraemos el Tenant ID estáticamente por falta de Contexto Multi-Tenant Nativo en Spring PoC
        // Normalmente esto vendría de `SecurityContextHolder.getContext().getAuthentication().getDetails()`
        String dummyInvokerTenant = "tenant_hq_corp"; 
        
        String incomingXml = payload.get("xmlContent");

        var result = dmnGovernanceUseCase.updateDmnContent(id, incomingXml, dummyInvokerTenant);
        return ResponseEntity.ok(result);
    }

    /**
     * CA-12: Retroceso explícito de estados Draft a V1 Activa.
     */
    @PostMapping("/{id}/rollback")
    @PreAuthorize("hasAuthority('ROLE_PROCESS_ARCHITECT')")
    public ResponseEntity<Void> rollbackDmnDraft(@PathVariable String id) {
        String dummyInvokerTenant = "tenant_hq_corp"; 
        dmnGovernanceUseCase.rollbackDraft(id, dummyInvokerTenant);
        return ResponseEntity.ok().build();
    }
}
