package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.AbstractIntegrationIT;
import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity;
import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity.Pattern;
import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity.Status;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus;
import com.ibpms.poc.infrastructure.jpa.repository.FormDesignRepository;
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

import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

// @Traceability: US-005, CA-39
@DisplayName("Form Catalog Active Integration Test")
public class FormCatalogActiveIntegrationIT extends AbstractIntegrationIT {

    @Autowired
    private FormDesignRepository formDesignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String jwtToken;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Limpiar usuario existente si quedo de un test previo
        userRepository.findByUsername("BPMN_Release_Manager")
                .ifPresent(u -> userRepository.delete(u));

        // Crear y guardar el usuario en la DB para saltar JIT provisioning
        UserEntity user = new UserEntity();
        user.setUsername("BPMN_Release_Manager");
        user.setEmail("bpmn_release_manager@sso.local");
        user.setStatus(UserStatus.ACTIVE);
        user.setIsExternalIdp(false);
        userRepository.save(user);

        // Generar un JWT valido firmado con la clave real del perfil de test usando JwtTokenProvider
        jwtToken = jwtTokenProvider.generateToken(
                "BPMN_Release_Manager",
                Arrays.asList("ibpms_rol_BPMN_Release_Manager"),
                "T1"
        );
    }

    @AfterEach
    public void tearDown() {
        formDesignRepository.deleteAll();

        // Limpiar el usuario creado para el test
        userRepository.findByUsername("BPMN_Release_Manager")
                .ifPresent(u -> userRepository.delete(u));
    }

    @Test
    @DisplayName("testListActiveForms: List active forms from repository")
    void testListActiveForms() {
        // Limpie la base de datos de formularios (usando formDesignRepository.deleteAll())
        formDesignRepository.deleteAll();

        // Inserte un formulario simple activo (status = Status.ACTIVE, pattern = Pattern.SIMPLE, technicalName = "frm_aprobacion", name = "Formulario Aprobación", formFields = "[]")
        FormDesignEntity f1 = new FormDesignEntity();
        f1.setId(UUID.randomUUID());
        f1.setName("Formulario Aprobación");
        f1.setTechnicalName("frm_aprobacion");
        f1.setPattern(Pattern.SIMPLE);
        f1.setStatus(Status.ACTIVE);
        f1.setFormFields("[]");
        f1.setAuthorId("test-author");
        formDesignRepository.save(f1);

        // Inserte un formulario maestro activo (status = Status.ACTIVE, pattern = Pattern.IFORM_MAESTRO, technicalName = "frm_onboarding_master", name = "Onboarding Integral", formFields = "[{\"camundaVariable\":\"v1\",\"type\":\"text\",\"stage\":\"STAGE_A\"},{\"camundaVariable\":\"v2\",\"type\":\"text\",\"stage\":\"STAGE_B\"}]")
        FormDesignEntity f2 = new FormDesignEntity();
        f2.setId(UUID.randomUUID());
        f2.setName("Onboarding Integral");
        f2.setTechnicalName("frm_onboarding_master");
        f2.setPattern(Pattern.IFORM_MAESTRO);
        f2.setStatus(Status.ACTIVE);
        f2.setFormFields("[{\"camundaVariable\":\"v1\",\"type\":\"text\",\"stage\":\"STAGE_A\"},{\"camundaVariable\":\"v2\",\"type\":\"text\",\"stage\":\"STAGE_B\"}]");
        f2.setAuthorId("test-author");
        formDesignRepository.save(f2);

        // Inserte un formulario en estado borrador (status = Status.DRAFT, pattern = Pattern.SIMPLE, technicalName = "frm_borrador", name = "Formulario Borrador", formFields = "[]")
        FormDesignEntity f3 = new FormDesignEntity();
        f3.setId(UUID.randomUUID());
        f3.setName("Formulario Borrador");
        f3.setTechnicalName("frm_borrador");
        f3.setPattern(Pattern.SIMPLE);
        f3.setStatus(Status.DRAFT);
        f3.setFormFields("[]");
        f3.setAuthorId("test-author");
        formDesignRepository.save(f3);

        // Inserte un formulario en estado eliminado (status = Status.DELETED, pattern = Pattern.SIMPLE, technicalName = "frm_deleted", name = "Formulario Eliminado", formFields = "[]")
        FormDesignEntity f4 = new FormDesignEntity();
        f4.setId(UUID.randomUUID());
        f4.setName("Formulario Eliminado");
        f4.setTechnicalName("frm_deleted");
        f4.setPattern(Pattern.SIMPLE);
        f4.setStatus(Status.DELETED);
        f4.setFormFields("[]");
        f4.setAuthorId("test-author");
        formDesignRepository.save(f4);

        // Envíe una petición GET a /api/v1/forms/active con la cabecera Authorization: Bearer <token>
        // Valide que:
        // - El estatus HTTP sea 200 (OK).
        // - El cuerpo JSON contenga exactamente 2 elementos (los dos formularios activos).
        // - El primer formulario tenga "id" = "frm_aprobacion", "name" = "Formulario Aprobación", "type" = "SIMPLE", y no contenga la propiedad "stages".
        // - El segundo formulario tenga "id" = "frm_onboarding_master", "name" = "Onboarding Integral", "type" = "MASTER", y "stages" = 2.
        // - Que los formularios "frm_borrador" y "frm_deleted" no estén presentes en los resultados devueltos.
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/forms/active")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(2))
            .body("[0].id", equalTo("frm_aprobacion"))
            .body("[0].name", equalTo("Formulario Aprobación"))
            .body("[0].type", equalTo("SIMPLE"))
            .body("[0]", not(hasKey("stages")))
            .body("[1].id", equalTo("frm_onboarding_master"))
            .body("[1].name", equalTo("Onboarding Integral"))
            .body("[1].type", equalTo("MASTER"))
            .body("[1].stages", equalTo(2))
            .body("id", not(hasItems("frm_borrador", "frm_deleted")));
    }
}
