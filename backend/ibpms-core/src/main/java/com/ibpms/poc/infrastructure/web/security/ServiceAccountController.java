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

        // @Traceability: US-036 - CA-01 (ADR-001 Refactor)
        Map<String, Object> response = serviceAccountService.createServiceAccount(name, request.get("description"), roleIdStr);
        return ResponseEntity.ok(response);
    }
}
