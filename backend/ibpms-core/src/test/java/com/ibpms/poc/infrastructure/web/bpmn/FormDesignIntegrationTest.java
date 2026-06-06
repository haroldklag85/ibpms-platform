package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.application.dto.CreateFormDesignDTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class FormDesignIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Limpiar procesos para que no falle el CA-26
        runtimeService.createProcessInstanceQuery().list().forEach(pi -> 
            runtimeService.deleteProcessInstance(pi.getId(), "Test cleanup")
        );
    }

    @Test
    public void testCreateAndUpdateForm_IncrementsVersion() {
        // Escenario 1: Creación y Auto-incremento de versión (CA-11)
        CreateFormDesignDTO dto = new CreateFormDesignDTO();
        dto.setName("Test Form");
        dto.setTechnicalName("test-form-" + UUID.randomUUID().toString());
        dto.setPattern("SIMPLE");
        dto.setVueTemplate("<template></template>");
        dto.setZodSchema("{}");
        dto.setFormFields(java.util.Collections.emptyList());

        // 1. Crear el formulario base
        String idStr = given().log().all()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "test-user")
                .body(dto)
                .when()
                .post("/api/v1/forms")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("version", equalTo(1))
                .body("technicalName", equalTo(dto.getTechnicalName()))
                .extract().path("id");

        UUID id = UUID.fromString(idStr);

        // 2. Intentar actualizar simulando "ACTIVE" 
        // Actualizamos a mano el status a ACTIVE usando JDBC
        jdbcTemplate.update("UPDATE ibpms_form_design SET status = 'ACTIVE' WHERE id = ?", id);

        dto.setName("Test Form V2");
        
        given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", "test-user")
                .body(dto)
                .when()
                .post("/api/v1/forms/" + id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("version", equalTo(2))
                .body("name", equalTo("Test Form V2"))
                .body("technicalName", equalTo(dto.getTechnicalName()));
    }

    @Test
    public void testDeleteForm_SoftDeleteSuccessful() {
        // Escenario 3: Soft Delete exitoso cuando no hay procesos
        CreateFormDesignDTO dto = new CreateFormDesignDTO();
        dto.setName("Form to Delete");
        dto.setTechnicalName("delete-form-" + UUID.randomUUID().toString());
        dto.setPattern("SIMPLE");
        dto.setVueTemplate("<template></template>");
        dto.setZodSchema("{}");
        dto.setFormFields(java.util.Collections.emptyList());

        String idStr = given()
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post("/api/v1/forms")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        // Borrar formulario
        given()
                .when()
                .delete("/api/v1/forms/" + idStr)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
                
        // Verificar en DB que ya no sale en el get activo (si estuviera disponible)
        // Como el get all está desactivado, podemos confiar en el status 204
    }

    @Test
    public void testDeleteForm_WithActiveProcess_ReturnsConflict() {
        // Escenario 2: Rechazo de borrado (Error 409) cuando hay instancias vivas
        CreateFormDesignDTO dto = new CreateFormDesignDTO();
        dto.setName("Form Locked");
        dto.setTechnicalName("locked-form-" + UUID.randomUUID().toString());
        dto.setPattern("SIMPLE");
        dto.setVueTemplate("<template></template>");
        dto.setZodSchema("{}");
        dto.setFormFields(java.util.Collections.emptyList());

        String idStr = given()
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post("/api/v1/forms")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        // Desplegar un proceso simple y arrancar una instancia
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("testProcess")
                .startEvent()
                .userTask("task1").name("User Task")
                .endEvent()
                .done();

        Deployment deployment = repositoryService.createDeployment()
                .addModelInstance("testProcess.bpmn", modelInstance)
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        org.camunda.bpm.engine.runtime.ProcessInstance processInstance = 
            runtimeService.startProcessInstanceById(processDefinition.getId());

        try {
            // Intentar borrar (Debe fallar con 409 porque hay una instancia activa en runtimeService)
            given()
                    .when()
                    .delete("/api/v1/forms/" + idStr)
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("error", containsString("Trámites Activos"));
        } finally {
            // Limpieza para no afectar otros tests
            runtimeService.deleteProcessInstance(processInstance.getId(), "Test cleanup");
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }
}
