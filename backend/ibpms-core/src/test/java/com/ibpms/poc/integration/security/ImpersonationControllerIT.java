package com.ibpms.poc.integration.security;

import com.ibpms.poc.AbstractIntegrationIT;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.DelegationRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImpersonationControllerIT extends AbstractIntegrationIT {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DelegationRepository delegationRepository;

    private UserEntity superAdmin;
    private UserEntity operador;
    private UserEntity otroSuperAdmin;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        delegationRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        RoleEntity adminRole = new RoleEntity("ROLE_SUPER_ADMIN", "Super Admin");
        RoleEntity operadorRole = new RoleEntity("ROLE_OPERARIO", "Operario");
        roleRepository.saveAll(List.of(adminRole, operadorRole));

        superAdmin = new UserEntity();
        superAdmin.setUsername("super_admin");
        superAdmin.setEmail("admin@test.com");
        superAdmin.getRoles().add(adminRole);
        superAdmin = userRepository.save(superAdmin);

        operador = new UserEntity();
        operador.setUsername("operador");
        operador.setEmail("op@test.com");
        operador.getRoles().add(operadorRole);
        operador = userRepository.save(operador);

        otroSuperAdmin = new UserEntity();
        otroSuperAdmin.setUsername("otro_admin");
        otroSuperAdmin.setEmail("otro@test.com");
        otroSuperAdmin.getRoles().add(adminRole);
        otroSuperAdmin = userRepository.save(otroSuperAdmin);
    }

    @Test
    void superAdminImpersonaOperador_Success() {
        String adminToken = jwtTokenProvider.generateToken(superAdmin.getUsername(), List.of("SUPER_ADMIN"), "tenant1");

        String responseToken = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/admin/impersonate/" + operador.getId())
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .extract().path("token");

        String impersonatedBy = jwtTokenProvider.getClaim(responseToken, "impersonatedBy");
        org.junit.jupiter.api.Assertions.assertEquals(superAdmin.getId().toString(), impersonatedBy);
        org.junit.jupiter.api.Assertions.assertEquals("operador", jwtTokenProvider.getSubject(responseToken));
    }

    @Test
    void superAdminImpersonaSuperAdmin_Forbidden() {
        String adminToken = jwtTokenProvider.generateToken(superAdmin.getUsername(), List.of("SUPER_ADMIN"), "tenant1");

        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/admin/impersonate/" + otroSuperAdmin.getId())
        .then()
            .statusCode(403)
            .body("error", notNullValue());
    }

    @Test
    void operadorIntentaImpersonar_Forbidden() {
        String opToken = jwtTokenProvider.generateToken(operador.getUsername(), List.of("OPERARIO"), "tenant1");

        given()
            .header("Authorization", "Bearer " + opToken)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/admin/impersonate/" + superAdmin.getId())
        .then()
            .statusCode(403);
    }

    @Test
    void exitImpersonation_Success() {
        String impersonationToken = jwtTokenProvider.generateImpersonationToken(
                operador.getUsername(), List.of("OPERARIO"), "tenant1", superAdmin.getId().toString());

        String exitToken = given()
            .header("Authorization", "Bearer " + impersonationToken)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/admin/impersonate/exit")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .extract().path("token");

        String impersonatedBy = jwtTokenProvider.getClaim(exitToken, "impersonatedBy");
        org.junit.jupiter.api.Assertions.assertNull(impersonatedBy);
        org.junit.jupiter.api.Assertions.assertEquals("super_admin", jwtTokenProvider.getSubject(exitToken));
    }

    @Test
    void jwtImpersonadoExpiraEn30Min() {
        String adminToken = jwtTokenProvider.generateToken(superAdmin.getUsername(), List.of("SUPER_ADMIN"), "tenant1");

        String responseToken = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/admin/impersonate/" + operador.getId())
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .extract().path("token");

        io.jsonwebtoken.Claims claims = jwtTokenProvider.parseClaims(responseToken);
        long exp = claims.getExpiration().getTime();
        long iat = claims.getIssuedAt().getTime();
        
        long diffSeconds = (exp - iat) / 1000;
        assertTrue(diffSeconds <= 1800, "El token debe expirar como máximo en 30 minutos");
    }
}
