package com.ibpms.poc.infrastructure.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/admin/reports/iso27001")
@Tag(name = "Reporte CISO ISO 27001", description = "Generación on-demand de reportes de cumplimiento (CA-24)")
@PreAuthorize("hasRole('ADMIN_IT')")
public class Iso27001ReportController {

    private static final Logger log = LoggerFactory.getLogger(Iso27001ReportController.class);

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.getName() != null) ? authentication.getName() : "SYSTEM";
    }

    @GetMapping("/generate")
    @Operation(summary = "Generar Reporte de Matriz de Accesos (Identity Governance)")
    public ResponseEntity<byte[]> generateReport() {
        String actor = getCurrentUser();
        String timestampUtc = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        
        // Simulación: Generar CSV de usuarios vs roles
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("UsuarioID,Rol,Permisos_Efectivos,Fecha_Asignacion\n");
        csvBuilder.append("usr_juan,Gerente_Financiero,APPROVE_PAYMENT|READ_ALL,2026-01-10T12:00:00Z\n");
        csvBuilder.append("sys_bot_erp,API_Service_Account,INITIATE_PROC,2026-02-15T08:00:00Z\n");
        
        String csvContent = csvBuilder.toString();
        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
        
        String hash = computeHash(csvContent);
        
        log.warn("SUDO INVOKE [Audit Trail]: Reporte ISO 27001 (Identity Governance) generado on-demand por {}. Hash de Integridad: {}", actor, hash);
        
        // En un entorno de producción, insertamos en ibpms_audit_reports
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "iso27001_report_" + timestampUtc + ".csv");
        headers.add("X-Report-Hash-SHA256", hash);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
    
    private String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Critial Error: Hash alg", e);
            return "ERROR_NO_HASH";
        }
    }
}
