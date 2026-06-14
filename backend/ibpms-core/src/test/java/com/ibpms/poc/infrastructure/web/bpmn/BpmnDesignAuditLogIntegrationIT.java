// @Traceability: US-005 - ADR-001
package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationIT;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnDesignAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class BpmnDesignAuditLogIntegrationIT extends AbstractIntegrationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BpmnProcessDesignRepository processDesignRepository;

    @Autowired
    private BpmnDesignAuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    private String token;
    private String processKey;
    private UUID designId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/processes";
        this.token = jwtTokenProvider.generateToken("BPMN_Release_Manager", Arrays.asList("ibpms_rol_BPMN_Release_Manager"), "tenant_alpha");

        // Asegurar que el usuario de prueba existe para evitar error 428 Precondition Required
        userRepository.findByUsername("BPMN_Release_Manager").ifPresent(user -> {
            user.setManager(null);
            user.getRoles().clear();
            userRepository.save(user);
            userRepository.delete(user);
        });

        UserEntity user = new UserEntity();
        user.setUsername("BPMN_Release_Manager");
        user.setEmail("bpmn_release_manager@sso.local");
        user.setStatus(UserStatus.ACTIVE);
        user.setIsExternalIdp(false);
        user.setMustChangePassword(false);
        userRepository.save(user);

        // Limpiar base de datos
        auditLogRepository.deleteAll();
        processDesignRepository.deleteAll();

        // Crear datos de prueba
        this.processKey = "test_process_" + UUID.randomUUID().toString().substring(0, 8);
        this.designId = UUID.randomUUID();

        BpmnProcessDesignEntity processDesign = new BpmnProcessDesignEntity();
        processDesign.setId(designId);
        processDesign.setName("Test Process Name");
        processDesign.setTechnicalId(processKey);
        processDesign.setStatus(BpmnProcessDesignEntity.Status.DRAFT);
        processDesign.setFormPattern(BpmnProcessDesignEntity.FormPattern.SIMPLE);
        processDesign.setCurrentVersion(1);
        processDesign.setMaxNodes(50);
        processDesign.setCreatedAt(LocalDateTime.now());
        processDesign.setUpdatedAt(LocalDateTime.now());
        processDesign.setCreatedBy("admin");
        processDesignRepository.save(processDesign);

        // Crear logs de auditoría en la DB
        LocalDateTime now = LocalDateTime.of(2026, 6, 6, 12, 0, 0);

        BpmnDesignAuditLogEntity log1 = new BpmnDesignAuditLogEntity(
            designId,
            BpmnDesignAuditLogEntity.Action.SAVE_DRAFT,
            "Harolt Gómez",
            1,
            "{\"event\":\"DRAFT_SAVED\"}"
        );
        log1.setTimestamp(now);
        auditLogRepository.save(log1);

        BpmnDesignAuditLogEntity log2 = new BpmnDesignAuditLogEntity(
            designId,
            BpmnDesignAuditLogEntity.Action.REQUEST_DEPLOY,
            "Ana García",
            2,
            "{\"event\":\"DEPLOY_REQUESTED\"}"
        );
        log2.setTimestamp(now.plusMinutes(15));
        auditLogRepository.save(log2);
    }

    @Test
    void testGetBpmnAuditLogsReturnsDatabaseDataMappedForFrontend() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/" + processKey + "/audit-logs")
        .then()
            .statusCode(200)
            .body("size()", is(2))
            // El log2 es el más reciente y debe ser el primero (Orden descendente por timestamp)
            .body("[0].action", equalTo("REQUEST DEPLOY"))
            .body("[0].user", equalTo("Ana García"))
            .body("[0].userId", equalTo("Ana García"))
            .body("[0].version", is(2))
            .body("[0].versionAffected", is(2))
            .body("[0].timestamp", containsString("2026-06-06T12:15:00"))
            
            // El log1 es el más antiguo y debe ser el segundo
            .body("[1].action", equalTo("IMPORT XML")) // SAVE_DRAFT mapea a "IMPORT XML"
            .body("[1].user", equalTo("Harolt Gómez"))
            .body("[1].userId", equalTo("Harolt Gómez"))
            .body("[1].version", is(1))
            .body("[1].versionAffected", is(1))
            .body("[1].timestamp", containsString("2026-06-06T12:00:00"));
    }

    @Test
    void testGetCatalogWithoutFilterReturnsAllProcesses() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/catalog")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("find { it.key == '" + processKey + "' }.status", equalTo("DRAFT"));
    }

    @Test
    void testGetCatalogFilteredByActiveStatusReturnsOnlyActive() {
        // Al consultar ACTIVE, nuestro proceso DRAFT no debe aparecer
        given()
            .header("Authorization", "Bearer " + token)
            .queryParam("status", "ACTIVE")
        .when()
            .get("/catalog")
        .then()
            .statusCode(200)
            .body("find { it.key == '" + processKey + "' }", nullValue());

        // Insertar un proceso ACTIVE
        UUID activeDesignId = UUID.randomUUID();
        String activeProcessKey = "active_process_" + UUID.randomUUID().toString().substring(0, 8);
        BpmnProcessDesignEntity activeProcess = new BpmnProcessDesignEntity();
        activeProcess.setId(activeDesignId);
        activeProcess.setName("Active Process Name");
        activeProcess.setTechnicalId(activeProcessKey);
        activeProcess.setStatus(BpmnProcessDesignEntity.Status.ACTIVE);
        activeProcess.setFormPattern(BpmnProcessDesignEntity.FormPattern.SIMPLE);
        activeProcess.setCurrentVersion(1);
        activeProcess.setMaxNodes(50);
        activeProcess.setCreatedAt(LocalDateTime.now());
        activeProcess.setUpdatedAt(LocalDateTime.now());
        activeProcess.setCreatedBy("admin");
        processDesignRepository.save(activeProcess);

        // Ahora al consultar ACTIVE, debe aparecer únicamente el proceso ACTIVE
        try {
            given()
                .header("Authorization", "Bearer " + token)
                .queryParam("status", "ACTIVE")
            .when()
                .get("/catalog")
            .then()
                .statusCode(200)
                .body("find { it.key == '" + activeProcessKey + "' }", notNullValue())
                .body("find { it.key == '" + activeProcessKey + "' }.status", equalTo("ACTIVE"))
                .body("find { it.key == '" + processKey + "' }", nullValue());
        } finally {
            // Limpieza específica
            processDesignRepository.deleteById(activeDesignId);
        }
    }
}
