package com.ibpms.poc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * Servicio para la gestión de cuentas de servicio y API Keys (CA-22 / US-036).
 */
@Service
public class ServiceAccountManager {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceAccountManager.class);
    private final SecureRandom secureRandom = new SecureRandom();

    public record ServiceAccountKeysDTO(String apiKeyPlain, String apiKeyHash, String serviceAccountId) {}

    /**
     * Genera una nueva llave de API (API Key) usando SecureRandom y su Hash SHA-256 respectivo.
     * @param clientName Nombre del cliente (Service Account).
     * @return ServiceAccountKeysDTO con la llave en plano y el hash.
     */
    public ServiceAccountKeysDTO generateApiKey(String clientName) {
        log.info("Audit: Generando nueva API Key para la cuenta de servicio: {}", clientName);
        
        // Generar 32 bytes de entropía
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String apiKeyPlain = "ibpms_sk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        String apiKeyHash = hashString(apiKeyPlain);
        String saId = UUID.randomUUID().toString();
        
        // En un entorno de producción, insertamos en ibpms_service_accounts aquí llamando a Repository
        log.warn("SUDO Action [Audit Trail]: API Key generada para Service Account [{}] (ID: {}). La clave plana solo se revelará esta vez.", clientName, saId);

        return new ServiceAccountKeysDTO(apiKeyPlain, apiKeyHash, saId);
    }
    
    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Critial Error: Algoritmo de Hashing SHA-256 no encontrado.", e);
            throw new RuntimeException("SHA-256 algorith not found", e);
        }
    }
}
