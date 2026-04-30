package com.ibpms.poc.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.service.AgileTaskService;
import com.ibpms.poc.application.service.TaskDraftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkboxTaskController.class)
class WorkboxTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgileTaskService taskService;

    @MockBean
    private TaskDraftService draftService;

    @Test
    @WithMockUser(username = "operador1", roles = {"OPERADOR"})
    void shouldRollbackClaimSuccessfully() throws Exception {
        UUID taskId = UUID.randomUUID();

        // Mocks no lanzan excepción para flujo exitoso (Status 200)
        doNothing().when(taskService).rollbackClaim(taskId, "operador1");

        mockMvc.perform(post("/api/v1/workbox/tasks/" + taskId + "/rollback-claim")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "operador1", roles = {"OPERADOR"})
    void shouldFailRollbackClaimIfAssignedToAnother() throws Exception {
        UUID taskId = UUID.randomUUID();

        // Emulando CA-21: Conflicto si fue ganado por otro asíncronamente
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "La tarea ya fue reclamada por otro operador"))
                .when(taskService).rollbackClaim(taskId, "operador1");

        mockMvc.perform(post("/api/v1/workbox/tasks/" + taskId + "/rollback-claim")
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
}
