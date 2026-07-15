package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.dto.BpmnLaneDTO;
import com.ibpms.poc.application.dto.LaneRoleAssignmentDTO;
import com.ibpms.poc.application.dto.LaneRoleAssignmentRequest;
import com.ibpms.poc.application.port.out.BpmnLanePort;
import com.ibpms.poc.application.service.bpmn.BpmnLaneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class LaneAdminController {

    private final BpmnLanePort bpmnLanePort;
    private final BpmnLaneService bpmnLaneService;

    public LaneAdminController(BpmnLanePort bpmnLanePort, BpmnLaneService bpmnLaneService) {
        this.bpmnLanePort = bpmnLanePort;
        this.bpmnLaneService = bpmnLaneService;
    }

    @GetMapping("/lanes")
    public ResponseEntity<List<BpmnLaneDTO>> getLanesByProcess(
            @RequestParam String processKey) {
        return ResponseEntity.ok(bpmnLanePort.getLanesByProcessKey(processKey));
    }

    @GetMapping("/roles/{roleId}/lane-assignments")
    public ResponseEntity<List<LaneRoleAssignmentDTO>> getLaneAssignments(
            @PathVariable UUID roleId) {
        return ResponseEntity.ok(bpmnLanePort.getAssignmentsByRoleId(roleId));
    }

    @PutMapping("/roles/{roleId}/lane-assignments")
    public ResponseEntity<Void> saveLaneAssignments(
            @PathVariable UUID roleId,
            @RequestBody List<LaneRoleAssignmentRequest> assignments) {
        bpmnLaneService.replaceAssignmentsForRole(roleId, assignments);
        return ResponseEntity.ok().build();
    }
}
