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
        noRoleUser.setIsActive(true);
        noRoleUser.setRoles(new HashSet<>()); // No roles assigned

        when(userRepository.findByEmail(defaultEmail)).thenReturn(Optional.of(noRoleUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        Map<String, String> creds = Map.of("email", defaultEmail, "password", rawPassword);

        assertThrows(AccessDeniedException.class, () -> {
            authSyncController.emergencyLogin(creds);
        });
    }
}
