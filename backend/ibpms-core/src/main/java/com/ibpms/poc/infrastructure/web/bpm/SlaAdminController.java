package com.ibpms.poc.infrastructure.web.bpm;

import com.ibpms.poc.application.service.bpm.SlaService;
import com.ibpms.poc.infrastructure.jpa.entity.bpm.HolidayEntity;
import com.ibpms.poc.infrastructure.jpa.entity.bpm.BusinessHoursEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/sla")
@SuppressWarnings("null")
public class SlaAdminController {

    private final SlaService slaService;

    public SlaAdminController(SlaService slaService) {
        this.slaService = slaService;
    }

    /**
     * CA-3: Disparador del recálculo (Anti-Deadlock Return)
     */
    @PostMapping("/apply")
    public ResponseEntity<String> applySlaMatrixRetroactively(@RequestParam(defaultValue = "false") boolean applyRetroactively) {
        if (applyRetroactively) {
            slaService.recalculateActiveSlas(); // Dispara asíncronamente
            return ResponseEntity.accepted().body("{\"status\": \"Recálculo en progreso (HTTP 202)\"}");
        }
        return ResponseEntity.ok("{\"status\": \"Matriz guardada para futuras instancias\"}");
    }

    /**
     * CA-5: Holiday API CRUD
     */
    @GetMapping("/holidays")
    public ResponseEntity<List<HolidayEntity>> getHolidays() {
        // @Traceability: Retro-Remediación ADR-001
        return ResponseEntity.ok(slaService.getHolidays());
    }

    @PostMapping("/holidays")
    public ResponseEntity<HolidayEntity> addHoliday(@RequestBody HolidayEntity holiday) {
        // @Traceability: Retro-Remediación ADR-001
        return ResponseEntity.ok(slaService.addHoliday(holiday));
    }

    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable java.util.UUID id) {
        // @Traceability: Retro-Remediación ADR-001
        slaService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * CA-4: Business Hours API CRUD
     */
    @GetMapping("/business-hours")
    public ResponseEntity<BusinessHoursEntity> getBusinessHours() {
        // @Traceability: Retro-Remediación ADR-001
        List<BusinessHoursEntity> configs = slaService.getBusinessHours();
        if (configs.isEmpty()) {
            return ResponseEntity.ok(new BusinessHoursEntity());
        }
        return ResponseEntity.ok(configs.get(0));
    }

    @PutMapping("/business-hours")
    public ResponseEntity<BusinessHoursEntity> updateBusinessHours(@RequestBody BusinessHoursEntity updatedConfig) {
        // @Traceability: Retro-Remediación ADR-001
        List<BusinessHoursEntity> configs = slaService.getBusinessHours();
        BusinessHoursEntity configToSave;
        if (configs.isEmpty()) {
            configToSave = updatedConfig;
        } else {
            configToSave = configs.get(0);
            configToSave.setStartTime(updatedConfig.getStartTime());
            configToSave.setEndTime(updatedConfig.getEndTime());
            configToSave.setWorkOnWeekends(updatedConfig.getWorkOnWeekends());
            if (updatedConfig.getTimezone() != null) {
                configToSave.setTimezone(updatedConfig.getTimezone());
            }
        }
        // @Traceability: Retro-Remediación ADR-001
        return ResponseEntity.ok(slaService.saveBusinessHours(configToSave));
    }
}
