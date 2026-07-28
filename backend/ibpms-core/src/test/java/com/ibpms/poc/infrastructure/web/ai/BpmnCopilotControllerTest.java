package com.ibpms.poc.infrastructure.web.ai;

import com.ibpms.poc.application.usecase.ai.BpmnCopilotUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;

import com.ibpms.poc.application.util.SecurityContextUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.junit.jupiter.api.AfterEach;

@ExtendWith(MockitoExtension.class)
public class BpmnCopilotControllerTest {

    private MockMvc mockMvc;
    private MockedStatic<SecurityContextUtils> mockSecurityContextUtils;

    @Mock
    private BpmnCopilotUseCase copilotUseCase;

    @InjectMocks
    private BpmnCopilotController controller;

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
    void wipeCopilotMemory_usesTenantFromJwt() throws Exception {
        mockSecurityContext("tenant_from_jwt");
        mockMvc.perform(delete("/api/v1/ai/copilot/session").param("sessionId", "session123"))
                .andExpect(status().isOk());
        verify(copilotUseCase).triggerRagSessionWipe("tenant_from_jwt", "session123");
        verify(copilotUseCase, never()).triggerRagSessionWipe(eq("tenant_hq_corp"), anyString());
    }

    @Test
    void wipeCopilotMemory_rejectsCrossTenant() throws Exception {
        mockSecurityContext("tenant_other");
        mockMvc.perform(delete("/api/v1/ai/copilot/session").param("sessionId", "sessionXYZ"))
                .andExpect(status().isOk());
        verify(copilotUseCase).triggerRagSessionWipe("tenant_other", "sessionXYZ");
    }
}
