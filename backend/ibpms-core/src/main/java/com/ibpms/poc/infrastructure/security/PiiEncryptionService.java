package com.ibpms.poc.infrastructure.security;

import com.ibpms.poc.domain.port.EncryptionKeyProvider;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PiiEncryptionService {

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128 bits auth tag

    private final EncryptionKeyProvider keyProvider;
    private final SecureRandom secureRandom;

    public PiiEncryptionService(EncryptionKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
        this.secureRandom = new SecureRandom();
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(keyProvider.getEncryptionKey(), ENCRYPTION_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Formato dictado: IV (12 bytes) prepended to ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando campo PII preventivo.", e);
        }
    }

    public String decrypt(String base64CipherText) {
        if (base64CipherText == null || base64CipherText.isEmpty()) {
            return base64CipherText;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(base64CipherText);

            if (decoded.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Payload muy corto, no contiene IV.");
            }

            // Extraer IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, GCM_IV_LENGTH);

            // Extraer ciphertext real
            int cipherTextLength = decoded.length - GCM_IV_LENGTH;
            byte[] cipherText = new byte[cipherTextLength];
            System.arraycopy(decoded, GCM_IV_LENGTH, cipherText, 0, cipherTextLength);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(keyProvider.getEncryptionKey(), ENCRYPTION_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, java.nio.charset.StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Error desencriptando campo PII preventivo.", e);
        }
    }
}
