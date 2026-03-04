package com.ibpms.poc.api.controller.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import com.ibpms.poc.infrastructure.security.JwtSecurityFilter;
import com.ibpms.poc.infrastructure.security.JwtAuthFilter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromptLibraryController.class)
@Import(com.ibpms.poc.infrastructure.security.SecurityConfig.class)
class PromptLibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtSecurityFilter jwtSecurityFilter;

    @Test
    @WithMockUser
    @DisplayName("Cualquier usuario autenticado puede LEER (GET) los Prompts Maestros")
    void canReadPrompts_WithAnyRole() throws Exception {
        mockMvc.perform(get("/api/v1/prompts/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "colaborador_raso")
    @DisplayName("Debe rechazar con 403 Forbidden modificaciones PUT sin el rol de ingeniero de prompts")
    void cannotModifyPrompts_WithoutPromptEngineerRole() throws Exception {
        mockMvc.perform(put("/api/v1/prompts/" + UUID.randomUUID())
                .content("Nuevo prompt malicioso")
                .with(csrf())) // CSRF requerido en operaciones mutating de Spring Security
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "prompt_engineer")
    @DisplayName("Debe permitir 200 OK modificaciones PUT cuando el rol es prompt_engineer")
    void canModifyPrompts_WithPromptEngineerRole() throws Exception {
        mockMvc.perform(put("/api/v1/prompts/" + UUID.randomUUID())
                .content("Nuevo prompt optimizado")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
