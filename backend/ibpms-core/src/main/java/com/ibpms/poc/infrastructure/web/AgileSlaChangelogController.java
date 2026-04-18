package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.SlaChangeLogService;
import com.ibpms.poc.domain.model.agile.AgileSlaChangelog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agile/tasks/{taskId}/sla-log")
public class AgileSlaChangelogController {

    private final SlaChangeLogService changeLogService;

    public AgileSlaChangelogController(SlaChangeLogService changeLogService) {
        this.changeLogService = changeLogService;
    }

    // CA-9: Bitácora SLA
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Page<AgileSlaChangelog>> getSlaLogs(
        @PathVariable UUID taskId,
        Pageable pageable) {
        return ResponseEntity.ok(changeLogService.getLogsByTask(taskId, pageable));
    }
}
