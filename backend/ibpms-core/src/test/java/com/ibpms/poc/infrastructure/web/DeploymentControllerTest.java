package com.ibpms.poc.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.DeploymentRequestDTO;
import com.ibpms.poc.application.port.in.DesplegarDefinicionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeploymentController.class)
class DeploymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DesplegarDefinicionUseCase desplegarDefinicionUseCase;

    // ── QA Instruction 4: Test Deploy RBAC ──
    @Test
    @DisplayName("Debe permitir Deploy (HTTP 201) si el usuario tiene rol RELEASE_MANAGER")
    @WithMockUser(username = "carlos.rm", roles = { "RELEASE_MANAGER" })
    void deployProcess_Exitoso_RoleReleaseManager() throws Exception {
        DeploymentRequestDTO request = new DeploymentRequestDTO();
        request.setResourceName("proceso.bpmn");
        request.setXmlString("<bpmn />");

        mockMvc.perform(post("/api/v1/deployments").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Debe rechazar Deploy (HTTP 403 Forbidden) si el usuario es solo DESIGNER")
    @WithMockUser(username = "ana.designer", roles = { "DESIGNER" })
    void deployProcess_Rechazado_RoleDesigner() throws Exception {
        DeploymentRequestDTO request = new DeploymentRequestDTO();
        request.setResourceName("proceso.bpmn");
        request.setXmlString("<bpmn />");

        mockMvc.perform(post("/api/v1/deployments").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
