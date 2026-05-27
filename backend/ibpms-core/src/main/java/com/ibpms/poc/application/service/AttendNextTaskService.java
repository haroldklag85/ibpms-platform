package com.ibpms.poc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.SkipReasonDTO;
import com.ibpms.poc.application.dto.WorkdeskGlobalItemDTO;
import com.ibpms.poc.application.dto.WsWorkdeskEventDTO;
import com.ibpms.poc.application.port.in.AttendNextTaskUseCase;
import com.ibpms.poc.application.port.out.TaskSkipPort;
import com.ibpms.poc.application.port.out.UserPort;
import com.ibpms.poc.application.port.out.WorkdeskProjectionPort;
import com.ibpms.poc.application.ports.in.UpdateFeatureToggleUseCase;
import com.ibpms.poc.application.port.out.AuditLogPort;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.infrastructure.jpa.entity.TaskSkipEntity;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Traceability(US = "US-001", CA = {"CA-28", "CA-21", "CA-16"})
public class AttendNextTaskService implements AttendNextTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(AttendNextTaskService.class);

    private final WorkdeskProjectionPort projectionPort;
    private final UpdateFeatureToggleUseCase featureToggleUseCase;
    private final TaskSkipPort taskSkipPort;
    private final UserPort userPort;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final AuditLogPort auditLogPort;

    public AttendNextTaskService(WorkdeskProjectionPort projectionPort,
                                 UpdateFeatureToggleUseCase featureToggleUseCase,
                                 TaskSkipPort taskSkipPort,
                                 UserPort userPort,
                                 SimpMessagingTemplate messagingTemplate,
                                 ObjectMapper objectMapper,
                                 AuditLogPort auditLogPort) {
        this.projectionPort = projectionPort;
        this.featureToggleUseCase = featureToggleUseCase;
        this.taskSkipPort = taskSkipPort;
        this.userPort = userPort;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.auditLogPort = auditLogPort;
    }

    @Override
    @Transactional
    @Traceability(US = "US-001", CA = {"CA-28", "CA-16"})
    public WorkdeskGlobalItemDTO attendNext(String userId) {
        // @Traceability: US-005, CA-28
        UserEntity user = userPort.findByUsername(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        final String tenantId = resolveTenantId(user, userId);

        // Validar toggle usando el UseCase
        if (!featureToggleUseCase.isFeatureEnabled(tenantId, "FORCE_ROUTING")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORCE_ROUTING toggle is OFF or missing");
        }

        String[] skills = null;
        if (user.getSkills() != null && !user.getSkills().isBlank()) {
            try {
                List<String> skillList = objectMapper.readValue(user.getSkills(), new TypeReference<List<String>>() {});
                if (!skillList.isEmpty()) {
                    skills = skillList.toArray(new String[0]);
                }
            } catch (JsonProcessingException e) {
                log.error("Error parsing skills JSON for user {}", userId, e);
            }
        }

        Optional<WorkdeskProjectionEntity> taskOpt = projectionPort.findNextAvailableTask(tenantId, skills);

        if (taskOpt.isEmpty()) {
            log.warn("CA-21: NO_SKILL_MATCH for user {}. Attempting Universal Fallback.", userId);
            
            // Brecha CA-21 audit log de negocio
            if (auditLogPort != null) {
                auditLogPort.saveAuditLog(
                        java.util.UUID.randomUUID().toString(),
                        "UNIVERSAL_FALLBACK",
                        tenantId,
                        "UNIVERSAL_FALLBACK_ATTEMPT",
                        "SYSTEM",
                        java.time.LocalDateTime.now(),
                        null,
                        false,
                        false,
                        "{\"userId\": \"" + userId + "\"}"
                );
            }

            taskOpt = projectionPort.findNextAvailableTask(tenantId, null);
            if (taskOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tasks available");
            }
        }

        WorkdeskProjectionEntity task = taskOpt.get();

        task.setAssignee(userId);
        projectionPort.save(task);

        WsWorkdeskEventDTO event = new WsWorkdeskEventDTO();
        event.setAction(WsWorkdeskEventDTO.Action.REMOVE);
        event.setTaskId(task.getId());
        event.setTenantId(tenantId);
        messagingTemplate.convertAndSend("/topic/workdesk/events", event);

        WsWorkdeskEventDTO personalEvent = new WsWorkdeskEventDTO();
        personalEvent.setAction(WsWorkdeskEventDTO.Action.ADD);
        personalEvent.setTaskId(task.getId());
        personalEvent.setTenantId(tenantId);
        messagingTemplate.convertAndSendToUser(userId, "/topic/workdesk/events", personalEvent);

        return mapToDTO(task);
    }

    @Override
    @Transactional
    @Traceability(US = "US-001", CA = {"CA-21"})
    public WorkdeskGlobalItemDTO skipAndAttendNext(String userId, SkipReasonDTO skipReason) {
        // @Traceability: US-005, CA-28
        UserEntity user = userPort.findByUsername(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        final String tenantId = resolveTenantId(user, userId);

        if ("OTRO".equalsIgnoreCase(skipReason.skipReason()) &&
            (skipReason.skipReasonDetail() == null || skipReason.skipReasonDetail().length() < 10)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El detalle debe tener al menos 10 caracteres");
        }

        TaskSkipEntity skipEntity = new TaskSkipEntity();
        skipEntity.setTenantId(tenantId);
        skipEntity.setUserId(userId);
        skipEntity.setTaskId(skipReason.taskId());
        skipEntity.setSkipReason(skipReason.skipReason());
        skipEntity.setSkipReasonDetail(skipReason.skipReasonDetail());
        taskSkipPort.save(skipEntity);

        LocalDateTime since = LocalDateTime.now().minusHours(1);
        int recentSkips = taskSkipPort.countRecentSkips(tenantId, userId, since);
        if (recentSkips >= 3) {
            log.warn("CA-21: SUPERVISOR_ALERT: User {} has >= 3 recent consecutive skips.", userId);
            
            if (auditLogPort != null) {
                auditLogPort.saveAuditLog(
                        java.util.UUID.randomUUID().toString(),
                        "SUPERVISOR_ALERT",
                        tenantId,
                        "SUPERVISOR_ALERT_SKIPS",
                        "SYSTEM",
                        java.time.LocalDateTime.now(),
                        null,
                        false,
                        false,
                        "{\"userId\": \"" + userId + "\", \"skips\": " + recentSkips + "}"
                );
            }
        }

        projectionPort.findById(skipReason.taskId()).ifPresent(task -> {
            boolean wasAssignedToMe = userId.equals(task.getAssignee());
            if (wasAssignedToMe) {
                task.setAssignee(null);
                projectionPort.save(task);

                WsWorkdeskEventDTO event = new WsWorkdeskEventDTO();
                event.setAction(WsWorkdeskEventDTO.Action.ADD);
                event.setTaskId(task.getId());
                event.setTenantId(tenantId);
                messagingTemplate.convertAndSend("/topic/workdesk/events", event);
            }
        });

        return attendNext(userId);
    }

    private WorkdeskGlobalItemDTO mapToDTO(WorkdeskProjectionEntity e) {
        WorkdeskGlobalItemDTO dto = new WorkdeskGlobalItemDTO();
        dto.setUnifiedId(e.getId());
        dto.setSourceSystem(e.getSourceSystem());
        dto.setOriginalTaskId(e.getOriginalTaskId());
        dto.setTitle(e.getTitle());
        dto.setSlaExpirationDate(e.getSlaExpirationDate());
        dto.setStatus(e.getStatus());
        dto.setAssignee(e.getAssignee());
        dto.setImpactLevel(e.getImpactLevel());
        dto.setProgressPercent(e.getProgressPercent());
        dto.setTypeBadge("BPMN".equals(e.getSourceSystem()) ? "⚡ Flujo" : "📅 Proyecto");
        dto.setFinancialImpactHigh(e.getImpactLevel() != null && e.getImpactLevel() >= 8);
        return dto;
    }

    private String resolveTenantId(UserEntity user, String userId) {
        try {
            return com.ibpms.poc.application.util.SecurityContextUtils.getTenantId();
        } catch (IllegalStateException e) {
            if (user.getEmail() != null && user.getEmail().contains("@")) {
                return "tenant_" + user.getEmail().split("@")[1].split("\\.")[0];
            } else {
                return userId;
            }
        }
    }
}
