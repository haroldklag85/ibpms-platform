package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class SandboxIsolationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcesoBpmPort procesoBpmPort;

    @Autowired
    private WorkdeskProjectionRepository workdeskProjectionRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/sandbox";
    }

    @Test
    @DisplayName("CA-63: testSandboxModePreventsRealApiCalls")
    void testSandboxModePreventsRealApiCalls() {
        // Enviar un payload que dispararía una tarea externa real, simulando que estamos en modo Sandbox
        given()
            .header("X-Sandbox-Mode", "true")
            .contentType(ContentType.JSON)
            .body("{\"payload\": {\"amount\": 5000}}")
        .when()
            .post("/workers/execute-mock")
        .then()
            .statusCode(200)
            .body("status", equalTo("mocked"))
            .body("real_api_called", equalTo(false));
    }

    @Test
    @DisplayName("CA-78: testSandboxMultiTenancyAisolation")
    @com.ibpms.poc.crosscutting.annotations.Traceability(US = "US-005", CA = {"CA-78"})
    void testSandboxMultiTenancyAisolation() throws Exception {
        // 1. Preparar el XML de proceso simple
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" " +
                "id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"sandbox-process-test\" isExecutable=\"true\" camunda:historyTimeToLive=\"180\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\">\n" +
                "      <bpmn:outgoing>Flow_1</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"UserTask_1\" />\n" +
                "    <bpmn:userTask id=\"UserTask_1\" name=\"Sandbox User Task\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <camunda:taskListener delegateExpression=\"${camundaTaskSyncListener}\" event=\"create\" />\n" +
                "        <camunda:taskListener delegateExpression=\"${camundaTaskSyncListener}\" event=\"assignment\" />\n" +
                "        <camunda:taskListener delegateExpression=\"${camundaTaskSyncListener}\" event=\"complete\" />\n" +
                "        <camunda:taskListener delegateExpression=\"${camundaTaskSyncListener}\" event=\"delete\" />\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_1</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_2</bpmn:outgoing>\n" +
                "    </bpmn:userTask>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"UserTask_1\" targetRef=\"EndEvent_1\" />\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\">\n" +
                "      <bpmn:incoming>Flow_2</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        // 2. Establecer el contexto de la solicitud con el header X-Sandbox-Mode = true
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("X-Sandbox-Mode", "true");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        try {
            // 3. Desplegar el proceso bajo Sandbox mode
            procesoBpmPort.desplegarProceso("sandbox-process-test.bpmn", xmlString);

            // Verificar en Camunda que el process definition tiene tenant_id = 'sandbox_tenant'
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey("sandbox-process-test")
                    .latestVersion()
                    .singleResult();
            
            assertNotNull(definition, "La definición del proceso debe estar desplegada");
            assertEquals("sandbox_tenant", definition.getTenantId(), "El Tenant ID de la definición debe ser 'sandbox_tenant'");

            // 4. Iniciar la instancia bajo Sandbox mode
            String instanceId = procesoBpmPort.iniciarProceso("sandbox-process-test", "bk-sandbox-test", Collections.emptyMap());
            assertNotNull(instanceId, "Se debe generar un ID de instancia");

            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(instanceId)
                    .singleResult();
            assertNotNull(instance, "La instancia debe existir en el runtime de Camunda");
            assertEquals("sandbox_tenant", instance.getTenantId(), "El Tenant ID de la instancia de proceso debe ser 'sandbox_tenant'");

            // 5. Verificar que la tarea de Camunda tenga tenantId = 'sandbox_tenant'
            org.camunda.bpm.engine.task.Task camundaTask = taskService.createTaskQuery()
                    .processInstanceId(instanceId)
                    .singleResult();
            assertNotNull(camundaTask, "Debe existir una tarea de usuario");
            assertEquals("sandbox_tenant", camundaTask.getTenantId(), "La tarea de Camunda debe tener el Tenant ID 'sandbox_tenant'");

            // 6. Verificar que la proyección CQRS en la base de datos tenga tenant_id = 'sandbox_tenant'
            WorkdeskProjectionEntity projection = workdeskProjectionRepository.findById("BPMN-" + camundaTask.getId())
                    .orElse(null);
            assertNotNull(projection, "La tarea debe estar sincronizada en la proyección CQRS");
            assertEquals("sandbox_tenant", projection.getTenantId(), "La proyección CQRS debe tener el tenant_id = 'sandbox_tenant'");

            // 7. Verificar exclusión: Llamar a findWorkdeskTasks con tenantId = 'sandbox_tenant' no debe retornar nada porque w.tenant_id != 'sandbox_tenant'
            Page<WorkdeskProjectionEntity> tasks = workdeskProjectionRepository.findWorkdeskTasks(
                    "sandbox_tenant", null, null, null, PageRequest.of(0, 10));
            assertTrue(tasks.isEmpty(), "Las consultas de Workdesk deben excluir estrictamente tareas del tenant sandbox_tenant");

        } finally {
            // Limpiar RequestContext
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
