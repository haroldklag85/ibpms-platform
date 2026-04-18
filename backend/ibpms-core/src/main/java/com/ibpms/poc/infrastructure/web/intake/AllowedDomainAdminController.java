package com.ibpms.poc.infrastructure.web.intake;

import com.ibpms.poc.domain.model.AllowedDomain;
import com.ibpms.poc.domain.port.AllowedDomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin CRUD controller for webhook domain whitelist (US-004 CA-12).
 * Only accessible by ADMIN_SISTEMA or ADMIN_TENANT roles.
 */
@RestController
@RequestMapping("/admin/webhook/allowed-domains")
public class AllowedDomainAdminController {

    private static final Logger log = LoggerFactory.getLogger(AllowedDomainAdminController.class);
    private final AllowedDomainRepository repository;

    public AllowedDomainAdminController(AllowedDomainRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(hidden = true)
    public ResponseEntity<List<AllowedDomain>> listDomains(
            @RequestParam(defaultValue = "default") String tenantId) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "GAP-4 [US-045]: AllowedDomain Admin — pendiente refinamiento Sprint asignado.");
    }

    @PostMapping
    @Operation(hidden = true)
    public ResponseEntity<AllowedDomain> addDomain(@RequestBody Map<String, String> body) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "GAP-4 [US-045]: AllowedDomain Admin — pendiente refinamiento Sprint asignado.");
    }

    @DeleteMapping("/{id}")
    @Operation(hidden = true)
    public ResponseEntity<Void> deactivateDomain(@PathVariable UUID id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "GAP-4 [US-045]: AllowedDomain Admin — pendiente refinamiento Sprint asignado.");
    }
}
