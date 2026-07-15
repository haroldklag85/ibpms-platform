// @Traceability: US-005/US-036 - ADR-001, ADR-016
package com.ibpms.poc.application.port.out;

import java.util.List;
import java.util.UUID;
import com.ibpms.poc.application.dto.BpmnLaneDTO;
import com.ibpms.poc.application.dto.LaneInfo;
import com.ibpms.poc.application.dto.LaneRoleAssignmentDTO;

public interface BpmnLanePort {
    List<BpmnLaneDTO> getLanesByProcessKey(String processDefinitionKey);
    void syncLanesFromDeployment(String processKey, UUID processDesignId, List<LaneInfo> lanes);
    void assignRoleToLane(UUID laneId, UUID roleId, boolean canInitiate, boolean canExecute);
    void removeRoleFromLane(UUID laneId, UUID roleId);
    List<LaneRoleAssignmentDTO> getAssignmentsByRoleId(UUID roleId);
}
