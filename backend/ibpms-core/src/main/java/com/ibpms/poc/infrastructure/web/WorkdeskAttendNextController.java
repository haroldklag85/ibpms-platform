package com.ibpms.poc.infrastructure.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.SkipReasonDTO;
import com.ibpms.poc.application.dto.WorkdeskGlobalItemDTO;
import com.ibpms.poc.application.dto.WsWorkdeskEventDTO;
import com.ibpms.poc.infrastructure.jpa.entity.FeatureToggleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.TaskSkipEntity;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FeatureToggleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.TaskSkipRepository;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/workdesk")
public class WorkdeskAttendNextController {

    private static final Logger log = LoggerFactory.getLogger(WorkdeskAttendNextController.class);

    private final WorkdeskProjectionRepository projectionRepository;
    private final FeatureToggleRepository featureToggleRepository;
    private final TaskSkipRepository taskSkipRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public WorkdeskAttendNextController(WorkdeskProjectionRepository projectionRepository,
                                        FeatureToggleRepository featureToggleRepository,
                                        TaskSkipRepository taskSkipRepository,
                                        UserRepository userRepository,
                                        SimpMessagingTemplate messagingTemplate,
                                        ObjectMapper objectMapper) {
        this.projectionRepository = projectionRepository;
        this.featureToggleRepository = featureToggleRepository;
        this.taskSkipRepository = taskSkipRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/attend-next")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<WorkdeskGlobalItemDTO> attendNext(Authentication authentication) {
        String currentUserId = authentication.getName();
        String tenantId = currentUserId; // Mapeo POC

        FeatureToggleEntity toggle = featureToggleRepository.findByTenantIdAndToggleKey(tenantId, "FORCE_ROUTING")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "FORCE_ROUTING toggle is OFF or missing"));
        if (!toggle.getEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORCE_ROUTING toggle is OFF");
        }

        UserEntity user = userRepository.findByUsername(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        String[] skills = null;
        if (user.getSkills() != null && !user.getSkills().isBlank()) {
            try {
                List<String> skillList = objectMapper.readValue(user.getSkills(), new TypeReference<List<String>>() {});
                if (!skillList.isEmpty()) {
                    skills = skillList.toArray(new String[0]);
                }
            } catch (JsonProcessingException e) {
                log.error("Error parsing skills JSON for user {}", currentUserId, e);
            }
        }

        Optional<WorkdeskProjectionEntity> taskOpt = projectionRepository.findNextAvailableTask(tenantId, skills);
        
        if (taskOpt.isEmpty()) {
            log.warn("CA-21: NO_SKILL_MATCH for user {}. Attempting Universal Fallback.", currentUserId);
            taskOpt = projectionRepository.findNextAvailableTask(tenantId, null);
            if (taskOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tasks available");
            }
        }

        WorkdeskProjectionEntity task = taskOpt.get();

        task.setAssignee(currentUserId);
        projectionRepository.save(task);

        WsWorkdeskEventDTO event = new WsWorkdeskEventDTO();
        event.setAction(WsWorkdeskEventDTO.Action.REMOVE);
        event.setTaskId(task.getId());
        event.setTenantId(tenantId);
        messagingTemplate.convertAndSend("/topic/workdesk/events", event);

        WsWorkdeskEventDTO personalEvent = new WsWorkdeskEventDTO();
        personalEvent.setAction(WsWorkdeskEventDTO.Action.ADD);
        personalEvent.setTaskId(task.getId());
        personalEvent.setTenantId(tenantId);
        messagingTemplate.convertAndSendToUser(currentUserId, "/topic/workdesk/events", personalEvent);

        return ResponseEntity.ok(mapToDTO(task));
    }

    @PostMapping("/attend-next/skip")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<WorkdeskGlobalItemDTO> skipAndNext(
            @RequestBody @Valid SkipReasonDTO skipReason,
            Authentication authentication) {
            
        String currentUserId = authentication.getName();
        String tenantId = currentUserId;
        
        if ("OTRO".equalsIgnoreCase(skipReason.skipReason()) && 
            (skipReason.skipReasonDetail() == null || skipReason.skipReasonDetail().length() < 10)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El detalle debe tener al menos 10 caracteres");
        }

        TaskSkipEntity skipEntity = new TaskSkipEntity();
        skipEntity.setTenantId(tenantId);
        skipEntity.setUserId(currentUserId);
        skipEntity.setTaskId(skipReason.taskId());
        skipEntity.setSkipReason(skipReason.skipReason());
        skipEntity.setSkipReasonDetail(skipReason.skipReasonDetail());
        taskSkipRepository.save(skipEntity);

        LocalDateTime since = LocalDateTime.now().minusHours(1);
        int recentSkips = taskSkipRepository.countRecentSkips(tenantId, currentUserId, since);
        if (recentSkips >= 3) {
            log.warn("CA-21: SUPERVISOR_ALERT: User {} has >= 3 recent consecutive skips.", currentUserId);
        }

        projectionRepository.findById(skipReason.taskId()).ifPresent(task -> {
            boolean wasAssignedToMe = currentUserId.equals(task.getAssignee());
            if(wasAssignedToMe) {
                task.setAssignee(null);
                projectionRepository.save(task);
                
                WsWorkdeskEventDTO event = new WsWorkdeskEventDTO();
                event.setAction(WsWorkdeskEventDTO.Action.ADD);
                event.setTaskId(task.getId());
                event.setTenantId(tenantId);
                messagingTemplate.convertAndSend("/topic/workdesk/events", event);
            }
        });

        return attendNext(authentication);
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
}
