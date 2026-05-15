package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.service.security.ServiceAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/service-accounts")
@SuppressWarnings("null")
public class ServiceAccountController {

    private final ServiceAccountService serviceAccountService;

    public ServiceAccountController(ServiceAccountService serviceAccountService) {
        this.serviceAccountService = serviceAccountService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createServiceAccount(@RequestBody Map<String, String> request) throws NoSuchAlgorithmException {
        String name = request.get("name");
        String roleIdStr = request.get("roleId");
        String daysToExpireStr = request.get("daysToExpire");

        // @Traceability(US="US-036", CA="CA-01", DESC="ADR-001 Refactor: Generación M2M y Hashing delegados a capa de Servicio. CISO Expiration Policy pasada como argumento.")
        Map<String, Object> response = serviceAccountService.createServiceAccount(name, request.get("description"), roleIdStr, daysToExpireStr);
        
        return ResponseEntity.ok(response);
    }
}
