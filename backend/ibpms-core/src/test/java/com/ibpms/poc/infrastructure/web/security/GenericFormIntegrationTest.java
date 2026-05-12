package com.ibpms.poc.infrastructure.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.AbstractLocalE2ETest;
import com.ibpms.poc.application.rest.dto.GenericFormSubmitRequest;
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

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GenericFormIntegrationTest extends AbstractLocalE2ETest {

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeEach
    void setUp() {
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("genericFormTestProcess")
                .startEvent()
                .userTask("genericTask")
                    .camundaAssignee("correct-user")
                .boundaryEvent("panicBoundary")
                    .error("TASK_CANCELLED_BY_OPERATOR")
                    .endEvent()
                .moveToActivity("genericTask")
                .endEvent()
                .done();

        deployment = repositoryService.createDeployment()
                .addModelInstance("genericFormTestProcess.bpmn", modelInstance)
                .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("genericFormTestProcess");
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
    }

    @Test
    @DisplayName("Fail-Fast: Submit de un usuario que NO es el Assignee devuelve un 403 Forbidden")
    @WithMockUser(username = "wrong-user")
    void testFailFast_WhenUserIsNotAssignee_ReturnsForbidden() throws Exception {
        GenericFormSubmitRequest request = new GenericFormSubmitRequest();
        request.setObservations("Testing observations > 10 chars");
        request.setManagementResult("APPROVED");
        request.setAttachmentUuids(new ArrayList<>());

        mockMvc.perform(post("/api/v1/workbox/tasks/" + taskId + "/generic-form-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CA-8 Panic Action: CANCELLED devuelve codigo y termina con el event boundary")
    @WithMockUser(username = "correct-user")
    void testPanicAction_Cancelled_TerminesProcessWithError() throws Exception {
        GenericFormSubmitRequest request = new GenericFormSubmitRequest();
        request.setObservations("Testing panic action with valid size justification");
        request.setManagementResult("APPROVED");
        request.setPanicAction("CANCELLED");
        request.setPanicJustification("El usuario presiona pánico para cancelar el proceso debido a un error grave."); // >= 20 chars
        request.setAttachmentUuids(new ArrayList<>());

        mockMvc.perform(post("/api/v1/workbox/tasks/" + taskId + "/generic-form-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertNull(
                runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult(),
                "Process should be completed/cancelled due to Panic Action"
        );
    }
}
