package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.infrastructure.jpa.entity.FormDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDesignRepository;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * // @Traceability: US-005, CA-26
 */
@DisplayName("Form Design Active Protection Integration Test")
public class FormDesignActiveProtectionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private FormDesignRepository formDesignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String jwtToken;
    private Deployment bpmnDeployment;
    private ProcessInstance processInstance;

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
        if (processInstance != null) {
            try {
                runtimeService.deleteProcessInstance(processInstance.getId(), "Test cleanup");
            } catch (Exception e) {
                // Ignorar si ya fue borrado
            }
        }
        if (bpmnDeployment != null) {
            try {
                repositoryService.deleteDeployment(bpmnDeployment.getId(), true);
            } catch (Exception e) {
                // Ignorar
            }
        }
        formDesignRepository.deleteAll();

        // Limpiar el usuario creado para el test
        userRepository.findByUsername("BPMN_Release_Manager")
                .ifPresent(u -> userRepository.delete(u));
    }

    @Test
    @DisplayName("testDeleteFormNotInUse: Eliminar formulario no en uso retorna 204")
    void testDeleteFormNotInUse() {
        // Arrange
        FormDesignEntity form = new FormDesignEntity();
        form.setId(UUID.randomUUID());
        form.setName("Formulario Test No En Uso");
        form.setTechnicalName("frm_solicitud_v1");
        form.setPattern(FormDesignEntity.Pattern.SIMPLE);
        form.setStatus(FormDesignEntity.Status.ACTIVE);
        form.setVersion(1);
        form.setVueTemplate("<template></template>");
        form.setZodSchema("{}");
        form.setFormFields("[]");
        form.setAuthorId("test-author");
        form = formDesignRepository.save(form);

        // Act & Assert
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/v1/forms/" + form.getId())
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("testDeleteFormInUse: Eliminar formulario en uso por proceso activo retorna 409 Conflict")
    void testDeleteFormInUse() {
        // Arrange
        FormDesignEntity form = new FormDesignEntity();
        form.setId(UUID.randomUUID());
        form.setName("Formulario Test En Uso");
        form.setTechnicalName("frm_solicitud_v1");
        form.setPattern(FormDesignEntity.Pattern.SIMPLE);
        form.setStatus(FormDesignEntity.Status.ACTIVE);
        form.setVersion(1);
        form.setVueTemplate("<template></template>");
        form.setZodSchema("{}");
        form.setFormFields("[]");
        form.setAuthorId("test-author");
        form = formDesignRepository.save(form);

        // Desplegar proceso BPMN programáticamente con una UserTask que use frm_solicitud_v1
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process_test_in_use")
                .startEvent()
                .userTask("userTaskWithFormKey")
                    .name("User Task with Form Key")
                    .camundaFormKey("frm_solicitud_v1")
                .endEvent()
                .done();

        bpmnDeployment = repositoryService.createDeployment()
                .addModelInstance("process_test_in_use.bpmn", modelInstance)
                .deploy();

        // Iniciar una instancia del proceso
        processInstance = runtimeService.startProcessInstanceByKey("process_test_in_use");

        // Act & Assert
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/v1/forms/" + form.getId())
        .then()
            .statusCode(HttpStatus.CONFLICT.value())
            .body("error", equalTo("Prohibido: Este formulario está siendo usado por 1 procesos activos."));
    }
}
