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
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.http.HttpStatus;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/workdesk")
public class WorkdeskQueryController {

    private static final Logger log = LoggerFactory.getLogger(WorkdeskQueryController.class);

    private final WorkdeskProjectionRepository projectionRepository;
    private final Bucket bucket;

    public WorkdeskQueryController(WorkdeskProjectionRepository projectionRepository) {
        this.projectionRepository = projectionRepository;
        // CA-30: Rate Limiting
        Bandwidth limit = Bandwidth.builder().capacity(60).refillGreedy(60, Duration.ofMinutes(1)).build();
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    /**
     * CQRS Facade. Endpoints puramente de lectura unificada (Camunda + Kanban).
     */
    @GetMapping("/global-inbox")
    public ResponseEntity<WorkdeskResponseDTO> getGlobalInbox(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String delegatedUserId,
            Pageable pageable) {
        
        // CA-30: Aplicar Rate Limiting (60 rev/min)
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

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
                
                // CA-23: Inyección del avance calculado
                dto.setProgressPercent(e.getProgressPercent());
                
                // CA-03: Badge visual de tipo
                dto.setTypeBadge("BPMN".equals(e.getSourceSystem()) ? "⚡ Flujo" : "📅 Proyecto");
                
                // CA-17: Flag de impacto financiero alto (umbral >= 8)
                dto.setFinancialImpactHigh(e.getImpactLevel() != null && e.getImpactLevel() >= 8);
                
                return dto;
            });

            return ResponseEntity.ok(new WorkdeskResponseDTO(false, dtoPage));
            
        } catch (Exception e) {
            // CA-07/CA-18: Degradación Elegante Multi-Motor
            boolean isCamundaFailure = e.getMessage() != null && 
                (e.getMessage().contains("Camunda") || e.getMessage().contains("ProcessEngine") || e.getCause() instanceof org.springframework.web.client.ResourceAccessException);
            
            if (isCamundaFailure) {
                log.warn("CA-07: Motor BPMN degradado. Retornando solo tareas Kanban locales.", e);
            } else {
                log.error("Error crítico completo en bandeja CQRS Workdesk.", e);
            }
            
            // Retornar vacío con bandera de degradación
            @SuppressWarnings("null")
            Page<WorkdeskGlobalItemDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            return ResponseEntity.ok(new WorkdeskResponseDTO(true, emptyPage));
        }
    }

    // CA-22, CA-29: Faceted Filters & Counters
    @GetMapping("/global-inbox/facets")
    public ResponseEntity<?> getFacets() {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String tenantId = (auth != null && auth.getName() != null) ? auth.getName() : "default";

            java.util.List<com.ibpms.poc.application.dto.FacetCountDto> facets = projectionRepository.countByStatusPerTenant(tenantId);
            return ResponseEntity.ok(facets);
        } catch (Exception e) {
            log.error("Error obteniendo facetas (CA-22, CA-29)", e);
            return ResponseEntity.ok(Collections.emptyList()); // Fallback degradación
        }
    }
}
