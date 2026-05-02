package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.model.EventType;
import com.ibpms.poc.domain.port.FormEventRepository;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.IdentityLink;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collection;

@Service
public class AutoClaimService {

    private final TaskService taskService;
    private final com.ibpms.poc.application.config.ClaimProperties claimProperties;
    private final FormEventRepository formEventRepository;

    public AutoClaimService(TaskService taskService, com.ibpms.poc.application.config.ClaimProperties claimProperties, FormEventRepository formEventRepository) {
        this.taskService = taskService;
        this.claimProperties = claimProperties;
        this.formEventRepository = formEventRepository;
    }

    /**
     * Valida y ejecuta el Auto-Claim si el usuario es elegible.
     * Si la tarea ya está asignada a otro, lanza excepcion.
     * Si no está asignada, verifica que sea candidato y hace claim.
     */
    @Transactional
    public void tryAutoClaim(String taskId, String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        if (task.getAssignee() != null) {
            if (!task.getAssignee().equals(userId)) {
                throw new IllegalStateException("FORBIDDEN: La tarea ya se encuentra asignada a otro usuario.");
            }
            return; // Ya la tiene asignada, OK.
        }

        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("HTTP 403 - FORBIDDEN: Usuario no autenticado.");
        }

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        boolean isAuthorized = authorities.stream().anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAuthorized && !links.isEmpty()) {
            boolean isCandidateGroup = links.stream()
                .filter(link -> link.getGroupId() != null)
                .anyMatch(link -> authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + link.getGroupId().toUpperCase())));
            
            boolean isCandidateUser = links.stream()
                .filter(link -> link.getUserId() != null)
                .anyMatch(link -> link.getUserId().equals(userId));

            if (!isCandidateGroup && !isCandidateUser) {
                throw new IllegalStateException("HTTP 403 - FORBIDDEN");
            }
        }

        taskService.claim(taskId, userId);

        FormEvent autoClaimEvent = FormEvent.builder()
                .taskId(taskId)
                .eventType(EventType.TASK_AUTO_CLAIMED)
                .payloadJson("{\"action\": \"auto-claim\", \"userId\": \"" + userId + "\"}")
                .userId(userId)
                .createdAt(java.time.ZonedDateTime.now())
                .build();
        formEventRepository.save(autoClaimEvent);
    }
}
