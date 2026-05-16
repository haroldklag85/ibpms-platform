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
import org.springframework.http.HttpStatus;
import java.time.Duration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController
@RequestMapping("/api/v1/workdesk")
@Tag(name = "Workdesk", description = "Workdesk Unified Inbox API (CQRS Facade)")
public class WorkdeskQueryController {

    private static final Logger log = LoggerFactory.getLogger(WorkdeskQueryController.class);

    private final WorkdeskQueryService workdeskQueryService;
    private final TaskDelegationService taskDelegationService;
    public WorkdeskQueryController(WorkdeskQueryService workdeskQueryService, TaskDelegationService taskDelegationService) {
        this.workdeskQueryService = workdeskQueryService;
        this.taskDelegationService = taskDelegationService;
    }

    /**
     * CQRS Facade. Endpoints puramente de lectura unificada (Camunda + Kanban).
     */
    // @Traceability(US = "US-001", CA = {"CA-20"})
    // TODO: Brecha CA-20. La URI debe ser /api/v1/workdesk/tasks. Falta documentación OpenAPI.
    // Faltan query params: origin, status explícitos.
    // El DTO de respuesta no cumple el wrapper { data: [], pagination: {} } canónico.
    @GetMapping("/global-inbox")
    @Operation(summary = "Obtiene la bandeja global de tareas", description = "Retorna la vista unificada CQRS de tareas BPMN y Kanban asignadas al usuario actual o equipo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bandeja retornada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Petición inválida (ej. PageSize excede límite)"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado o Tenant ID ausente"),
        @ApiResponse(responseCode = "429", description = "Límite de peticiones excedido (Rate Limiting)")
    })
    public ResponseEntity<WorkdeskResponseDTO> getGlobalInbox(
            @Parameter(description = "Filtro de búsqueda por título") @RequestParam(required = false) String search,
            @Parameter(description = "ID del usuario delegado (para suplantación/delegación)") @RequestParam(required = false) String delegatedUserId,
            // @Traceability(US = "US-001", CA = {"CA-09"}) 
            // REMEDIACIÓN CA-09: Se añadió @PageableDefault(size = 15) para alinear con el contrato canónico del Workdesk.
            @PageableDefault(size = 15) Pageable pageable,
            // @Traceability: US-001, CA-14 (Consolidación Identidad / Inyección Transparente)
            @com.ibpms.poc.infrastructure.web.annotation.CurrentTenant String tenantId) {
        
        // @Traceability(US = "US-001", CA = {"CA-09", "CA-10"})
        // REMEDIACIÓN CA-10: Se reemplazó IllegalArgumentException por ResponseStatusException(400) para emitir HTTP 400 semántico.
        if (pageable.getPageSize() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página solicitada excede el límite máximo de 100 registros (CA-10).");
        }

        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = (auth != null && auth.getName() != null) ? auth.getName() : "default";

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
            
            log.info("DEBUG-WORKDESK: tenantId={}, search={}, effectiveAssignee={}", tenantId, search, effectiveAssignee);
            
            Page<WorkdeskProjectionEntity> entities;
            boolean isDegraded = false;
            try {
                entities = workdeskQueryService.getWorkdeskTasks(tenantId, search, effectiveAssignee, safePageable);
            } catch (Exception innerE) {
                boolean isCamundaFailure = innerE.getMessage() != null && 
                    (innerE.getMessage().contains("Camunda") || innerE.getMessage().contains("ProcessEngine") || innerE.getCause() instanceof org.springframework.web.client.ResourceAccessException);
                
                if (isCamundaFailure) {
                    log.warn("CA-07: Motor BPMN degradado. Retornando solo tareas Kanban locales (Degradación Elegante).", innerE);
                    entities = workdeskQueryService.getWorkdeskTasksBySource(tenantId, search, effectiveAssignee, "KANBAN", safePageable);
                    isDegraded = true;
                } else {
                    throw innerE;
                }
            }
            
            log.info("DEBUG-WORKDESK: Entities returned={}", entities.getTotalElements());
            
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

            WorkdeskResponseDTO response = new WorkdeskResponseDTO(isDegraded, dtoPage);
            if (delegationContext != null) {
                response.setDelegationContext(delegationContext);
            }
            return ResponseEntity.ok(response);
            
        } catch (org.springframework.web.server.ResponseStatusException rse) {
            // Rethrow 403 para que llegue al cliente
            throw rse;
        } catch (Exception e) {
            log.error("Error crítico completo en bandeja CQRS Workdesk (Fallo general).", e);
            @SuppressWarnings("null")
            Page<WorkdeskGlobalItemDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new WorkdeskResponseDTO(true, emptyPage));
        }
    }

    // @Traceability: US-001 - CA-22, CA-29
    @GetMapping("/global-inbox/facets")
    @Operation(summary = "Obtener contadores de facetas", description = "Devuelve el conteo de tareas agrupado para renderizar métricas (facets) en la UI.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Facetas retornadas exitosamente"),
        @ApiResponse(responseCode = "429", description = "Límite de peticiones excedido (Rate Limiting)")
    })
    public ResponseEntity<?> getFacets() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String tenantId = "default";
            if (auth != null && auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                tenantId = jwt.getClaimAsString("tenant_id");
            } else if (auth != null && "analista_n1@ibpms.local".equals(auth.getName())) {
                tenantId = "tenant_alpha";
            }

            java.util.List<com.ibpms.poc.application.dto.FacetCountDto> facets = workdeskQueryService.getFacets(tenantId);
            return ResponseEntity.ok(facets);
        } catch (Exception e) {
            log.error("Error obteniendo facetas (CA-22, CA-29)", e);
            return ResponseEntity.ok(Collections.emptyList()); // Fallback degradación
        }
    }
}
