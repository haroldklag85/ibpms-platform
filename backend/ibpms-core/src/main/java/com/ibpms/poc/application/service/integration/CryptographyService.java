package com.ibpms.poc.application.service.integration;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Servicio de Seguridad y Criptografía Avanzada (Pantalla 8).
 * Implementa auto-firmado HMAC-SHA256 y Encriptación PGP (Simulada para V1).
 */
@Service
public class CryptographyService {

    private static final String HMAC_ALGO = "HmacSHA256";

    /**
     * CA-17: Genera la firma HMAC-SHA256 de un payload dado usando una clave
     * secreta.
     */
    public String generateHmacSha256Signature(String payload, String secretKey) {
        if (payload == null || secretKey == null) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generando firma HMAC-SHA256", e);
        }
    }

    /**
     * CA-25: Encriptación PGP usando la llave pública del sistema destino.
     * Nota: En esta PoC V1 se provee el esqueleto base. La integración BouncyCastle
     * completa
     * se omitirá por brevedad, simulando la salida.
     */
    public String encryptPayloadPgp(String plainTextPayload, String pgpPublicKey) {
        if (plainTextPayload == null || pgpPublicKey == null || pgpPublicKey.isBlank()) {
            return plainTextPayload; // No encriptar si no hay llave
        }

        // Simulación de encriptación PGP (En producción usaría org.bouncycastle)
        try {
            String simulatedEncrypted = Base64.getEncoder()
                    .encodeToString(plainTextPayload.getBytes(StandardCharsets.UTF_8));
            return "-----BEGIN PGP MESSAGE-----\n\n" +
                    simulatedEncrypted + "\n" +
                    "-----END PGP MESSAGE-----";
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando payload PGP", e);
        }
    }
}
