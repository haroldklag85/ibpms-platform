package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.port.in.TrackTimeUseCase;
import com.ibpms.poc.application.port.out.TimeLogPort;
import com.ibpms.poc.domain.model.kanban.TimeLogEntry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ibpms.poc.crosscutting.annotations.Traceability;

@RestController
@RequestMapping("/api/v1/time-tracking")
@Traceability(US = "US-008", CA = {"CA-03", "CA-09", "CA-11"})
public class TimeTrackingController {

    private final TrackTimeUseCase trackTimeUseCase;
    private final TimeLogPort timeLogPort;

    public TimeTrackingController(TrackTimeUseCase trackTimeUseCase, TimeLogPort timeLogPort) {
        this.trackTimeUseCase = trackTimeUseCase;
        this.timeLogPort = timeLogPort;
    }

    @PostMapping("/start")
    public ResponseEntity<TimeLogEntry> startTimer(@RequestBody Map<String, String> body, Authentication authentication) {
        UUID referenceId = UUID.fromString(body.get("referenceId"));
        String referenceType = body.get("referenceType");
        String userId = authentication.getName();

        TimeLogEntry entry = trackTimeUseCase.startTimer(referenceId, referenceType, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @PostMapping("/stop/{logId}")
    public ResponseEntity<TimeLogEntry> stopTimer(@PathVariable UUID logId, Authentication authentication) {
        String userId = authentication.getName();
        TimeLogEntry entry = trackTimeUseCase.stopTimer(logId, userId);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TimeLogEntry>> getLogsByTask(@PathVariable UUID taskId) {
        List<TimeLogEntry> logs = timeLogPort.findByReferenceId(taskId);
        return ResponseEntity.ok(logs);
    }

    @RequestMapping(value = "/**", method = {RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Void> handlePutAndDelete() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}
