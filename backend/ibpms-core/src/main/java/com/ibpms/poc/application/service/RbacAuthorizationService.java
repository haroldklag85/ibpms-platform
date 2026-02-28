package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.IdpGroupMappingEntity;
import com.ibpms.poc.infrastructure.jpa.entity.ProfileBpmnAssignmentEntity;
import com.ibpms.poc.infrastructure.jpa.repository.IdpGroupMappingRepository;
import com.ibpms.poc.infrastructure.jpa.repository.ProfileBpmnAssignmentRepository;
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
 */
@Service
public class RbacAuthorizationService {

    private final IdpGroupMappingRepository groupMappingRepository;
    private final ProfileBpmnAssignmentRepository assignmentRepository;

    public RbacAuthorizationService(IdpGroupMappingRepository groupMappingRepository,
            ProfileBpmnAssignmentRepository assignmentRepository) {
        this.groupMappingRepository = groupMappingRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Devuelve una lista de Lanes BPMN (ej. "Gestor_Finanzas", "Aprobador_Legal")
     * dado un listado de IDs de grupos (ej. ObjectIDs de EntraID provenientes del
     * token).
     */
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
}
