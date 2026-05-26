package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.application.service.FormDesignService;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.application.dto.FormFieldMetadataDTO;

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
@Traceability(US = "US-005", CA = {"CA-39"})
public class FormCatalogController {

    private final FormDesignService formDesignService;

    // @Traceability: US-005, CA-39
    public FormCatalogController(FormDesignService formDesignService) {
        this.formDesignService = formDesignService;
    }

    /**
     * CA-39: Directorio transversal de formularios activos.
     * Nutre el dropdown de "Form Key" en el BPMN Modeler.
     * (Mapeo desplazado a /active para evitar AmbiguousHandler con FormDirectoryController de la UI principal).
     */
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveForms() {
        List<FormDesignDTO> activeForms = formDesignService.listarCatalogo();

        List<Map<String, Object>> response = activeForms.stream()
            .filter(form -> "ACTIVE".equalsIgnoreCase(form.getStatus()))
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
                            .map(FormFieldMetadataDTO::getStage)
                            .filter(Objects::nonNull)
                            .filter(s -> !s.trim().isEmpty())
                            .distinct()
                            .count();
                    }
                    map.put("stages", stagesCount > 0 ? (int) stagesCount : 1);
                }
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
