// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.web.dmn;

import com.ibpms.poc.application.usecase.dmn.DmnGovernanceUseCase;
import com.ibpms.poc.application.dto.DmnDefinitionDto;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ibpms.poc.application.util.SecurityContextUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.junit.jupiter.api.AfterEach;

@ExtendWith(MockitoExtension.class)
public class DmnGovernanceControllerTest {

    private MockMvc mockMvc;
    private MockedStatic<SecurityContextUtils> mockSecurityContextUtils;

    @Mock
    private DmnGovernanceUseCase dmnGovernanceUseCase;

    @InjectMocks
    private DmnGovernanceController controller;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @AfterEach
    void tearDown() {
        if (mockSecurityContextUtils != null) {
            mockSecurityContextUtils.close();
        }
    }

    private void mockSecurityContext(String tenant) {
        mockSecurityContextUtils = Mockito.mockStatic(SecurityContextUtils.class);
        mockSecurityContextUtils.when(SecurityContextUtils::getTenantId).thenReturn(tenant);
        mockSecurityContextUtils.when(SecurityContextUtils::getAssignee).thenReturn("usr_tester");
    }

    @Test
    void listDefinitions_filtersByTenant() throws Exception {
        mockSecurityContext("tenant_alpha");
        when(dmnGovernanceUseCase.listDeployedDecisionDefinitions("tenant_alpha"))
                .thenReturn(List.of(
                        new DmnDefinitionDto("id1", "dmn-key-1", "Decision 1", 1, "dep1", "2026-04-19T00:00:00Z")
                ));

        mockMvc.perform(get("/api/v1/dmn-models/definitions")
                .accept(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("dmn-key-1"));

        verify(dmnGovernanceUseCase).listDeployedDecisionDefinitions("tenant_alpha");
    }

    @Test
    void listDefinitions_emptyWhenNoDmn() throws Exception {
        mockSecurityContext("tenant_beta");
        when(dmnGovernanceUseCase.listDeployedDecisionDefinitions("tenant_beta"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/dmn-models/definitions")
                .accept(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(dmnGovernanceUseCase).listDeployedDecisionDefinitions("tenant_beta");
    }
}
