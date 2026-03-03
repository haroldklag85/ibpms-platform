package com.ibpms.poc.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboundDispatcherTest {

    private final CryptographyService mockCrypto = Mockito.mock(CryptographyService.class);
    private final OutboundDispatcher dispatcher = new OutboundDispatcher(mockCrypto);

    // ── QA Instruction: APIM SSRF Defense (CA-12) ──
    @Test
    @DisplayName("Debe bloquear y arrojar SecurityException si se intenta despachar hacia Localhost (127.0.0.1)")
    void dispatch_BlocksLocalhost_ThrowsSecurityException() {
        String mockPayload = "{\"data\":\"test\"}";

        SecurityException ex = assertThrows(SecurityException.class, () -> {
            dispatcher.dispatchRestJson("http://127.0.0.1:8080/apim/fake", mockPayload, null);
        });

        assertTrue(ex.getMessage().contains("red local"));
    }

    @Test
    @DisplayName("Debe bloquear y arrojar SecurityException si se intentan extraer Metadatos Cloud (169.254.169.254)")
    void dispatch_BlocksCloudMetadata_ThrowsSecurityException() {
        String mockPayload = "{\"data\":\"test\"}";

        SecurityException ex = assertThrows(SecurityException.class, () -> {
            dispatcher.dispatchRestJson("http://169.254.169.254/latest/meta-data/iam/security-credentials/",
                    mockPayload, null);
        });

        assertTrue(ex.getMessage().contains("metadatos Cloud"));
    }
}
