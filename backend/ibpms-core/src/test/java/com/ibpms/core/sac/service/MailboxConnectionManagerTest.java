package com.ibpms.core.sac.service;

import com.ibpms.core.sac.exception.ConnectionValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailboxConnectionManagerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MailboxConnectionManager connectionManager;

    @BeforeEach
    void setUp() {
        // Enforce using the mocked RestTemplate
        ReflectionTestUtils.setField(connectionManager, "restTemplate", restTemplate);
    }

    @Test
    void testValidateGraphConnection_Success_ShouldNotThrow() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"access_token\":\"mocked-token\"}", HttpStatus.OK));

        assertDoesNotThrow(() -> 
            connectionManager.validateGraphConnection("a-tenant", "a-client", "a-secret")
        );
    }

    @Test
    void testValidateGraphConnection_Unauthorized_ShouldThrowConnectionValidationException() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid client secret"));

        assertThrows(ConnectionValidationException.class, () -> 
            connectionManager.validateGraphConnection("bad-tenant", "bad-client", "bad-secret")
        );
    }
}
