package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity;
import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity.Pattern;
import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity.Status;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus;
import com.ibpms.poc.infrastructure.jpa.repository.FormDesignRepository;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

// @Traceability: US-005, CA-40
@DisplayName("Form Catalog Active Pattern Filter Integration Test")
public class FormCatalogActivePatternIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FormDesignRepository formDesignRepository;

    @Autowired
    private BpmnProcessDesignRepository bpmnProcessDesignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String jwtToken;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Limpiar usuario existente si quedó de un test previo
        userRepository.findByUsername("BPMN_Release_Manager")
                .ifPresent(u -> userRepository.delete(u));

        // Crear y guardar el usuario en la DB para saltar JIT provisioning
        UserEntity user = new UserEntity();
        user.setUsername("BPMN_Release_Manager");
        user.setEmail("bpmn_release_manager@sso.local");
        user.setStatus(UserStatus.ACTIVE);
        user.setIsExternalIdp(false);
        userRepository.save(user);

        // Generar un JWT válido firmado con la clave real del perfil de test usando JwtTokenProvider
        jwtToken = jwtTokenProvider.generateToken(
                "BPMN_Release_Manager",
                Arrays.asList("ibpms_rol_BPMN_Release_Manager"),
                "T1"
        );
    }

    @AfterEach
    public void tearDown() {
        formDesignRepository.deleteAll();
        bpmnProcessDesignRepository.deleteAll();

        // Limpiar el usuario creado para el test
        userRepository.findByUsername("BPMN_Release_Manager")
                .ifPresent(u -> userRepository.delete(u));
    }

    @Test
    @DisplayName("testListActiveFormsFilteredBySimpleProcess")
    void testListActiveFormsFilteredBySimpleProcess() {
        formDesignRepository.deleteAll();
        bpmnProcessDesignRepository.deleteAll();

        // Crea y guarda un proceso BPMN en BpmnProcessDesignRepository
        BpmnProcessDesignEntity process = new BpmnProcessDesignEntity();
        process.setId(UUID.randomUUID());
        process.setTechnicalId("simple-process");
        process.setFormPattern(BpmnProcessDesignEntity.FormPattern.SIMPLE);
        process.setStatus(BpmnProcessDesignEntity.Status.DRAFT);
        process.setName("Proceso Simple");
        process.setMaxNodes(100);
        process.setCurrentVersion(1);
        process.setCreatedAt(LocalDateTime.now());
        process.setUpdatedAt(LocalDateTime.now());
        process.setCreatedBy("BPMN_Release_Manager");
        bpmnProcessDesignRepository.save(process);

        // Crea y guarda un formulario simple activo en FormDesignRepository
        FormDesignEntity f1 = new FormDesignEntity();
        f1.setId(UUID.randomUUID());
        f1.setName("Formulario Simple Activo");
        f1.setTechnicalName("frm_simple_active");
        f1.setPattern(Pattern.SIMPLE);
        f1.setStatus(Status.ACTIVE);
        f1.setFormFields("[]");
        f1.setAuthorId("test-author");
        formDesignRepository.save(f1);

        // Crea y guarda un formulario maestro activo en FormDesignRepository
        FormDesignEntity f2 = new FormDesignEntity();
        f2.setId(UUID.randomUUID());
        f2.setName("Formulario Maestro Activo");
        f2.setTechnicalName("frm_maestro_active");
        f2.setPattern(Pattern.IFORM_MAESTRO);
        f2.setStatus(Status.ACTIVE);
        f2.setFormFields("[]");
        f2.setAuthorId("test-author");
        formDesignRepository.save(f2);

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/forms/active?processKey=simple-process")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(1))
            .body("[0].id", equalTo("frm_simple_active"))
            .body("[0].type", equalTo("SIMPLE"));
    }

    @Test
    @DisplayName("testListActiveFormsFilteredByMaestroProcess")
    void testListActiveFormsFilteredByMaestroProcess() {
        formDesignRepository.deleteAll();
        bpmnProcessDesignRepository.deleteAll();

        // Crea y guarda un proceso BPMN en BpmnProcessDesignRepository
        BpmnProcessDesignEntity process = new BpmnProcessDesignEntity();
        process.setId(UUID.randomUUID());
        process.setTechnicalId("maestro-process");
        process.setFormPattern(BpmnProcessDesignEntity.FormPattern.IFORM_MAESTRO);
        process.setStatus(BpmnProcessDesignEntity.Status.DRAFT);
        process.setName("Proceso Maestro");
        process.setMaxNodes(100);
        process.setCurrentVersion(1);
        process.setCreatedAt(LocalDateTime.now());
        process.setUpdatedAt(LocalDateTime.now());
        process.setCreatedBy("BPMN_Release_Manager");
        bpmnProcessDesignRepository.save(process);

        // Crea y guarda los mismos dos formularios activos
        FormDesignEntity f1 = new FormDesignEntity();
        f1.setId(UUID.randomUUID());
        f1.setName("Formulario Simple Activo");
        f1.setTechnicalName("frm_simple_active");
        f1.setPattern(Pattern.SIMPLE);
        f1.setStatus(Status.ACTIVE);
        f1.setFormFields("[]");
        f1.setAuthorId("test-author");
        formDesignRepository.save(f1);

        FormDesignEntity f2 = new FormDesignEntity();
        f2.setId(UUID.randomUUID());
        f2.setName("Formulario Maestro Activo");
        f2.setTechnicalName("frm_maestro_active");
        f2.setPattern(Pattern.IFORM_MAESTRO);
        f2.setStatus(Status.ACTIVE);
        f2.setFormFields("[]");
        f2.setAuthorId("test-author");
        formDesignRepository.save(f2);

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/forms/active?processKey=maestro-process")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(1))
            .body("[0].id", equalTo("frm_maestro_active"))
            .body("[0].type", equalTo("MASTER"));
    }

    @Test
    @DisplayName("testListActiveFormsNoFilter")
    void testListActiveFormsNoFilter() {
        formDesignRepository.deleteAll();
        bpmnProcessDesignRepository.deleteAll();

        // Crea y guarda los mismos dos formularios activos
        FormDesignEntity f1 = new FormDesignEntity();
        f1.setId(UUID.randomUUID());
        f1.setName("Formulario Simple Activo");
        f1.setTechnicalName("frm_simple_active");
        f1.setPattern(Pattern.SIMPLE);
        f1.setStatus(Status.ACTIVE);
        f1.setFormFields("[]");
        f1.setAuthorId("test-author");
        formDesignRepository.save(f1);

        FormDesignEntity f2 = new FormDesignEntity();
        f2.setId(UUID.randomUUID());
        f2.setName("Formulario Maestro Activo");
        f2.setTechnicalName("frm_maestro_active");
        f2.setPattern(Pattern.IFORM_MAESTRO);
        f2.setStatus(Status.ACTIVE);
        f2.setFormFields("[]");
        f2.setAuthorId("test-author");
        formDesignRepository.save(f2);

        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/forms/active")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(2))
            .body("id", hasItems("frm_simple_active", "frm_maestro_active"));
    }
}
