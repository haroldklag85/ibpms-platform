package com.ibpms.poc.infrastructure.security;

import com.ibpms.poc.domain.port.EncryptionKeyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PiiEncryptionServiceTest {

    private PiiEncryptionService piiEncryptionService;

    @BeforeEach
    void setUp() {
        // Mock a 32-byte key for AES-256
        String dummyKey = "12345678901234567890123456789012";
        EncryptionKeyProvider provider = () -> dummyKey.getBytes(StandardCharsets.UTF_8);
        piiEncryptionService = new PiiEncryptionService(provider);
    }

    @Test
    void pii_encryption_round_trip_success() {
        String plainText = "{\"salary\": 5000, \"name\": \"John Doe\"}";
        
        String cipherText = piiEncryptionService.encrypt(plainText);
        
        // Assert it is encrypted and not visible in raw
        assertThat(cipherText).isNotEqualTo(plainText);
        assertThat(cipherText).doesNotContain("salary");
        assertThat(cipherText).doesNotContain("John Doe");

        String decryptedText = piiEncryptionService.decrypt(cipherText);
        
        // Assert it equals original mathematically
        assertThat(decryptedText).isEqualTo(plainText);
    }
    
    @Test
    void decrypt_invalid_payload_throws_exception() {
        String invalidPayload = "short"; // Not base64, no IV
        assertThatThrownBy(() -> piiEncryptionService.decrypt(invalidPayload))
                .isInstanceOf(RuntimeException.class);
    }
}
