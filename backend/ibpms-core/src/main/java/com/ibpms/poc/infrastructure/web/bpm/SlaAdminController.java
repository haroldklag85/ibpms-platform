package com.ibpms.poc.infrastructure.web.bpm;

import com.ibpms.poc.application.service.bpm.SlaService;
import com.ibpms.poc.infrastructure.jpa.entity.bpm.HolidayEntity;
import com.ibpms.poc.infrastructure.jpa.repository.bpm.HolidayRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/sla")
public class SlaAdminController {

    private final SlaService slaService;
    private final HolidayRepository holidayRepository;

    public SlaAdminController(SlaService slaService, HolidayRepository holidayRepository) {
        this.slaService = slaService;
        this.holidayRepository = holidayRepository;
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
        return ResponseEntity.ok(holidayRepository.findAll());
    }

    @PostMapping("/holidays")
    public ResponseEntity<HolidayEntity> addHoliday(@RequestBody HolidayEntity holiday) {
        return ResponseEntity.ok(holidayRepository.save(holiday));
    }
}
