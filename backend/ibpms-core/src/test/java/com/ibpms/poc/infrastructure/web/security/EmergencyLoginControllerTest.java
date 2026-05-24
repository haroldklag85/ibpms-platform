package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.SecurityAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.SecurityAuditLogRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @Traceability(US = "US-038", CA = "CA-04")
 * TDD: Validación del Protocolo Break-Glass.
 */
class EmergencyLoginControllerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SecurityAuditLogRepository securityAuditLogRepository;

    @InjectMocks
    private EmergencyLoginController emergencyLoginController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emergencyLoginController, "emergencySecret", "test-secret");
    }

    @Test
    void emergencyLogin_shouldReturnUnauthorized_whenSecretIsInvalid() {
        EmergencyLoginController.EmergencyLoginRequest req = new EmergencyLoginController.EmergencyLoginRequest("wrong-secret", "emergencia");
        MockHttpServletRequest mockReq = new MockHttpServletRequest();

        ResponseEntity<Map<String, String>> response = emergencyLoginController.emergencyLogin(req, mockReq);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(securityAuditLogRepository, never()).save(any());
    }

    @Test
    void emergencyLogin_shouldReturnBadRequest_whenJustificationIsBlank() {
        EmergencyLoginController.EmergencyLoginRequest req = new EmergencyLoginController.EmergencyLoginRequest("test-secret", "");
        MockHttpServletRequest mockReq = new MockHttpServletRequest();

        ResponseEntity<Map<String, String>> response = emergencyLoginController.emergencyLogin(req, mockReq);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(securityAuditLogRepository, never()).save(any());
    }

    @Test
    void emergencyLogin_shouldReturnToken_andAudit_whenValid() {
        EmergencyLoginController.EmergencyLoginRequest req = new EmergencyLoginController.EmergencyLoginRequest("test-secret", "Caída de SSO principal");
        MockHttpServletRequest mockReq = new MockHttpServletRequest();
        mockReq.setRemoteAddr("127.0.0.1");

        when(jwtTokenProvider.generateToken(anyString(), anyList(), anyString())).thenReturn("mocked.jwt.token");

        ResponseEntity<Map<String, String>> response = emergencyLoginController.emergencyLogin(req, mockReq);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mocked.jwt.token", response.getBody().get("access_token"));
        
        verify(securityAuditLogRepository, times(1)).save(argThat((SecurityAuditLogEntity audit) -> 
            audit.getIsBreakGlass() && 
            "Caída de SSO principal".equals(audit.getJustification()) &&
            "127.0.0.1".equals(audit.getIpAddress())
        ));
    }
}
