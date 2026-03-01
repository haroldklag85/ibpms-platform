package com.ibpms.poc.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.ProjectTemplateDTO;
import com.ibpms.poc.application.port.in.CrearProjectTemplateUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProjectTemplateController.class)
public class ProjectTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CrearProjectTemplateUseCase crearProjectTemplateUseCase;

    @Test
    @WithMockUser(username = "architect_user", roles = { "Architect" })
    void shouldCreateProjectTemplateSuccessfully() throws Exception {
        UUID expectedId = UUID.randomUUID();
        ProjectTemplateDTO dtoOut = new ProjectTemplateDTO();
        dtoOut.setId(expectedId);
        dtoOut.setName("WBS Desarrollo Inmobiliario");
        dtoOut.setCreatedAt(LocalDateTime.now());
        dtoOut.setCreatedBy("architect_user");

        when(crearProjectTemplateUseCase.crearPlantilla(any(ProjectTemplateDTO.class), anyString()))
                .thenReturn(dtoOut);

        ProjectTemplateDTO dtoIn = new ProjectTemplateDTO();
        dtoIn.setName("WBS Desarrollo Inmobiliario");
        dtoIn.setDescription("Plantilla Base para Proyectos Vivienda");
        dtoIn.setCategory("Construccion");

        ProjectTemplateDTO.PhaseDTO phase1 = new ProjectTemplateDTO.PhaseDTO();
        phase1.setName("Acabados");
        phase1.setOrderIndex(1);
        dtoIn.setPhases(Collections.singletonList(phase1));

        mockMvc.perform(post("/project-templates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoIn)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedId.toString()))
                .andExpect(jsonPath("$.name").value("WBS Desarrollo Inmobiliario"))
                .andExpect(jsonPath("$.createdBy").value("architect_user"));
    }

    @Test
    @WithMockUser(username = "lawyer", roles = { "Funcionario" })
    void shouldDenyAccessToNonArchitectRole() throws Exception {
        ProjectTemplateDTO dtoIn = new ProjectTemplateDTO();
        dtoIn.setName("WBS Prueba");

        mockMvc.perform(post("/project-templates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoIn)))
                .andExpect(status().isForbidden());
    }
}
