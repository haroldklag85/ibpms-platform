package com.ibpms.poc.infrastructure.web.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("US-038 CA-4: Protocolo Break-Glass - Permite Login de Emergencia Local bypaseando EntraID")
    void testBreakGlassProtocol_EmergencyLogin_Returns200() throws Exception {
        // En una catástrofe OAUTH2, el administrador puede ir directamente al endpoint break-glass.
        // Dado el SecurityConfig actualizado, este endpoint es '.permitAll()'.
        
        String emergencyPayload = """
                {
                   "username": "sys_emergency_admin",
                   "password": "EmergencySecretPassword2026!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/emergency-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emergencyPayload))
                // Si el bypass de OAUTH2 y JWT funciona para este endpoint crudo, retornará algo (200 OK o 4XX Client Error si auth falla, pero NO 401/403 Security Header block)
                // Para el propósito del test, asumimos que devuelve 200 con el Bearer Token Break-Glass.
                .andExpect(status().isOk());
    }
}
