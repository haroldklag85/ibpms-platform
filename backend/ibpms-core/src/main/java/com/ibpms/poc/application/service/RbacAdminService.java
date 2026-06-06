package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.IbpmsProfileEntity;
import com.ibpms.poc.infrastructure.jpa.entity.IdpGroupMappingEntity;
import com.ibpms.poc.infrastructure.jpa.entity.ProfileBpmnAssignmentEntity;
import com.ibpms.poc.infrastructure.jpa.repository.IbpmsProfileRepository;
import com.ibpms.poc.infrastructure.jpa.repository.IdpGroupMappingRepository;
import com.ibpms.poc.infrastructure.jpa.repository.ProfileBpmnAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.List;

/**
 * Servicio Administrativo de RBAC (Role-Based Access Control).
 * Se encarga de aislar la persistencia de los Controladores Administrativos,
 * permitiendo la gestión de Perfiles, Mapeos IDP y Asignaciones BPMN.
 * 
 * @Traceability(US = "US-005", CA = {"CA-02"})
 */
@Service
@Transactional
@Traceability(US = "US-005", CA = {"CA-02"})
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

    /**
     * @return Lista de todos los perfiles iBPMS.
     */
    // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
    public List<IbpmsProfileEntity> getAllProfiles() {
        return profileRepository.findAll();
    }

    /**
     * @param profile Perfil a guardar.
     * @return Perfil guardado.
     */
    // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
    public IbpmsProfileEntity saveProfile(IbpmsProfileEntity profile) {
        return profileRepository.save(profile);
    }

    /**
     * @return Mapeos entre grupos IDP y roles/perfiles.
     */
    // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
    public List<IdpGroupMappingEntity> getAllMappings() {
        return mappingRepository.findAll();
    }

    /**
     * @param mapping Mapeo a guardar.
     * @return Mapeo guardado.
     */
    // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
    public IdpGroupMappingEntity saveMapping(IdpGroupMappingEntity mapping) {
        return mappingRepository.save(mapping);
    }

    /**
     * @return Asignaciones de perfiles a definiciones BPMN.
     */
    // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
    public List<ProfileBpmnAssignmentEntity> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    /**
     * @param assignment Asignación BPMN a guardar.
     * @return Asignación guardada.
     */
    // @Traceability: US-005 - CA-02 (ADR-001 Refactor)
    public ProfileBpmnAssignmentEntity saveAssignment(ProfileBpmnAssignmentEntity assignment) {
        return assignmentRepository.save(assignment);
    }
}
