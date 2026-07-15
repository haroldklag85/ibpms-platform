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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BpmnLaneService implements BpmnLanePort {

    private final BpmnLaneJpaRepository bpmnLaneRepository;
    private final LaneRoleAssignmentJpaRepository laneRoleAssignmentRepository;

    public BpmnLaneService(BpmnLaneJpaRepository bpmnLaneRepository, LaneRoleAssignmentJpaRepository laneRoleAssignmentRepository) {
        this.bpmnLaneRepository = bpmnLaneRepository;
        this.laneRoleAssignmentRepository = laneRoleAssignmentRepository;
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
    @Transactional
    public void assignRoleToLane(UUID laneId, UUID roleId, boolean canInitiate, boolean canExecute) {
        // Implementation for single assignment if needed
    }

    @Override
    @Transactional
    public void removeRoleFromLane(UUID laneId, UUID roleId) {
        // Implementation for single removal if needed
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

    @Transactional
    public void replaceAssignmentsForRole(UUID roleId, List<LaneRoleAssignmentRequest> assignments) {
        laneRoleAssignmentRepository.deleteByRole_Id(roleId);
        
        RoleEntity role = new RoleEntity();
        role.setId(roleId);
        
        List<LaneRoleAssignmentEntity> newAssignments = assignments.stream().map(req -> {
            LaneRoleAssignmentEntity entity = new LaneRoleAssignmentEntity();
            BpmnLaneEntity lane = new BpmnLaneEntity();
            lane.setId(req.laneId());
            entity.setLane(lane);
            entity.setRole(role);
            entity.setCanInitiate(req.canInitiate());
            entity.setCanExecute(req.canExecute());
            entity.setAssignedBy("system");
            return entity;
        }).collect(Collectors.toList());
        
        laneRoleAssignmentRepository.saveAll(newAssignments);
    }
}
