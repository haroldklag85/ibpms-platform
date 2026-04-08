package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.WorkdeskGlobalItemDTO;
import com.ibpms.poc.application.dto.WorkdeskResponseDTO;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/workdesk")
public class WorkdeskQueryController {

    private static final Logger log = LoggerFactory.getLogger(WorkdeskQueryController.class);

    private final WorkdeskProjectionRepository projectionRepository;

    public WorkdeskQueryController(WorkdeskProjectionRepository projectionRepository) {
        this.projectionRepository = projectionRepository;
    }

    /**
     * CQRS Facade. Endpoints puramente de lectura unificada (Camunda + Kanban).
     */
    @GetMapping("/global-inbox")
    public ResponseEntity<WorkdeskResponseDTO> getGlobalInbox(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String delegatedUserId,
            Pageable pageable) {
        
        // CA-09, CA-10: Max 100 limit, default to 15 (pageable usually defaults to 20, but limit to 100)
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Pagina solicitada excede el limite maximo de 100 registros (CA-10).");
        }

        try {
            // CA-14 Strict Isolation mapping
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String tenantId = (auth != null && auth.getName() != null) ? auth.getName() : "default";

            Page<WorkdeskProjectionEntity> entities = projectionRepository.findWorkdeskTasks(tenantId, search, delegatedUserId, pageable);
            
            Page<WorkdeskGlobalItemDTO> dtoPage = entities.map(e -> {
                WorkdeskGlobalItemDTO dto = new WorkdeskGlobalItemDTO();
                dto.setUnifiedId(e.getId());
                dto.setSourceSystem(e.getSourceSystem());
                dto.setOriginalTaskId(e.getOriginalTaskId());
                dto.setTitle(e.getTitle());
                dto.setSlaExpirationDate(e.getSlaExpirationDate());
                dto.setStatus(e.getStatus());
                dto.setAssignee(e.getAssignee());
                dto.setImpactLevel(e.getImpactLevel());
                return dto;
            });

            return ResponseEntity.ok(new WorkdeskResponseDTO(false, dtoPage));
            
        } catch (Exception e) {
            log.error("Error crítico obteniendo la bandeja CQRS Workdesk. Retornando estado degradado", e);
            // Tolerancia a fallos: NUNCA retornar 500, retornar lista vacia con bandera "degraded"
            @SuppressWarnings("null")
            Page<WorkdeskGlobalItemDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            return ResponseEntity.ok(new WorkdeskResponseDTO(true, emptyPage));
        }
    }
}
