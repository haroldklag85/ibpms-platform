package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.AiMetricsDTO;
import com.ibpms.poc.application.dto.ProcessHealthDTO;
import com.ibpms.poc.application.port.in.ObtenerMetricasUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ObtenerMetricasUseCase obtenerMetricasUseCase;

    @Test
    @WithMockUser(username = "gerente", roles = { "Directivo" })
    void shouldReturnProcessHealthMetrics() throws Exception {
        ProcessHealthDTO healthDTO = new ProcessHealthDTO(150, 500, 300, 20);
        when(obtenerMetricasUseCase.getProcessHealth()).thenReturn(healthDTO);

        mockMvc.perform(get("/analytics/process-health")
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeCases").value(150))
                .andExpect(jsonPath("$.completedCases").value(500))
                .andExpect(jsonPath("$.activeTasks").value(300))
                .andExpect(jsonPath("$.overdueTasks").value(20));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "Admin_Intake" })
    void shouldReturnAiMetrics() throws Exception {
        AiMetricsDTO aiDTO = new AiMetricsDTO();
        aiDTO.setTotalAiEvents(100);
        aiDTO.setGeneratedDmns(40);
        aiDTO.setAutoRoutedEmails(60);
        aiDTO.setAverageSimilarityScore(0.85);

        when(obtenerMetricasUseCase.getAiMetrics()).thenReturn(aiDTO);

        mockMvc.perform(get("/analytics/ai-metrics")
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAiEvents").value(100))
                .andExpect(jsonPath("$.generatedDmns").value(40))
                .andExpect(jsonPath("$.autoRoutedEmails").value(60))
                .andExpect(jsonPath("$.averageSimilarityScore").value(0.85));
    }

    @Test
    @WithMockUser(username = "asesor", roles = { "Funcionario" })
    void shouldForbidAccessToNonDirectives() throws Exception {
        mockMvc.perform(get("/analytics/process-health")
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON)))
                .andExpect(status().isForbidden());
    }
}
