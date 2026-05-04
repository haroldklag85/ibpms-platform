package com.ibpms.poc.infrastructure.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.security.UserCreateRequestDTO;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.restassured.RestAssured;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;
import java.util.List;
import static org.hamcrest.Matchers.*;



import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.AfterEach;

import java.util.UUID;


import com.ibpms.poc.AbstractIntegrationTest;

@SuppressWarnings("null")
public class IdentityManagementIntegrationTest extends AbstractIntegrationTest {


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;


    private UUID existingUserId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        adminToken = jwtTokenProvider.generateToken("test-admin", List.of("ROLE_SUPER_ADMIN"), "tenant1");

        RestAssured.port = port;
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

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/api/v1/admin/users")
                .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("fields[0].field", equalTo("password"));
    }

    @Test
    @DisplayName("CA-5: Kill Switch - Desactivar usuario invalida sesión (HTTP 200 OK con payload de expulsión)")
    void testKillSwitchIsolation() throws Exception {
        // Enviar petición de kill-session asumiendo que el admin presiona el botón
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/api/v1/admin/users/" + existingUserId + "/kill-session")
                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"));

        // Verificamos que el usuario quedó inactivo en BD
        UserEntity user = userRepository.findById(existingUserId).orElseThrow();
        assert !user.getIsActive();
    }

    @Test
    @DisplayName("US-036 p2: Soft-Delete Guard - Un DELETE físico debe ser repudiado con HTTP 405")
    void testSoftDeleteGuardReturns405() throws Exception {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/v1/admin/users/" + existingUserId)
                .then()
                .statusCode(405);
    }
}
