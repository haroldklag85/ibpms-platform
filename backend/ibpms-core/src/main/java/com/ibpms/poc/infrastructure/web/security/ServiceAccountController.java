package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.ServiceAccountEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.ServiceAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/service-accounts")
@SuppressWarnings("null")
public class ServiceAccountController {

    private final ServiceAccountRepository serviceAccountRepository;
    private final RoleRepository roleRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ServiceAccountController(ServiceAccountRepository serviceAccountRepository, RoleRepository roleRepository) {
        this.serviceAccountRepository = serviceAccountRepository;
        this.roleRepository = roleRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createServiceAccount(@RequestBody Map<String, String> request) throws NoSuchAlgorithmException {
        String name = request.get("name");
        String roleIdStr = request.get("roleId");

        RoleEntity role = roleRepository.findById(UUID.fromString(roleIdStr))
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
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(365);
        if (request.containsKey("daysToExpire")) {
            long days = Long.parseLong(request.get("daysToExpire"));
            if (days > 730) {
                throw new IllegalArgumentException("La expiración no puede exceder los 730 días (Políticas CISO).");
            }
            expiresAt = LocalDateTime.now().plusDays(days);
        }

        ServiceAccountEntity account = new ServiceAccountEntity(name, request.get("description"), hashedKeyWithSalt, role, expiresAt);
        serviceAccountRepository.save(account);
 
        Map<String, Object> response = new HashMap<>();
        response.put("id", account.getId());
        response.put("name", account.getName());
        response.put("plainApiKey", rawApiKey);
        response.put("expiresAt", expiresAt);
        response.put("message", "GUARDE LA API KEY. NUNCA PODRÁ VOLVER A VISUALIZARLA.");
 
        return ResponseEntity.ok(response);
    }
}
