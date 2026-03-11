package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.infrastructure.jpa.entity.IbpmsProfileEntity;
import com.ibpms.poc.infrastructure.jpa.entity.IdpGroupMappingEntity;
import com.ibpms.poc.infrastructure.jpa.entity.ProfileBpmnAssignmentEntity;
import com.ibpms.poc.infrastructure.jpa.repository.IbpmsProfileRepository;
import com.ibpms.poc.infrastructure.jpa.repository.IdpGroupMappingRepository;
import com.ibpms.poc.infrastructure.jpa.repository.ProfileBpmnAssignmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de Administración de Seguridad y RBAC.
 * Proporciona Endpoints de mantenimiento a los Frontends de configuración.
 * Nota: Estos endpoints deberían requerir un Authority / Role de SUPER_ADMIN en
 * producción.
 */
@RestController
@RequestMapping("/api/v1/admin/security")
public class RbacAdminController {

    private final IbpmsProfileRepository profileRepository;
    private final IdpGroupMappingRepository mappingRepository;
    private final ProfileBpmnAssignmentRepository assignmentRepository;

    public RbacAdminController(IbpmsProfileRepository profileRepository,
            IdpGroupMappingRepository mappingRepository,
            ProfileBpmnAssignmentRepository assignmentRepository) {
        this.profileRepository = profileRepository;
        this.mappingRepository = mappingRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<IbpmsProfileEntity>> getAllProfiles() {
        return ResponseEntity.ok(profileRepository.findAll());
    }

    @PostMapping("/profiles")
    public ResponseEntity<IbpmsProfileEntity> createProfile(@RequestBody IbpmsProfileEntity profile) {
        return ResponseEntity.ok(profileRepository.save(java.util.Objects.requireNonNull(profile)));
    }

    @GetMapping("/mappings")
    public ResponseEntity<List<IdpGroupMappingEntity>> getAllMappings() {
        return ResponseEntity.ok(mappingRepository.findAll());
    }

    @PostMapping("/mappings")
    public ResponseEntity<IdpGroupMappingEntity> addMapping(@RequestBody IdpGroupMappingEntity mapping) {
        return ResponseEntity.ok(mappingRepository.save(java.util.Objects.requireNonNull(mapping)));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<ProfileBpmnAssignmentEntity>> getAllAssignments() {
        return ResponseEntity.ok(assignmentRepository.findAll());
    }

    @PostMapping("/assignments")
    public ResponseEntity<ProfileBpmnAssignmentEntity> addAssignment(
            @RequestBody ProfileBpmnAssignmentEntity assignment) {
        return ResponseEntity.ok(assignmentRepository.save(java.util.Objects.requireNonNull(assignment)));
    }
}
