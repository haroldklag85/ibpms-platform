package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.port.in.BpmnTutorUseCase;
import com.ibpms.poc.infrastructure.web.dto.BpmnAnalysisResultDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cognitive/bpmn")
public class AiBpmnTutorController {

    private final BpmnTutorUseCase tutorUseCase;

    public AiBpmnTutorController(BpmnTutorUseCase tutorUseCase) {
        this.tutorUseCase = tutorUseCase;
    }

    @PostMapping("/analyze")
    public ResponseEntity<BpmnAnalysisResultDTO> analyzeBpmn(
            @RequestBody String bpmnXml,
            @RequestParam(defaultValue = "false") boolean strictMode) {
        BpmnAnalysisResultDTO result = tutorUseCase.evaluateProcess(bpmnXml, strictMode);
        return ResponseEntity.ok(result);
    }
}
