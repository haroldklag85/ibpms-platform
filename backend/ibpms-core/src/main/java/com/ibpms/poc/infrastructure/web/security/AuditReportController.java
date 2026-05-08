package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.AuditReportEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.AuditReportRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

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
