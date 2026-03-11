package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.WorkdeskGlobalItemDTO;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workdesk")
public class WorkdeskQueryController {

    private final WorkdeskProjectionRepository projectionRepository;

    public WorkdeskQueryController(WorkdeskProjectionRepository projectionRepository) {
        this.projectionRepository = projectionRepository;
    }

    /**
     * CQRS Facade. Endpoints puramente de lectura unificada (Camunda + Kanban).
     */
    @GetMapping("/global-inbox")
    public ResponseEntity<Page<WorkdeskGlobalItemDTO>> getGlobalInbox(Pageable pageable) {
        // En un entorno de microservicios o alta carga, esta tabla `ibpms_workdesk_projection` 
        // sería consultada incluso desde un ElasticSearch o Redis.
        
        Page<WorkdeskProjectionEntity> entities = projectionRepository.findAll(pageable);
        
        Page<WorkdeskGlobalItemDTO> dtoPage = entities.map(e -> {
            WorkdeskGlobalItemDTO dto = new WorkdeskGlobalItemDTO();
            dto.setUnifiedId(e.getId());
            dto.setSourceSystem(e.getSourceSystem());
            dto.setOriginalTaskId(e.getOriginalTaskId());
            dto.setTitle(e.getTitle());
            dto.setSlaExpirationDate(e.getSlaExpirationDate());
            dto.setStatus(e.getStatus());
            dto.setAssignee(e.getAssignee());
            return dto;
        });

        return ResponseEntity.ok(dtoPage);
    }
}
