package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.ServiceAccountEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.ServiceAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ServiceAccountService {

    private final ServiceAccountRepository serviceAccountRepository;
    private final RoleService roleService;
    private final SecureRandom secureRandom = new SecureRandom();

    public ServiceAccountService(ServiceAccountRepository serviceAccountRepository, RoleService roleService) {
        this.serviceAccountRepository = serviceAccountRepository;
        this.roleService = roleService;
    }

    // @Traceability: Retro-Remediación ADR-001
    public Map<String, Object> createServiceAccount(String name, String description, String roleIdStr) throws NoSuchAlgorithmException {
        RoleEntity role = roleService.findById(UUID.fromString(roleIdStr))
                .orElseThrow(() -> new IllegalArgumentException("Role no encontrado"));

        // CA-10: Generador Máquina-a-Máquina Seguro (Opaco)
        byte[] keyBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);
        String rawApiKey = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);

        // Hashing Criptográfico SHA-256 (Never store plain keys)
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(rawApiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String hashedKey = hexString.toString();

        ServiceAccountEntity account = new ServiceAccountEntity(name, description, hashedKey, role);
        serviceAccountRepository.save(account);

        Map<String, Object> response = new HashMap<>();
        response.put("id", account.getId());
        response.put("name", account.getName());
        response.put("plainApiKey", rawApiKey);
        response.put("message", "GUARDE LA API KEY. NUNCA PODRÁ VOLVER A VISUALIZARLA.");

        return response;
    }
}
