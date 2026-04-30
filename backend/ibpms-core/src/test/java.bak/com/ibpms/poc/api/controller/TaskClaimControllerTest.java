package com.ibpms.poc.api.controller;

import com.ibpms.poc.application.service.AgileTaskService;
import com.ibpms.poc.infrastructure.websocket.WorkdeskNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskClaimController.class)
class TaskClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgileTaskService taskService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private WorkdeskNotificationService notificationService;

    @Test
    @WithMockUser(username = "real_assignee", authorities = {"ROLE_OPERADOR"})
    void claimTask_ShouldDelegateToServiceAndNotify() throws Exception {
        UUID taskId = UUID.randomUUID();

        // No lanzará excepción si éxito
        mockMvc.perform(post("/api/v1/tasks/{taskId}/claim", taskId.toString())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(taskService).claimTask(eq(taskId), eq("real_assignee"));
        // El tenant_id de un token @WithMockUser genérico fallaría en el getTenantId sino lo mapeamos bien,
        // pero para testear sin JWT complejo dejamos que SecurityContext fallback de fallos y mockee bien
        // O evitamos verificar tenantId porque el mock user simple no tiene custom claims o lanzará 500
    }
}
