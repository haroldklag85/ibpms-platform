package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.infrastructure.jpa.entity.IbpmsProfileEntity;
import com.ibpms.poc.infrastructure.jpa.entity.IdpGroupMappingEntity;
import com.ibpms.poc.infrastructure.jpa.entity.ProfileBpmnAssignmentEntity;
import com.ibpms.poc.application.service.RbacAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.List;

/**
 * Controller de Administración de Seguridad y RBAC.
 * Proporciona Endpoints de mantenimiento a los Frontends de configuración.
 * Nota: Estos endpoints deberían requerir un Authority / Role de SUPER_ADMIN en
 * producción.
 */
@RestController
@RequestMapping("/api/v1/admin/security")
@Traceability(US = "US-036", CA = {"CA-01"})
public class RbacAdminController {

    private final RbacAdminService rbacAdminService;

    public RbacAdminController(RbacAdminService rbacAdminService) {
        this.rbacAdminService = rbacAdminService;
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<IbpmsProfileEntity>> getAllProfiles() {
        // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
        return ResponseEntity.ok(rbacAdminService.getAllProfiles());
    }

    @PostMapping("/profiles")
    public ResponseEntity<IbpmsProfileEntity> createProfile(@RequestBody IbpmsProfileEntity profile) {
        // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
        return ResponseEntity.ok(rbacAdminService.saveProfile(java.util.Objects.requireNonNull(profile)));
    }

    @GetMapping("/mappings")
    public ResponseEntity<List<IdpGroupMappingEntity>> getAllMappings() {
        // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
        return ResponseEntity.ok(rbacAdminService.getAllMappings());
    }

    @PostMapping("/mappings")
    public ResponseEntity<IdpGroupMappingEntity> addMapping(@RequestBody IdpGroupMappingEntity mapping) {
        // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
        return ResponseEntity.ok(rbacAdminService.saveMapping(java.util.Objects.requireNonNull(mapping)));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<ProfileBpmnAssignmentEntity>> getAllAssignments() {
        // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
        return ResponseEntity.ok(rbacAdminService.getAllAssignments());
    }

    @PostMapping("/assignments")
    public ResponseEntity<ProfileBpmnAssignmentEntity> addAssignment(
            @RequestBody ProfileBpmnAssignmentEntity assignment) {
        // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
        return ResponseEntity.ok(rbacAdminService.saveAssignment(java.util.Objects.requireNonNull(assignment)));
    }
}
