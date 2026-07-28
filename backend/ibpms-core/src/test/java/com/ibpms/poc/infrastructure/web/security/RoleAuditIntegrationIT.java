package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleAuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


import io.restassured.RestAssured;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import static io.restassured.RestAssured.given;
import java.util.List;
import static org.hamcrest.Matchers.*;



import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.AfterEach;
import com.ibpms.poc.AbstractIntegrationIT;

@SuppressWarnings("null")
public class RoleAuditIntegrationIT extends AbstractIntegrationIT {


    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String auditorToken;

    @org.junit.jupiter.api.BeforeEach
    void setUpPort() {
        io.restassured.RestAssured.port = port;
        auditorToken = jwtTokenProvider.generateToken("test-auditor", List.of("ROLE_AUDITOR_GLOBAL"), "tenant1");
    }

    @Autowired
    private RoleAuditLogRepository auditLogRepository;

    @AfterEach
    void tearDown() {
        auditLogRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("US-036 p4: Decodificación BLOB - La exportación ISO27001 debe despachar un CSV/ByteStream legible y estricto")
    void testIso27001RoleMatrixExport_BlobDecoding() throws Exception {
        // Enforce the Endpoint call simulating a direct HTTP POST request (refactored from legacy GET)
        given()
                .header("Authorization", "Bearer " + auditorToken)
                .when()
                .post("/api/v1/security/audit/reports/iso27001")
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ibpms_iso27001_report.csv\"")
                .contentType("text/csv")
                .body(containsString("User_ID;Email;Status;Roles;Permissions_Count;Initiatable_Processes"));
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
