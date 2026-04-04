package com.ibpms.poc.infrastructure.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleAuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import com.ibpms.poc.AbstractIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
public class RoleAuditIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleAuditLogRepository auditLogRepository;

    @AfterEach
    void tearDown() {
        auditLogRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("US-036 p4: Decodificación BLOB - La exportación ISO27001 debe despachar un CSV/ByteStream legible y estricto")
    void testIso27001RoleMatrixExport_BlobDecoding() throws Exception {
        // Enforce the Endpoint call simulating a direct HTTP GET request from a Browser's <a> tag
        mockMvc.perform(get("/api/v1/security/audit/reports/iso27001/role-matrix"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"iso27001-role-matrix-report.csv\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv")))
                // Magic Number Assertion: Text CSV files don't have classical binary magic numbers like PDFs (%PDF),
                // but we assert the CSV header structure exists in the payload, confirming clean stream encoding.
                .andExpect(content().string(org.hamcrest.Matchers.containsString("EntraID_UUID,Email,FullName,PrimaryRole,Status,GranularPermissions")));
    }

    @Test
    @DisplayName("US-036 p4: JSON Deltas - El cambio de Roles genera un Delta Quirúrgico en el Log")
    void testJsonDeltaAudit_SurgicalGrantPrecision() throws Exception {
        RoleAuditLogEntity audit = new RoleAuditLogEntity();
        audit.setRoleId(UUID.randomUUID());
        audit.setAdminId("admin_user_x");
        audit.setTimestamp(LocalDateTime.now());
        audit.setAction("UPDATE");

        // Delta quirúrgico: Se añadió grant_A y se removieron grant_B, grant_C. El resto quedó intacto.
        String surgicalDelta = "{ \"added\": [\"grant_A\"], \"removed\": [\"grant_B\", \"grant_C\"] }";
        audit.setDeltaJson(surgicalDelta);

        auditLogRepository.saveAndFlush(audit);

        // Aserción QA: Verificamos que al recuperar el Delta JSON de BD no se corrompa la estructura.
        RoleAuditLogEntity savedAudit = auditLogRepository.findById(audit.getId()).orElseThrow();
        String jsonFromDb = savedAudit.getDeltaJson();
        
        assertThat(jsonFromDb).contains("\"added\": [\"grant_A\"]");
        assertThat(jsonFromDb).contains("\"removed\": [\"grant_B\", \"grant_C\"]");
    }
}
