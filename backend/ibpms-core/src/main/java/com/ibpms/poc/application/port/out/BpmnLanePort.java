// @Traceability: US-005/US-036 - ADR-001, ADR-016
package com.ibpms.poc.application.port.out;

import java.util.List;
import java.util.UUID;
import com.ibpms.poc.application.dto.BpmnLaneDTO;
import com.ibpms.poc.application.dto.LaneInfo;
import com.ibpms.poc.application.dto.LaneRoleAssignmentDTO;
import com.ibpms.poc.application.dto.LaneRoleAssignmentRequest;

public interface BpmnLanePort {
    List<BpmnLaneDTO> getLanesByProcessKey(String processDefinitionKey);
    void syncLanesFromDeployment(String processKey, UUID processDesignId, List<LaneInfo> lanes);
    List<LaneRoleAssignmentDTO> getAssignmentsByRoleId(UUID roleId);
    void replaceAssignmentsForRole(UUID roleId, List<LaneRoleAssignmentRequest> assignments);
}
