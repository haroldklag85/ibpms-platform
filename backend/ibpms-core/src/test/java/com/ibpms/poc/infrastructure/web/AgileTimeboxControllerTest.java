package com.ibpms.poc.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ibpms.poc.application.service.AgileTimeboxService;
import com.ibpms.poc.domain.model.agile.AgileTimebox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AgileTimeboxController.class)
@ActiveProfiles("test")
class AgileTimeboxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgileTimeboxService timeboxService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/v1/agile/timeboxes → 201 Created con datos válidos y rol ADMIN")
    @WithMockUser(username = "scrum.master", roles = {"ADMIN"})
    void createTimebox_ValidRequest_Returns201() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID timeboxId = UUID.randomUUID();

        AgileTimebox mockTimebox = AgileTimebox.builder()
                .id(timeboxId)
                .projectId(projectId)
                .name("Sprint 5")
                .goal("Estabilizar Motor DMN")
                .startDate(LocalDate.of(2026, 4, 14))
                .endDate(LocalDate.of(2026, 4, 28))
                .status("PLANNING")
                .createdBy("admin")
                .createdAt(ZonedDateTime.now())
                .build();

        when(timeboxService.createTimebox(any(), any(), any(), any(), any(), any()))
                .thenReturn(mockTimebox);

        String payload = objectMapper.writeValueAsString(
                new AgileTimeboxController.CreateTimeboxRequest(
                        projectId,
                        "Sprint 5",
                        "Estabilizar Motor DMN",
                        LocalDate.of(2026, 4, 14),
                        LocalDate.of(2026, 4, 28)
                )
        );

        mockMvc.perform(post("/api/v1/agile/timeboxes").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sprint 5"))
                .andExpect(jsonPath("$.status").value("PLANNING"));
    }

    @Test
    @DisplayName("POST /api/v1/agile/timeboxes → 403 Forbidden si el usuario no tiene rol autorizado")
    @WithMockUser(username = "ciudadano.externo", roles = {"CIUDADANO"})
    void createTimebox_UnauthorizedRole_Returns403() throws Exception {
        UUID projectId = UUID.randomUUID();
        String payload = objectMapper.writeValueAsString(
                new AgileTimeboxController.CreateTimeboxRequest(
                        projectId,
                        "Sprint X",
                        null,
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 15)
                )
        );

        mockMvc.perform(post("/api/v1/agile/timeboxes").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }
}
