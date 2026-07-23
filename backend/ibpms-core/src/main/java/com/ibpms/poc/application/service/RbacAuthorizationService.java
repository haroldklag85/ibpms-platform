package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.IbpmsProfileEntity;

import com.ibpms.poc.infrastructure.jpa.entity.ProfileBpmnAssignmentEntity;
import com.ibpms.poc.infrastructure.jpa.repository.IbpmsProfileRepository;
import com.ibpms.poc.infrastructure.jpa.repository.IdpGroupMappingRepository;
import com.ibpms.poc.infrastructure.jpa.repository.ProfileBpmnAssignmentRepository;
import com.ibpms.poc.application.port.out.RbacPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio Autorizador RBAC:
 * Cruza los "claims/groups" de un Token JWT delegando al OIDC contra la base de
 * datos interna.
 * Retorna la lista de roles o carriles BPMN a los cuales el usuario tiene
 * acceso.
 * Adicionalmente implementa RbacPort para la auto-generación de Roles de
 * Proceso (US-029).
 */
@Service
public class RbacAuthorizationService implements RbacPort {

    private final IdpGroupMappingRepository groupMappingRepository;
    private final ProfileBpmnAssignmentRepository assignmentRepository;
    private final IbpmsProfileRepository profileRepository;

    public RbacAuthorizationService(IdpGroupMappingRepository groupMappingRepository,
            ProfileBpmnAssignmentRepository assignmentRepository,
            IbpmsProfileRepository profileRepository) {
        this.groupMappingRepository = groupMappingRepository;
        this.assignmentRepository = assignmentRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Devuelve una lista de Lanes BPMN (ej. "Gestor_Finanzas", "Aprobador_Legal")
     * dado un listado de IDs de grupos (ej. ObjectIDs de EntraID provenientes del
     * token).
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getPermittedBpmnLanesForGroups(List<String> idpGroupsList) {
        List<String> permittedLanes = new ArrayList<>();

        for (String groupId : idpGroupsList) {
            groupMappingRepository.findByIdpGroupId(groupId).ifPresent(mapping -> {
                // Obtener perfil lógico
                var profile = mapping.getProfile();

                // Buscar assignments de Lanes de este perfil en Camunda
                List<ProfileBpmnAssignmentEntity> assignments = assignmentRepository.findByProfile_Id(profile.getId());

                permittedLanes.addAll(assignments.stream()
                        .map(ProfileBpmnAssignmentEntity::getBpmnLaneId)
                        .collect(Collectors.toList()));
            });
        }

        // Retornar lista sin duplicados
        return permittedLanes.stream().distinct().collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void bindLaneToProfile(String processKey, String laneId, String profileName, String description) {
        // 1. Crear o recuperar el Perfil Global de Seguridad
        IbpmsProfileEntity profile = profileRepository.findByProfileName(profileName).orElseGet(() -> {
            IbpmsProfileEntity newProfile = new IbpmsProfileEntity();
            newProfile.setProfileName(profileName);
            newProfile.setDescription(description);
            return profileRepository.save(newProfile);
        });

        // 2. Revisar si la asignación de este Lane ya existe para este perfil
        // específico
        boolean assignmentExists = assignmentRepository.findByProfile_Id(profile.getId()).stream()
                .anyMatch(a -> a.getBpmnProcessKey().equals(processKey) && a.getBpmnLaneId().equals(laneId));

        if (!assignmentExists) {
            ProfileBpmnAssignmentEntity assignment = new ProfileBpmnAssignmentEntity();
            assignment.setProfile(profile);
            assignment.setBpmnProcessKey(processKey);
            assignment.setBpmnLaneId(laneId);
            assignmentRepository.save(assignment);
        }
    }

    // @Traceability: US-005, CA-06 Purga de Roles Zombies
    @Override
    @Transactional
    public void purgeZombieLanes(String processKey, List<String> activeLaneIds) {
        // 1. Obtener todas las asignaciones históricas de este proceso
        List<ProfileBpmnAssignmentEntity> existingAssignments = assignmentRepository.findByBpmnProcessKey(processKey);

        // 2. Filtrar las asignaciones que ya no existen en la lista de activeLaneIds (V2)
        for (ProfileBpmnAssignmentEntity assignment : existingAssignments) {
            if (!activeLaneIds.contains(assignment.getBpmnLaneId())) {
                // Eliminar asignación (el carril fue borrado)
                assignmentRepository.delete(assignment);

                // Opcional: Si el perfil queda sin ninguna otra asignación, eliminar el perfil también
                IbpmsProfileEntity profile = assignment.getProfile();
                List<ProfileBpmnAssignmentEntity> remainingAssignments = assignmentRepository.findByProfile_Id(profile.getId());
                // remainingAssignments incluye la actual en la memoria, así que validamos si su tamaño es 1
                if (remainingAssignments.size() <= 1) {
                    profileRepository.delete(profile);
                }
            }
        }
    }
}
