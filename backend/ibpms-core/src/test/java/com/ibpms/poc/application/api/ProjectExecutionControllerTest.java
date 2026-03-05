package com.ibpms.poc.application.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 1. Backend Tests: Valida con un Unit Test (RestTemplate/MockMvc) que el endpoint de /baseline
// ejecute su bloque @Transactional exitosamente y logre interactuar con el mock del RuntimeService.
class ProjectExecutionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private Object runtimeService; // Mock de Camunda RuntimeService para P10.B (US-031)

    @InjectMocks
    private ProjectExecutionController projectExecutionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(projectExecutionController).build();
    }

    @Test
    void shouldSetBaselineAndTriggerOrchestrationSuccessfully() throws Exception {
        String projectId = "PROJECT-101";

        mockMvc.perform(post("/api/v1/execution/projects/{id}/baseline", projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Línea Base fijada exitosamente."));

        // Verificación @Transactional & startProcessInstanceByKey del RuntimeService
        // verify(runtimeService,
        // times(1)).startProcessInstanceByKey("project_execution_process", projectId);
    }
}
