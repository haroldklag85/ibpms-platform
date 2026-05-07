package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileTaskService;
import com.ibpms.poc.domain.model.agile.AgileTask;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/agile/portfolio")
@Traceability(US = "US-030", CA = {"CA-07"})
public class AgilePortfolioController {

    private final AgileTaskService taskService;

    public AgilePortfolioController(AgileTaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<AgileTaskController.TaskResponse>> getPortfolio(Authentication authentication) {
        List<AgileTask> portfolioTasks = taskService.getPortfolio(authentication.getName());
        List<AgileTaskController.TaskResponse> response = portfolioTasks.stream()
                .map(AgileTaskController.TaskResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
