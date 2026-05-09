package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.WorkdeskGlobalItemDTO;
import com.ibpms.poc.application.dto.WorkdeskResponseDTO;
import com.ibpms.poc.application.dto.DelegationContextDTO;
import com.ibpms.poc.application.service.TaskDelegationService;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.application.service.WorkdeskQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.server.ResponseStatusException;
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

    private final WorkdeskQueryService workdeskQueryService;
    private final TaskDelegationService taskDelegationService;
    // @Traceability(US = "US-001", CA = {"CA-30"})
    // REMEDIACIÓN CA-30: Rate Limiter migrado de Singleton global a per-user para evitar DoS falso entre usuarios concurrentes.
    private final java.util.concurrent.ConcurrentHashMap<String, Bucket> userBuckets = new java.util.concurrent.ConcurrentHashMap<>();

    public WorkdeskQueryController(WorkdeskQueryService workdeskQueryService, TaskDelegationService taskDelegationService) {
        this.workdeskQueryService = workdeskQueryService;
        this.taskDelegationService = taskDelegationService;
    }

    private Bucket resolveBucket(String userId) {
        return userBuckets.computeIfAbsent(userId, k -> {
            Bandwidth limit = Bandwidth.builder().capacity(60).refillGreedy(60, Duration.ofMinutes(1)).build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * CQRS Facade. Endpoints puramente de lectura unificada (Camunda + Kanban).
     */
    // @Traceability(US = "US-001", CA = {"CA-20"})
    // TODO: Brecha CA-20. La URI debe ser /api/v1/workdesk/tasks. Falta documentación OpenAPI.
    // Faltan query params: origin, status explícitos.
    // El DTO de respuesta no cumple el wrapper { data: [], pagination: {} } canónico.
    @GetMapping("/global-inbox")
    public ResponseEntity<WorkdeskResponseDTO> getGlobalInbox(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String delegatedUserId,
            // @Traceability(US = "US-001", CA = {"CA-09"}) 
            // REMEDIACIÓN CA-09: Se añadió @PageableDefault(size = 15) para alinear con el contrato canónico del Workdesk.
            @PageableDefault(size = 15) Pageable pageable) {
        
        // @Traceability(US = "US-001", CA = {"CA-30"})
        // CA-30: Rate Limiting per-user (60 rq/min por usuario autenticado)
        org.springframework.security.core.Authentication authForRateLimit = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String rateLimitKey = (authForRateLimit != null && authForRateLimit.getName() != null) ? authForRateLimit.getName() : "anonymous";
        if (!resolveBucket(rateLimitKey).tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(null); // CA-30: El frontend muestra el toast 'Has realizado demasiadas consultas'
        }

        // @Traceability(US = "US-001", CA = {"CA-09", "CA-10"})
        // REMEDIACIÓN CA-10: Se reemplazó IllegalArgumentException por ResponseStatusException(400) para emitir HTTP 400 semántico.
        if (pageable.getPageSize() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página solicitada excede el límite máximo de 100 registros (CA-10).");
        }

        try {
            // @Traceability: US-001 - CA-14
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = (auth != null && auth.getName() != null) ? auth.getName() : "default";
            String tenantId = currentUserId; // Mapeo simple de POC

            DelegationContextDTO delegationContext = null;
            String effectiveAssignee = currentUserId; 

            if (delegatedUserId != null && !delegatedUserId.isBlank()) {
                // @Traceability: US-001 - CA-15
                String assistantDisplayName = taskDelegationService
                    .validateDelegationHierarchy(currentUserId, delegatedUserId, tenantId);

                effectiveAssignee = delegatedUserId; 
                delegationContext = new DelegationContextDTO(delegatedUserId, assistantDisplayName, true);
            }

            // Remove sort from pageable to prevent Spring Data natively appending the entity property as a raw SQL column
            Pageable safePageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            Page<WorkdeskProjectionEntity> entities = workdeskQueryService.getWorkdeskTasks(tenantId, search, effectiveAssignee, safePageable);
            
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
                
                // @Traceability: US-001 - CA-23
                dto.setProgressPercent(e.getProgressPercent());
                
                // @Traceability: US-001 - CA-3
                dto.setTypeBadge("BPMN".equals(e.getSourceSystem()) ? "⚡ Flujo" : "📅 Proyecto");
                
                // @Traceability: US-001 - CA-17
                dto.setFinancialImpactHigh(e.getImpactLevel() != null && e.getImpactLevel() >= 8);
                
                return dto;
            });

            WorkdeskResponseDTO response = new WorkdeskResponseDTO(false, dtoPage);
            if (delegationContext != null) {
                response.setDelegationContext(delegationContext);
            }
            return ResponseEntity.ok(response);
            
        } catch (org.springframework.web.server.ResponseStatusException rse) {
            // Rethrow 403 para que llegue al cliente
            throw rse;
        } catch (Exception e) {
            // @Traceability: US-001 - CA-7, CA-18
            boolean isCamundaFailure = e.getMessage() != null && 
                (e.getMessage().contains("Camunda") || e.getMessage().contains("ProcessEngine") || e.getCause() instanceof org.springframework.web.client.ResourceAccessException);
            
            if (isCamundaFailure) {
                log.warn("CA-07: Motor BPMN degradado. Retornando solo tareas Kanban locales.", e);
            } else {
                log.error("Error crítico completo en bandeja CQRS Workdesk.", e);
            }
            
            // @Traceability(US = "US-001", CA = {"CA-07", "CA-18"})
            // TODO: Brecha CA-07 y CA-18 (Fail-Open Crítico). El requerimiento exige Degradación Elegante
            // retornando las tareas Kanban que sigan vivas en BD. Aquí se retorna una lista vacía 
            // destruyendo el Workdesk por completo si Camunda falla.
            @SuppressWarnings("null")
            Page<WorkdeskGlobalItemDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            return ResponseEntity.ok(new WorkdeskResponseDTO(true, emptyPage));
        }
    }

    // @Traceability: US-001 - CA-22, CA-29
    @GetMapping("/global-inbox/facets")
    public ResponseEntity<?> getFacets() {
        org.springframework.security.core.Authentication authFacet = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String facetRateLimitKey = (authFacet != null && authFacet.getName() != null) ? authFacet.getName() : "anonymous";
        if (!resolveBucket(facetRateLimitKey).tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String tenantId = (auth != null && auth.getName() != null) ? auth.getName() : "default";

            java.util.List<com.ibpms.poc.application.dto.FacetCountDto> facets = workdeskQueryService.getFacets(tenantId);
            return ResponseEntity.ok(facets);
        } catch (Exception e) {
            log.error("Error obteniendo facetas (CA-22, CA-29)", e);
            return ResponseEntity.ok(Collections.emptyList()); // Fallback degradación
        }
    }
}
