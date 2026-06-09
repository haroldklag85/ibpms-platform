package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.ServiceAccountEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.ServiceAccountRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de Aplicación para Cuentas de Servicio.
 * Gestiona la lógica de negocio y persistencia de cuentas Service-to-Service (M2M).
 * 
 * @Traceability(US = "US-036", CA = {"CA-01"})
 */
@Service
@Transactional
@Traceability(US = "US-036", CA = {"CA-01"})
public class ServiceAccountService {

    private final ServiceAccountRepository serviceAccountRepository;
    private final RoleService roleService;
    private final SecureRandom secureRandom = new SecureRandom();

    public ServiceAccountService(ServiceAccountRepository serviceAccountRepository, RoleService roleService) {
        this.serviceAccountRepository = serviceAccountRepository;
        this.roleService = roleService;
    }

    /**
     * Crea una cuenta de servicio con su respectivo token y claves.
     * @param name Nombre de la cuenta.
     * @param description Descripción (opcional).
     * @param roleIdStr UUID del rol asociado.
     * @return Mapa con las credenciales de la cuenta.
     * @throws Exception en caso de fallo en encriptación.
     */
    // @Traceability: US-036 - CA-01 / CA-10 / CA-22 (ADR-001 Refactor: Delegación de Criptografía M2M y CISO)
    public Map<String, Object> createServiceAccount(String name, String description, String roleIdStr, String daysToExpireStr) throws java.security.NoSuchAlgorithmException {
        RoleEntity role = roleService.findById(UUID.fromString(roleIdStr))
                .orElseThrow(() -> new IllegalArgumentException("Role no encontrado"));

        // CA-10: Generador Máquina-a-Máquina Seguro (Opaco)
        byte[] keyBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);
        String rawApiKey = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);

        // Hashing Criptográfico SHA-256 con Salt (CA-22)
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        String saltStr = Base64.getEncoder().encodeToString(salt);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        byte[] hash = digest.digest(rawApiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String hashedKeyWithSalt = saltStr + ":" + hexString.toString();

        // CA-22: Fecha de expiración obligatoria (default 365 días)
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now().plusDays(365);
        if (daysToExpireStr != null && !daysToExpireStr.isBlank()) {
            long days = Long.parseLong(daysToExpireStr);
            if (days > 730) {
                throw new IllegalArgumentException("La expiración no puede exceder los 730 días (Políticas CISO).");
            }
            expiresAt = java.time.LocalDateTime.now().plusDays(days);
        }

        ServiceAccountEntity account = new ServiceAccountEntity(name, description, hashedKeyWithSalt, role, expiresAt);
        serviceAccountRepository.save(account);

        Map<String, Object> response = new HashMap<>();
        response.put("id", account.getId());
        response.put("name", account.getName());
        response.put("plainApiKey", rawApiKey);
        response.put("expiresAt", expiresAt);
        response.put("message", "GUARDE LA API KEY. NUNCA PODRÁ VOLVER A VISUALIZARLA.");

        return response;
    }
}
