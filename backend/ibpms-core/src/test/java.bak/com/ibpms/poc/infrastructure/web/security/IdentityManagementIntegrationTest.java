package com.ibpms.poc.infrastructure.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.security.UserCreateRequestDTO;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.AfterEach;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ibpms.poc.AbstractIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
public class IdentityManagementIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private UUID existingUserId;

    @BeforeEach
    void setUp() {
        // Prepare a user for testing
        UserEntity user = new UserEntity();
        user.setUsername("test_operator");
        user.setEmail("test@operator.com");
        user.setPasswordHash("hashed_temp_password");
        user.setIsActive(true);
        user = userRepository.saveAndFlush(user);
        this.existingUserId = user.getId();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("CA-2: Zero-Tolerance Entropy - Contraseña débil debe ser rechazada (HTTP 400)")
    void testZeroToleranceEntropy() throws Exception {
        UserCreateRequestDTO request = new UserCreateRequestDTO();
        request.setUsername("hacker");
        request.setEmail("hacker@malicious.com");
        request.setPassword("123456"); // Missing upper, special, and length

        mockMvc.perform(post("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.fields[0].field").value("password"));
    }

    @Test
    @DisplayName("CA-5: Kill Switch - Desactivar usuario invalida sesión (HTTP 200 OK con payload de expulsión)")
    void testKillSwitchIsolation() throws Exception {
        // Enviar petición de kill-session asumiendo que el admin presiona el botón
        mockMvc.perform(post("/api/v1/admin/users/" + existingUserId + "/kill-session")
                .header("Authorization", "Bearer mock-jwt-token-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        // Verificamos que el usuario quedó inactivo en BD
        UserEntity user = userRepository.findById(existingUserId).orElseThrow();
        assert !user.getIsActive();
    }

    @Test
    @DisplayName("US-036 p2: Soft-Delete Guard - Un DELETE físico debe ser repudiado con HTTP 405")
    void testSoftDeleteGuardReturns405() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/" + existingUserId))
                .andExpect(status().isMethodNotAllowed()); // The backend must repudiate this verb to protect DB integrity
    }
}
