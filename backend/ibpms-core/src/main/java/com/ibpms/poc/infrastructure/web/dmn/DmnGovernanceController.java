package com.ibpms.poc.infrastructure.web.dmn;

import com.ibpms.poc.application.usecase.dmn.DmnGovernanceUseCase;
import com.ibpms.poc.application.util.SecurityContextUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * Adapter-In (Controller) protegido por la directiva de AppSec para el mantenimiento DMN.
 * @Traceability: US-007 - Generador Cognitivo de DMN (NLP a Tablas de Decisión)
 */
@RestController
@RequestMapping("/api/v1/dmn-models")
@Tag(name = "DMN Governance", description = "Gobernanza de Modelos DMN, persistencia y ciclo de vida")
@Traceability(US = "US-007", CA = {"CA-13", "CA-14"})
public class DmnGovernanceController {

    private final DmnGovernanceUseCase dmnGovernanceUseCase;

    public DmnGovernanceController(DmnGovernanceUseCase dmnGovernanceUseCase) {
        this.dmnGovernanceUseCase = dmnGovernanceUseCase;
    }

    /**
     * CA-06: Enforcing Rol `ROLE_PROCESS_ARCHITECT`. Restringe la edición agresivamente.
     */
    @Operation(summary = "Sobreescribe Borrador DMN", description = "CA-06: Restringe la edición a ROLE_PROCESS_ARCHITECT")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_PROCESS_ARCHITECT')")
    @com.ibpms.poc.crosscutting.annotations.Traceability(US = "US-007", CA = {"CA-32", "CA-06"})
    public ResponseEntity<?> overrideDmnDraft(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        String invokerTenant = SecurityContextUtils.getTenantId();
        
        String incomingXml = (String) payload.get("xmlContent");
        boolean isManual = payload.containsKey("isManual") ? (Boolean) payload.get("isManual") : false;

        var result = dmnGovernanceUseCase.updateDmnContent(id, incomingXml, invokerTenant, isManual);
        return ResponseEntity.ok(result);
    }

    /**
     * @Traceability: US-007 - Generador Cognitivo de DMN (NLP a Tablas de Decisión)
     * CA-12: Retroceso explícito de estados Draft a V1 Activa.
     */
    @Operation(summary = "Rollback de DMN", description = "Retrocede de estado Draft a V1 Activa")
    @PostMapping("/{id}/rollback")
    @PreAuthorize("hasAuthority('ROLE_PROCESS_ARCHITECT')")
    public ResponseEntity<Void> rollbackDmnDraft(@PathVariable String id) {
        String invokerTenant = SecurityContextUtils.getTenantId(); 
        dmnGovernanceUseCase.rollbackDraft(id, invokerTenant);
        return ResponseEntity.ok().build();
    }

    /**
     * @Traceability: US-007 - Generador Cognitivo de DMN (NLP a Tablas de Decisión)
     * CA-13: Catálogo DMN Paginado
     */
    @Operation(summary = "Catálogo DMN", description = "Lista todos los DMN bajo el tenant actual")
    @GetMapping("/catalog")
    public ResponseEntity<?> getDmnCatalog(@RequestParam(defaultValue = "0") int page, 
                                           @RequestParam(defaultValue = "10") int size) {
        String invokerTenant = SecurityContextUtils.getTenantId();
        return ResponseEntity.ok(dmnGovernanceUseCase.getDmnCatalog(invokerTenant, page, size));
    }

    /**
     * @Traceability: US-007 - Generador Cognitivo de DMN (NLP a Tablas de Decisión)
     * CA-14: Obtener detalle de un DMN específico
     */
    @Operation(summary = "Obtiene un DMN", description = "Retorna el XML y metadatos de un DMN específico")
    @GetMapping("/{id}")
    public ResponseEntity<?> getDmnById(@PathVariable String id) {
        String invokerTenant = SecurityContextUtils.getTenantId();
        return ResponseEntity.ok(dmnGovernanceUseCase.getDmnById(id, invokerTenant));
    }

    /**
     * CA-15: Endpoint simulador funcional (Fallback mode available en backend)
     */
    @Operation(summary = "Simulación DMN (Fallback)", description = "Punto de simulación con Fallback DB")
    @PostMapping("/simulate")
    public ResponseEntity<?> simulateDmnExecution(@RequestBody Map<String, Object> variables) {
        String invokerTenant = SecurityContextUtils.getTenantId();
        return ResponseEntity.ok(dmnGovernanceUseCase.simulateDmnExecution(variables, invokerTenant));
    }

    /**
     * @Traceability: US-007 - Generador Cognitivo de DMN (NLP a Tablas de Decisión)
     * B-20: Lista las tablas DMN publicadas en el motor Camunda.
     * Consumido por BpmnDesigner (Frontend) para dropdown visual de decisionRef.
     */
    @Operation(summary = "Lista Definiciones en Engine", description = "Lista DMN activas en motor para Dropdowns de Diseño BPMN")
    @GetMapping("/definitions")
    public ResponseEntity<?> listDeployedDmnDefinitions() {
        String invokerTenant = SecurityContextUtils.getTenantId();
        var definitions = dmnGovernanceUseCase.listDeployedDecisionDefinitions(invokerTenant);
        return ResponseEntity.ok(definitions);
    }
}
