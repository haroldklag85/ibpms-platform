package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.port.in.AuditLogUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditLogController {

    private final AuditLogUseCase auditUseCase;

    public AuditLogController(AuditLogUseCase auditUseCase) {
        this.auditUseCase = auditUseCase;
    }

    @GetMapping("/cases/{caseId}/diff")
    public ResponseEntity<String> getCaseChanges(
            @PathVariable String caseId,
            // Permite variar entre consultar el Task o el Expediente
            @RequestParam(defaultValue = "ExpedienteEntity") String entityType) {
        String diffJson = auditUseCase.getEntityDiffHistory(entityType, caseId);
        return ResponseEntity.ok(diffJson);
    }
}
