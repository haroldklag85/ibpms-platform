// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.AbstractIntegrationTest;


import com.ibpms.poc.application.service.TaskDelegationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WorkdeskQueryControllerDelegationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskDelegationService taskDelegationService;

    @Test
    @WithMockUser(username = "usr_admin_alpha", roles = {"ADMIN"})
    void shouldReturn403WhenDelegationHasNoRelation_CU_J04_NEG_04() throws Exception {
        // Mock ResponseStatusException for validateDelegationHierarchy to simulate HTTP 403
        when(taskDelegationService.validateDelegationHierarchy(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        )).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: No tiene autorización jerárquica para visualizar el escritorio de este usuario."));

        mockMvc.perform(get("/api/v1/workdesk/global-inbox")
                .param("delegatedUserId", "unauthorized_user")
                .contentType("application/json"))
                .andExpect(status().isForbidden());
    }
}
