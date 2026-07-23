// @Traceability: US-001, CA-29 Contadores de Facetas
package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.AbstractIntegrationIT;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Workdesk Facets CA-29 Integration Test")
public class WorkdeskFacetsCA29IntegrationIT extends AbstractIntegrationIT {

    @Autowired
    private WorkdeskProjectionRepository workdeskProjectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.ibpms.poc.infrastructure.jpa.repository.security.DelegationRepository delegationRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String token;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;

        // Limpiar el repositorio de delegaciones, proyecciones y el repositorio de usuarios
        delegationRepository.deleteAll();
        workdeskProjectionRepository.deleteAll();
        userRepository.findAll().forEach(user -> {
            user.setManager(null);
            user.getRoles().clear();
            userRepository.save(user);
        });
        userRepository.deleteAll();

        // Crear un usuario "BPMN_Release_Manager" en la base de datos
        UserEntity user = new UserEntity();
        user.setUsername("BPMN_Release_Manager");
        user.setEmail("bpmn_release_manager@sso.local");
        user.setStatus(UserStatus.ACTIVE);
        user.setIsExternalIdp(false);
        user.setMustChangePassword(false);
        userRepository.save(user);

        // Generar un token JWT
        token = jwtTokenProvider.generateToken(
                "BPMN_Release_Manager",
                Arrays.asList("ibpms_rol_BPMN_Release_Manager"),
                "tenant_alpha"
        );
    }

    @Test
    @DisplayName("Debe retornar los contadores de facetas agrupados por origen y estado")
    public void shouldReturnFacetsCounters() {
        // Insertar 4 tareas en el repositorio con tenantId = "tenant_alpha"
        WorkdeskProjectionEntity t1 = new WorkdeskProjectionEntity();
        t1.setId("task_f1");
        t1.setSourceSystem("BPMN");
        t1.setStatus("PENDING");
        t1.setOriginalTaskId("orig_f1");
        t1.setTitle("T1");
        t1.setImpactLevel(5);
        t1.setAssignee(null);
        t1.setTenantId("tenant_alpha");
        workdeskProjectionRepository.save(t1);

        WorkdeskProjectionEntity t2 = new WorkdeskProjectionEntity();
        t2.setId("task_f2");
        t2.setSourceSystem("BPMN");
        t2.setStatus("PENDING");
        t2.setOriginalTaskId("orig_f2");
        t2.setTitle("T2");
        t2.setImpactLevel(5);
        t2.setAssignee(null);
        t2.setTenantId("tenant_alpha");
        workdeskProjectionRepository.save(t2);

        WorkdeskProjectionEntity t3 = new WorkdeskProjectionEntity();
        t3.setId("task_f3");
        t3.setSourceSystem("KANBAN");
        t3.setStatus("IN_PROGRESS");
        t3.setOriginalTaskId("orig_f3");
        t3.setTitle("T3");
        t3.setImpactLevel(5);
        t3.setAssignee(null);
        t3.setTenantId("tenant_alpha");
        workdeskProjectionRepository.save(t3);

        WorkdeskProjectionEntity t4 = new WorkdeskProjectionEntity();
        t4.setId("task_f4");
        t4.setSourceSystem("KANBAN");
        t4.setStatus("OVERDUE");
        t4.setOriginalTaskId("orig_f4");
        t4.setTitle("T4");
        t4.setImpactLevel(5);
        t4.setAssignee(null);
        t4.setTenantId("tenant_alpha");
        workdeskProjectionRepository.save(t4);

        // Realizar la petición GET /api/v1/workdesk/global-inbox
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/v1/workdesk/global-inbox")
        .then()
            .log().all()
            .statusCode(200)
            .body("facets.origin.BPMN", equalTo(2))
            .body("facets.origin.KANBAN", equalTo(2))
            .body("facets.status.PENDING", equalTo(2))
            .body("facets.status.IN_PROGRESS", equalTo(1))
            .body("facets.status.OVERDUE", equalTo(1));
    }
}
