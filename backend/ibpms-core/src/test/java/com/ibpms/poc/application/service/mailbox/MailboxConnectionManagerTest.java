package com.ibpms.poc.application.service.mailbox;

import com.ibpms.poc.domain.exception.ConnectionValidationException;
import com.ibpms.poc.infrastructure.jpa.entity.SacMailboxEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailboxConnectionManagerTest {

    @InjectMocks
    private MailboxConnectionManager mailboxConnectionManager;

    private RestTemplate restTemplateMock;

    @BeforeEach
    void setUp() {
        restTemplateMock = mock(RestTemplate.class);
        // Inyectar el RestTemplate mockeado en la instancia real que lo inicia en el
        // constructor
        ReflectionTestUtils.setField(mailboxConnectionManager, "restTemplate", restTemplateMock);
    }

    @Test
    void whenProtocolIsImap_thenThrowConnectionValidationException() {
        // Arrange
        SacMailboxEntity mailbox = new SacMailboxEntity();
        mailbox.setAlias("Soporte IMAP Inseguro");
        mailbox.setProtocol("IMAP");

        // Act & Assert
        ConnectionValidationException exception = assertThrows(ConnectionValidationException.class,
                () -> mailboxConnectionManager.validateConnection(mailbox, "secret123"));

        assertTrue(exception.getMessage().contains("Legacy protocols like IMAP are prohibited."));
    }

    @Test
    void whenGraphApiRejectsCredentials_thenThrowException() {
        // Arrange
        SacMailboxEntity mailbox = new SacMailboxEntity();
        mailbox.setAlias("Soporte Segudo Graph");
        mailbox.setProtocol("GRAPH");
        mailbox.setTenantId("tenant.onmicrosoft.com");
        mailbox.setClientId("test-client");

        // Simular que Microsoft rechaza el token con HttpClientErrorException 401
        // Unauthorized
        when(restTemplateMock.postForEntity(any(String.class), any(Object.class), eq(String.class)))
                .thenThrow(mock(HttpClientErrorException.class));

        // Act & Assert
        ConnectionValidationException exception = assertThrows(ConnectionValidationException.class,
                () -> mailboxConnectionManager.validateConnection(mailbox, "wrong-secret"));

        assertTrue(exception.getMessage().contains("Connection refused by Microsoft Azure"));
    }
}
