package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.port.in.DelegateTaskUseCase;
import com.ibpms.poc.infrastructure.web.dto.DelegationRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workdesk/tasks")
public class KanbanTaskController {

    private final DelegateTaskUseCase delegateTaskUseCase;

    public KanbanTaskController(DelegateTaskUseCase delegateTaskUseCase) {
        this.delegateTaskUseCase = delegateTaskUseCase;
    }

    @PostMapping("/{taskId}/delegate")
    public ResponseEntity<String> delegateSubTask(@PathVariable String taskId, @RequestBody DelegationRequestDTO dto) {
        String newSubTaskId = delegateTaskUseCase.delegateSubTask(taskId, dto.getTitle(), dto.getAssignee());
        return ResponseEntity.status(HttpStatus.CREATED).body(newSubTaskId);
    }
}
