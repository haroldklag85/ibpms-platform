package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.IdentityEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.IdentityRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Controlador de Reportes de Auditoría (CA-16, CA-17).
 * Provee la matriz de roles y accesos transpuesta para la ISO 27001.
 */
@RestController
@RequestMapping("/api/v1/security/audit/reports")
public class AuditReportController {

    private final IdentityRepository identityRepository;

    public AuditReportController(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @GetMapping("/iso27001/role-matrix")
    public ResponseEntity<StreamingResponseBody> downloadIso27001RoleMatrix() {

        List<IdentityEntity> users = identityRepository.findAll();

        StreamingResponseBody stream = out -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
                // Escribir cabecera CSV
                writer.write("EntraID_UUID,Email,FullName,PrimaryRole,Status,GranularPermissions");
                writer.newLine();

                // Escribir filas usando Stream para bajo consumo de memoria
                for (IdentityEntity user : users) {
                    String roleName = user.getPrimaryRole() != null ? user.getPrimaryRole().getRoleName() : "N/A";
                    String permissions = user.getPrimaryRole() != null ? user.getPrimaryRole().getGranularPermissions()
                            : "[]";
                    String status = user.getActive() ? "ACTIVE" : "INACTIVE";

                    writer.write(String.format("%s,%s,%s,%s,%s,\"%s\"",
                            user.getEntraIdObjectId(),
                            user.getEmail(),
                            user.getFullName(),
                            roleName,
                            status,
                            permissions.replace("\"", "\"\"") // Escape quotes para CSV
                    ));
                    writer.newLine();
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"iso27001-role-matrix-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(stream);
    }
}
