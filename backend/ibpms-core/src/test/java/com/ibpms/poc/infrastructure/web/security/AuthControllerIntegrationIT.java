// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.AbstractLocalE2EIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
public class AuthControllerIntegrationIT extends AbstractLocalE2EIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("US-038 CA-4: Protocolo Break-Glass - Permite Login de Emergencia Local bypaseando EntraID")
    void testBreakGlassProtocol_EmergencyLogin_Returns200() throws Exception {
        // En una catástrofe OAUTH2, el administrador puede ir directamente al endpoint break-glass.
        // Dado el SecurityConfig actualizado, este endpoint es '.permitAll()'.
        
        String emergencyPayload = """
                {
                   "email": "root@ibpms.local",
                   "password": "Root#Temp4Sys"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/emergency-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emergencyPayload))
                .andExpect(status().isOk());
    }
}
