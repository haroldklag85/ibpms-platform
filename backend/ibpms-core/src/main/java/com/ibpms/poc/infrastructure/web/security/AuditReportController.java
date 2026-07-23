package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.AuditReportEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de Reportes de Auditoría ISO 27001 (CA-16).
 * Genera matrices de accesos con sellado de integridad SHA-256.
 */
@RestController
@RequestMapping("/api/v1/security/audit/reports")
public class AuditReportController {

    private static final Logger log = LoggerFactory.getLogger(AuditReportController.class);
    private final com.ibpms.poc.application.service.security.AuditReportService auditReportService;

    public AuditReportController(com.ibpms.poc.application.service.security.AuditReportService auditReportService) {
        this.auditReportService = auditReportService;
    }

    @GetMapping
    public ResponseEntity<List<AuditReportEntity>> getAllReports() {
        log.info("Fetching all audit reports");
        return ResponseEntity.ok(auditReportService.getAllReports());
    }

    // @Traceability(US="US-036", CA="CA-16", DESC="ADR-001: Lógica legacy de HEAD erradicada. Generación ISO delegada a AuditReportService.")
    @org.springframework.web.bind.annotation.PostMapping("/iso27001")
    public ResponseEntity<byte[]> downloadIso27001Report() {
        log.info("CA-16: Solicitando generación de reporte ISO 27001.");

        String adminUser = SecurityContextHolder.getContext().getAuthentication() != null ? 
                           SecurityContextHolder.getContext().getAuthentication().getName() : "SYSTEM";

        com.ibpms.poc.application.service.security.AuditReportService.ReportResult result = auditReportService.generateIso27001Report(adminUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ibpms_iso27001_report.csv\"")
                .header("X-Report-Hash", "sha256:" + result.getSha256Hash()) 
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(result.getContent());
    }
}
