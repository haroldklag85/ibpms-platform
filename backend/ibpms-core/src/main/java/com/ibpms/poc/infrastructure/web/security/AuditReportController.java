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
@RequestMapping("/api/v1/admin/security/reports")
public class AuditReportController {

    private static final Logger log = LoggerFactory.getLogger(AuditReportController.class);
    private final UserRepository userRepository;
    private final AuditReportRepository auditReportRepository;

    public AuditReportController(UserRepository userRepository, AuditReportRepository auditReportRepository) {
        this.userRepository = userRepository;
        this.auditReportRepository = auditReportRepository;
    }

    @GetMapping("/iso27001")
    public ResponseEntity<StreamingResponseBody> downloadIso27001Report() {
        log.info("CA-16: Iniciando generación de reporte ISO 27001.");

        List<UserEntity> users = userRepository.findAll();
        String adminUser = SecurityContextHolder.getContext().getAuthentication() != null ? 
                           SecurityContextHolder.getContext().getAuthentication().getName() : "SYSTEM";

        StreamingResponseBody stream = out -> {
            ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(contentBuffer, StandardCharsets.UTF_8))) {
                
                // Cabecera CSV (Separador ; para compatibilidad Excel Regional)
                writer.write("User_ID;Email;Status;Roles;Permissions_Count;Initiatable_Processes");
                writer.newLine();

                for (UserEntity user : users) {
                    String roles = user.getRoles().stream()
                            .map(r -> r.getName())
                            .collect(Collectors.joining("|"));
                    
                    int permCount = user.getRoles().stream()
                            .mapToInt(r -> r.getProcessPermissions().size())
                            .sum();

                    String processes = user.getRoles().stream()
                            .flatMap(r -> r.getProcessPermissions().stream())
                            .filter(p -> p.getCanInitiateProcess())
                            .map(p -> p.getProcessDefinitionKey())
                            .distinct()
                            .collect(Collectors.joining("|"));

                    writer.write(String.format("%s;%s;%s;%s;%d;%s",
                            user.getUsername(),
                            user.getEmail(),
                            user.getStatus(),
                            roles,
                            permCount,
                            processes
                    ));
                    writer.newLine();
                }
                writer.flush();
                
                byte[] content = contentBuffer.toByteArray();
                
                // CA-16: Cálculo de Integridad SHA-256
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = digest.digest(content);
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                String fileHash = hexString.toString();

                // CA-16: Registro de Telemetría Forense
                AuditReportEntity audit = new AuditReportEntity(
                        "ISO_27001_ACCESS_MATRIX",
                        adminUser,
                        fileHash,
                        "{\"userCount\":" + users.size() + "}"
                );
                auditReportRepository.save(audit);

                log.info("CA-16: Reporte generado exitosamente. Hash: {}", fileHash);
                
                // Escribir al output stream real
                out.write(content);
            } catch (Exception e) {
                log.error("Error crítico generando reporte ISO 27001", e);
                throw new RuntimeException("Error en generación de reporte", e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ibpms_iso27001_report.csv\"")
                .header("X-Report-Hash", "sha256:" + "PENDING") // El hash se calcula en el stream, no puede ir en el header de forma síncrona fácilmente con StreamingResponseBody sin buffers previos
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(stream);
    }
}
