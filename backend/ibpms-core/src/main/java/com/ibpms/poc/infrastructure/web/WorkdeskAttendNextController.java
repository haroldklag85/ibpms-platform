package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.SkipReasonDTO;
import com.ibpms.poc.application.dto.WorkdeskGlobalItemDTO;
import com.ibpms.poc.application.port.in.AttendNextTaskUseCase;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workdesk")
@Traceability(US = "US-001", CA = {"CA-28", "CA-21", "CA-16"})
public class WorkdeskAttendNextController {

    private final AttendNextTaskUseCase attendNextTaskUseCase;

    public WorkdeskAttendNextController(AttendNextTaskUseCase attendNextTaskUseCase) {
        this.attendNextTaskUseCase = attendNextTaskUseCase;
    }

    @PostMapping("/attend-next")
    @PreAuthorize("isAuthenticated()")
    @Traceability(US = "US-001", CA = {"CA-28", "CA-16"})
    public ResponseEntity<WorkdeskGlobalItemDTO> attendNext(Authentication authentication) {
        String currentUserId = authentication.getName();
        WorkdeskGlobalItemDTO dto = attendNextTaskUseCase.attendNext(currentUserId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/attend-next/skip")
    @PreAuthorize("isAuthenticated()")
    @Traceability(US = "US-001", CA = {"CA-21"})
    public ResponseEntity<WorkdeskGlobalItemDTO> skipAndNext(
            @RequestBody @Valid SkipReasonDTO skipReason,
            Authentication authentication) {
        String currentUserId = authentication.getName();
        WorkdeskGlobalItemDTO dto = attendNextTaskUseCase.skipAndAttendNext(currentUserId, skipReason);
        return ResponseEntity.ok(dto);
    }
}
