package com.ibpms.poc.application.service.bpmn;

import com.ibpms.poc.application.dto.BpmnLaneDTO;
import com.ibpms.poc.application.dto.LaneInfo;
import com.ibpms.poc.application.dto.LaneRoleAssignmentDTO;
import com.ibpms.poc.application.dto.LaneRoleAssignmentRequest;
import com.ibpms.poc.application.port.out.BpmnLanePort;
import com.ibpms.poc.infrastructure.jpa.entity.bpmn.BpmnLaneEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.LaneRoleAssignmentEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnLaneJpaRepository;
import com.ibpms.poc.infrastructure.jpa.repository.LaneRoleAssignmentJpaRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BpmnLaneService implements BpmnLanePort {

    private final BpmnLaneJpaRepository bpmnLaneRepository;
    private final LaneRoleAssignmentJpaRepository laneRoleAssignmentRepository;
    private final RoleRepository roleRepository;
    private final EntityManager entityManager;

    public BpmnLaneService(BpmnLaneJpaRepository bpmnLaneRepository, 
                           LaneRoleAssignmentJpaRepository laneRoleAssignmentRepository,
                           RoleRepository roleRepository,
                           EntityManager entityManager) {
        this.bpmnLaneRepository = bpmnLaneRepository;
        this.laneRoleAssignmentRepository = laneRoleAssignmentRepository;
        this.roleRepository = roleRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<BpmnLaneDTO> getLanesByProcessKey(String processDefinitionKey) {
        return bpmnLaneRepository.findByProcessDesign_TechnicalId(processDefinitionKey).stream()
                .map(lane -> new BpmnLaneDTO(
                        lane.getId(),
                        processDefinitionKey,
                        lane.getLaneXmlId(),
                        lane.getLaneName(),
                        lane.getActorDescription(),
                        lane.getLinkedRole() != null ? lane.getLinkedRole().getName() : null
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void syncLanesFromDeployment(String processKey, UUID processDesignId, List<LaneInfo> lanes) {
        if (processDesignId == null) {
            log.warn("syncLanesFromDeployment: processDesignId is null for processKey={}. Skipping lane sync to avoid ConstraintViolationException.", processKey);
            return;
        }
        
        List<BpmnLaneEntity> existingLanes = bpmnLaneRepository.findByProcessDesign_TechnicalId(processKey);
        Set<String> xmlLaneIds = lanes.stream().map(LaneInfo::laneXmlId).collect(Collectors.toSet());

        for (LaneInfo laneInfo : lanes) {
            BpmnLaneEntity laneEntity = existingLanes.stream()
                .filter(l -> l.getLaneXmlId().equals(laneInfo.laneXmlId()))
                .findFirst()
                .orElseGet(() -> {
                    BpmnLaneEntity newLane = new BpmnLaneEntity();
                    // Ideally we fetch the processDesignEntity using processDesignId here
                    // However, we can use EntityManager.getReference or let the caller provide it.
                    // For now, we will create a proxy entity if processDesignId is provided.
                    if(processDesignId != null) {
                        com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity pde = new com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity();
                        pde.setId(processDesignId);
                        newLane.setProcessDesign(pde);
                    }
                    newLane.setLaneXmlId(laneInfo.laneXmlId());
                    return newLane;
                });
            
            laneEntity.setLaneName(laneInfo.laneName());
            bpmnLaneRepository.save(laneEntity);
        }

        // Purgar lanes zombies
        existingLanes.stream()
            .filter(l -> !xmlLaneIds.contains(l.getLaneXmlId()))
            .forEach(bpmnLaneRepository::delete);
    }

    @Override
    public List<LaneRoleAssignmentDTO> getAssignmentsByRoleId(UUID roleId) {
        return laneRoleAssignmentRepository.findByRole_Id(roleId).stream()
                .map(assignment -> new LaneRoleAssignmentDTO(
                        assignment.getLane().getId(),
                        assignment.getLane().getLaneName(),
                        assignment.getLane().getProcessDesign().getTechnicalId(),
                        assignment.getCanInitiate(),
                        assignment.getCanExecute()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void replaceAssignmentsForRole(UUID roleId, List<LaneRoleAssignmentRequest> assignments) {
        if (!roleRepository.existsById(roleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleId);
        }
        
        for (LaneRoleAssignmentRequest req : assignments) {
            if (!bpmnLaneRepository.existsById(req.laneId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lane not found: " + req.laneId());
            }
        }

        laneRoleAssignmentRepository.deleteByRole_Id(roleId);
        
        RoleEntity role = entityManager.getReference(RoleEntity.class, roleId);
        
        List<LaneRoleAssignmentEntity> newAssignments = assignments.stream().map(req -> {
            LaneRoleAssignmentEntity entity = new LaneRoleAssignmentEntity();
            BpmnLaneEntity lane = entityManager.getReference(BpmnLaneEntity.class, req.laneId());
            entity.setLane(lane);
            entity.setRole(role);
            entity.setCanInitiate(req.canInitiate());
            entity.setCanExecute(req.canExecute());
            
            String currentUser = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "system";
            entity.setAssignedBy(currentUser);
            
            return entity;
        }).collect(Collectors.toList());
        
        laneRoleAssignmentRepository.saveAll(newAssignments);
    }
}
