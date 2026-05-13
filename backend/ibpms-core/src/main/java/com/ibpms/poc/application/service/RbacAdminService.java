package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.IbpmsProfileEntity;
import com.ibpms.poc.infrastructure.jpa.entity.IdpGroupMappingEntity;
import com.ibpms.poc.infrastructure.jpa.entity.ProfileBpmnAssignmentEntity;
import com.ibpms.poc.infrastructure.jpa.repository.IbpmsProfileRepository;
import com.ibpms.poc.infrastructure.jpa.repository.IdpGroupMappingRepository;
import com.ibpms.poc.infrastructure.jpa.repository.ProfileBpmnAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RbacAdminService {

    private final IbpmsProfileRepository profileRepository;
    private final IdpGroupMappingRepository mappingRepository;
    private final ProfileBpmnAssignmentRepository assignmentRepository;

    public RbacAdminService(IbpmsProfileRepository profileRepository,
                            IdpGroupMappingRepository mappingRepository,
                            ProfileBpmnAssignmentRepository assignmentRepository) {
        this.profileRepository = profileRepository;
        this.mappingRepository = mappingRepository;
        this.assignmentRepository = assignmentRepository;
    }

    // @Traceability: Retro-Remediación ADR-001
    public List<IbpmsProfileEntity> getAllProfiles() {
        return profileRepository.findAll();
    }

    // @Traceability: Retro-Remediación ADR-001
    public IbpmsProfileEntity saveProfile(IbpmsProfileEntity profile) {
        return profileRepository.save(profile);
    }

    // @Traceability: Retro-Remediación ADR-001
    public List<IdpGroupMappingEntity> getAllMappings() {
        return mappingRepository.findAll();
    }

    // @Traceability: Retro-Remediación ADR-001
    public IdpGroupMappingEntity saveMapping(IdpGroupMappingEntity mapping) {
        return mappingRepository.save(mapping);
    }

    // @Traceability: Retro-Remediación ADR-001
    public List<ProfileBpmnAssignmentEntity> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    // @Traceability: Retro-Remediación ADR-001
    public ProfileBpmnAssignmentEntity saveAssignment(ProfileBpmnAssignmentEntity assignment) {
        return assignmentRepository.save(assignment);
    }
}
