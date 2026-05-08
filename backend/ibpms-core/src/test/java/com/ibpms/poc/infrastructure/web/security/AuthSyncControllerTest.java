package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AuthSyncControllerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthSyncController authSyncController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testEmergencyLoginFailsWithoutRoles() {
        String defaultEmail = "admin@alpha.com";
        String rawPassword = "Test123!";
        String encodedPassword = "encodedPassword";

        UserEntity noRoleUser = new UserEntity();
        noRoleUser.setUsername("admin");
        noRoleUser.setEmail(defaultEmail);
        noRoleUser.setPasswordHash(encodedPassword);
        noRoleUser.setStatus(com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus.ACTIVE);
        noRoleUser.setRoles(new HashSet<>()); // No roles assigned

        when(userRepository.findByEmail(defaultEmail)).thenReturn(Optional.of(noRoleUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        Map<String, String> creds = Map.of("email", defaultEmail, "password", rawPassword);

        assertThrows(AccessDeniedException.class, () -> {
            authSyncController.emergencyLogin(creds);
        });
    }
    @Test
    public void testEmergencyLoginMissingFields() {
        Map<String, String> creds = Map.of("email", "test@alpha.com"); // missing password
        org.springframework.http.ResponseEntity<?> response = authSyncController.emergencyLogin(creds);
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        org.junit.jupiter.api.Assertions.assertEquals("MISSING_FIELDS", body.get("code"));
    }

    @Test
    public void testEmergencyLoginUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        Map<String, String> creds = Map.of("email", "no@alpha.com", "password", "123");
        org.springframework.http.ResponseEntity<?> response = authSyncController.emergencyLogin(creds);
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        org.junit.jupiter.api.Assertions.assertEquals("USER_NOT_FOUND", body.get("code"));
    }

    @Test
    public void testEmergencyLoginInvalidPassword() {
        UserEntity user = new UserEntity();
        user.setPasswordHash("hash");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Map<String, String> creds = Map.of("email", "admin@alpha.com", "password", "wrong");
        org.springframework.http.ResponseEntity<?> response = authSyncController.emergencyLogin(creds);
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        org.junit.jupiter.api.Assertions.assertEquals("INVALID_PASSWORD", body.get("code"));
    }

    @Test
    public void testEmergencyLoginAccountDisabled() {
        UserEntity user = new UserEntity();
        user.setPasswordHash("hash");
        user.setStatus(com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus.INACTIVE);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        Map<String, String> creds = Map.of("email", "admin@alpha.com", "password", "correct");
        org.springframework.http.ResponseEntity<?> response = authSyncController.emergencyLogin(creds);
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, String> body = (Map<String, String>) response.getBody();
        org.junit.jupiter.api.Assertions.assertEquals("ACCOUNT_DISABLED", body.get("code"));
    }
}
