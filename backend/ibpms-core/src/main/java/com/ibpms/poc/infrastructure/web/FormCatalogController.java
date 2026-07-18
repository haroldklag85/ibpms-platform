package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.application.service.FormDesignService;
import com.ibpms.poc.application.service.BpmnDesignService;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.application.dto.FormFieldMetadataDTO;
import com.ibpms.poc.application.dto.BpmnProcessDesignDTO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controller del Catálogo de Formularios.
 * Puente entre el Motor BPMN de Diseño y el Constructor de Formularios (CA-39).
 */
@RestController
@RequestMapping("/api/v1/forms")
@Traceability(US = "US-005", CA = {"CA-39", "CA-40"})
public class FormCatalogController {

    private final FormDesignService formDesignService;
    private final BpmnDesignService bpmnDesignService;

    // @Traceability: US-005, CA-39
    public FormCatalogController(FormDesignService formDesignService, BpmnDesignService bpmnDesignService) {
        this.formDesignService = formDesignService;
        this.bpmnDesignService = bpmnDesignService;
    }

    /**
     * CA-39: Directorio transversal de formularios activos.
     * Nutre el dropdown de "Form Key" en el BPMN Modeler.
     * (Mapeo desplazado a /active para evitar AmbiguousHandler con FormDirectoryController de la UI principal).
     */
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveForms(
            @RequestParam(value = "processKey", required = false) String processKey) {
        // @Traceability: US-005, CA-40
        String pattern = null;
        if (processKey != null && !processKey.trim().isEmpty()) {
            try {
                BpmnProcessDesignDTO process = bpmnDesignService.obtenerPorTechnicalId(processKey);
                pattern = process.getFormPattern();
            } catch (Exception e) {
                pattern = null;
            }
        }

        List<FormDesignDTO> activeForms = formDesignService.listarCatalogo();

        final String finalPattern = pattern;
        List<Map<String, Object>> response = activeForms.stream()
            .filter(form -> "ACTIVE".equalsIgnoreCase(form.getStatus()) || "DRAFT".equalsIgnoreCase(form.getStatus()))
            .map(form -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", form.getTechnicalName());
                map.put("name", form.getName());

                boolean isMaster = "IFORM_MAESTRO".equalsIgnoreCase(form.getPattern()) || "MASTER".equalsIgnoreCase(form.getPattern());
                map.put("type", isMaster ? "MASTER" : "SIMPLE");

                if (isMaster) {
                    long stagesCount = 0;
                    if (form.getFormFields() != null) {
                        stagesCount = form.getFormFields().stream()
                            .map(f -> (String) f.get("stage"))
                            .filter(Objects::nonNull)
                            .filter(s -> !s.trim().isEmpty())
                            .distinct()
                            .count();
                    }
                    map.put("stages", stagesCount > 0 ? (int) stagesCount : 1);
                }
                return map;
            })
            .filter(map -> {
                if (finalPattern == null) {
                    return true;
                }
                if ("SIMPLE".equalsIgnoreCase(finalPattern)) {
                    return "SIMPLE".equals(map.get("type"));
                }
                if ("IFORM_MAESTRO".equalsIgnoreCase(finalPattern)) {
                    return "MASTER".equals(map.get("type"));
                }
                return true;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
