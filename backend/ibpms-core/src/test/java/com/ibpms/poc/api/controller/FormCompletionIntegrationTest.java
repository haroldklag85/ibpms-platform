package com.ibpms.poc.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.AbstractLocalE2ETest;
import com.ibpms.poc.application.dto.FormSubmitRequest;
import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDefinitionRepository;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FormCompletionIntegrationTest extends AbstractLocalE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FormDefinitionRepository formDefinitionRepository;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private String taskId;
    private String processInstanceId;
    private Deployment deployment;
    private FormDefinitionEntity schemaEntity;

    @BeforeEach
    void setUp() {
        // Prepare strict JSON Schema in DB
        String strictSchema = "{"
            + "\"$schema\": \"http://json-schema.org/draft-07/schema#\","
            + "\"type\": \"object\","
            + "\"properties\": {"
            + "  \"wizardStep1Age\": { \"type\": \"integer\", \"minimum\": 18 }"
            + "},"
            + "\"required\": [\"wizardStep1Age\"]"
            + "}";
            
        schemaEntity = new FormDefinitionEntity();
        schemaEntity.setFormId(UUID.randomUUID());
        schemaEntity.setVersionId(1);
        schemaEntity.setSchemaContent(strictSchema);
        schemaEntity.setCreatedBy("admin");
        schemaEntity.setHashSha256("mock-hash");
        formDefinitionRepository.save(schemaEntity);

        // Prepare Camunda Task
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("formCompletionProcess")
                .startEvent()
                .userTask("completionTask")
                    .camundaAssignee("operator-user")
                .endEvent()
                .done();

        deployment = repositoryService.createDeployment()
                .addModelInstance("formCompletionProcess.bpmn", modelInstance)
                .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("formCompletionProcess");
        processInstanceId = processInstance.getId();

        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        taskId = task.getId();
    }

    @AfterEach
    void tearDown() {
        if (runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult() != null) {
            runtimeService.deleteProcessInstance(processInstanceId, "Test cleanup");
        }
        repositoryService.deleteDeployment(deployment.getId(), true);
        formDefinitionRepository.deleteById(schemaEntity.getId());
    }

    // @Traceability: US-029 - CA-22 - Validando bloqueo de Submit si el Wizard no cumple con JSON schema estricto
    @Test
    @DisplayName("US-029: Submit with invalid payload against DB injected JSON Schema should return 400 Bad Request")
    @WithMockUser(username = "operator-user")
    void testFailFast_WhenPayloadBreaksSchema_ReturnsBadRequest() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("wizardStep1Age", 15); // Invalid: minimum is 18

        FormSubmitRequest request = FormSubmitRequest.builder()
                .payload(payload)
                .schemaVersion(schemaEntity.getId().toString())
                .idempotencyKey(UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/v1/workbox/bpmn-tasks/" + taskId + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
