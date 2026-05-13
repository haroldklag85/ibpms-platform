package com.ibpms.poc.infrastructure.web.messaging;

import com.ibpms.poc.application.service.JwtBlacklistService;
import com.ibpms.poc.application.service.messaging.DlqManagementService;
import com.ibpms.poc.application.service.security.RoleHierarchyService;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.ServiceAccountRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @Traceability(US = "US-034", CA = "CA-02")
 * TDD: Validación de RBAC y endpoints del Dashboard DLQ.
 */
@WebMvcTest(AdminQueueController.class)
class AdminQueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DlqManagementService dlqManagementService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ServiceAccountRepository serviceAccountRepository;

    // Security filter chain dependencies
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private JwtBlacklistService jwtBlacklistService;

    @MockBean
    private RoleHierarchyService roleHierarchyService;

    @MockBean
    private com.ibpms.poc.application.service.security.EntraIdSyncService entraIdSyncService;

    @MockBean
    private com.ibpms.poc.infrastructure.jpa.repository.security.RoleDelegationRepository roleDelegationRepository;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getSummary_shouldReturnOk_forAdminRole() throws Exception {
        when(dlqManagementService.getDlqSummary())
                .thenReturn(Map.of("totalMessages", 3, "oldestMessages", java.util.List.of()));

        mockMvc.perform(get("/api/v1/admin/queues/dlq/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMessages").value(3));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSummary_shouldReturnForbidden_forRegularUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/queues/dlq/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void retryMessages_shouldReturnRetriedCount() throws Exception {
        when(dlqManagementService.retryMessages(10)).thenReturn(5);

        mockMvc.perform(post("/api/v1/admin/queues/dlq/retry")
                        .param("maxMessages", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retriedCount").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN_IT")
    void purge_shouldReturnOk_forAdminIT() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/queues/dlq/purge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PURGED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void purge_shouldReturnForbidden_forRegularUser() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/queues/dlq/purge"))
                .andExpect(status().isForbidden());
    }
}
