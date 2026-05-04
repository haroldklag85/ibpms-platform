package com.ibpms.poc.application.util;

import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityContextUtilsTest {

    @AfterEach
    @BeforeEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAssignee_shouldReturnNameFromAuthentication() {
        var auth = new UsernamePasswordAuthenticationToken("john_doe", "password", java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = SecurityContextUtils.getAssignee();

        assertThat(result).isEqualTo("john_doe");
    }

    @Test
    void getAssignee_shouldReturnSystemWhenUnauthenticated() {
        String result = SecurityContextUtils.getAssignee();
        assertThat(result).isEqualTo("system");
    }

    @Test
    void getTenantId_shouldReturnTenantFromJwtClaim() {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "none"), Map.of("tenant_id", "tenant_xyz"));
        var auth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = SecurityContextUtils.getTenantId();

        assertThat(result).isEqualTo("tenant_xyz");
    }

    @Test
    void getTenantId_shouldThrowExceptionWhenNoTenantFound() {
        var auth = new UsernamePasswordAuthenticationToken("john_doe", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(IllegalStateException.class, SecurityContextUtils::getTenantId);
    }
}
