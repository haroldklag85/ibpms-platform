package com.ibpms.poc.infrastructure.security;

import com.ibpms.poc.domain.port.EncryptionKeyProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class EnvEncryptionKeyProvider implements EncryptionKeyProvider {

    private final byte[] keyBytes;

    public EnvEncryptionKeyProvider(@Value("${ibpms.pii.encryption.key}") String encryptionKeyString) {
        if (encryptionKeyString == null || encryptionKeyString.length() != 32) {
            throw new IllegalArgumentException("IBPMS_PII_ENCRYPTION_KEY debe estar presente y ser de exactamente 32 caracteres (AES-256).");
        }
        this.keyBytes = encryptionKeyString.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getEncryptionKey() {
        return keyBytes;
    }
}
