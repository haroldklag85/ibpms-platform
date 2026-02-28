package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.port.in.AppBuilderUseCase;
import com.ibpms.poc.domain.model.UiTemplate;
import com.ibpms.poc.infrastructure.web.dto.PublishTemplateRequestDTO;
import com.ibpms.poc.infrastructure.web.dto.UiTemplateDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final AppBuilderUseCase appBuilderUseCase;

    public TemplateController(AppBuilderUseCase appBuilderUseCase) {
        this.appBuilderUseCase = appBuilderUseCase;
    }

    @PostMapping
    public ResponseEntity<UiTemplateDTO> saveTemplate(@RequestBody PublishTemplateRequestDTO request) {
        UiTemplate template = appBuilderUseCase.saveTemplate(
                request.getName(),
                request.getType(),
                request.getRawCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(UiTemplateDTO.fromDomain(template));
    }

    @GetMapping("/{name}/latest")
    public ResponseEntity<UiTemplateDTO> getLatestTemplateMetadata(@PathVariable String name) {
        UiTemplate template = appBuilderUseCase.getLatestTemplateByName(name);
        return ResponseEntity.ok(UiTemplateDTO.fromDomain(template));
    }

    @GetMapping(value = "/{name}/latest/raw", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getLatestTemplateRawCode(@PathVariable String name) {
        UiTemplate template = appBuilderUseCase.getLatestTemplateByName(name);
        return ResponseEntity.ok(template.getRawCode());
    }

    @GetMapping("/{name}/history")
    public ResponseEntity<List<UiTemplateDTO>> getVersionHistory(@PathVariable String name) {
        List<UiTemplateDTO> history = appBuilderUseCase.getVersionHistory(name)
                .stream()
                .map(UiTemplateDTO::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }
}
