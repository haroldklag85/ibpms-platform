package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.AuditReportEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.AuditReportRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditReportService {

    private static final Logger log = LoggerFactory.getLogger(AuditReportService.class);
    private final UserRepository userRepository;
    private final AuditReportRepository auditReportRepository;

    public AuditReportService(UserRepository userRepository, AuditReportRepository auditReportRepository) {
        this.userRepository = userRepository;
        this.auditReportRepository = auditReportRepository;
    }

    public List<AuditReportEntity> getAllReports() {
        return auditReportRepository.findAll();
    }

    public ReportResult generateIso27001Report(String adminUser) {
        log.info("CA-16: Iniciando generación de reporte ISO 27001 en Application Service.");
        List<UserEntity> users = userRepository.findAll();
        ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(contentBuffer, StandardCharsets.UTF_8))) {
            writer.write("User_ID;Email;Status;Roles;Permissions_Count;Initiatable_Processes");
            writer.newLine();

            for (UserEntity user : users) {
                String roles = "";
                int permCount = 0;
                String processes = "";
                
                if (user.getRoles() != null) {
                    roles = user.getRoles().stream()
                            .map(r -> r.getName())
                            .collect(Collectors.joining("|"));
                    
                    permCount = user.getRoles().stream()
                            .mapToInt(r -> r.getProcessPermissions() != null ? r.getProcessPermissions().size() : 0)
                            .sum();
                            
                    processes = user.getRoles().stream()
                            .filter(r -> r.getProcessPermissions() != null)
                            .flatMap(r -> r.getProcessPermissions().stream())
                            .filter(p -> Boolean.TRUE.equals(p.getCanInitiateProcess()))
                            .map(p -> p.getProcessDefinitionKey())
                            .distinct()
                            .collect(Collectors.joining("|"));
                }

                writer.write(String.format("%s;%s;%s;%s;%d;%s",
                        user.getUsername() != null ? user.getUsername() : "",
                        user.getEmail() != null ? user.getEmail() : "",
                        user.getStatus() != null ? user.getStatus() : "",
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

            log.info("CA-16: Reporte generado y firmado exitosamente. Hash: {}", fileHash);
            
            return new ReportResult(content, fileHash);
            
        } catch (Exception e) {
            log.error("Error crítico generando reporte ISO 27001", e);
            throw new RuntimeException("Error en generación de reporte", e);
        }
    }

    public static class ReportResult {
        private final byte[] content;
        private final String sha256Hash;

        public ReportResult(byte[] content, String sha256Hash) {
            this.content = content;
            this.sha256Hash = sha256Hash;
        }

        public byte[] getContent() {
            return content;
        }

        public String getSha256Hash() {
            return sha256Hash;
        }
    }
}
