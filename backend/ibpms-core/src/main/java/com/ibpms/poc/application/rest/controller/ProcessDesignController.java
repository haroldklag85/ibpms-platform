package com.ibpms.poc.application.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.rest.dto.GenericFormConfigUpdateRequest;
import com.ibpms.poc.application.service.BpmnDesignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/design/processes")
@Tag(name = "Process Design Controller", description = "Endpoints for process design configuration (CA-7, CA-17)")
public class ProcessDesignController {

    private final BpmnDesignService bpmnDesignService;
    private final ObjectMapper objectMapper;
    private final RepositoryService repositoryService;

    public ProcessDesignController(BpmnDesignService bpmnDesignService,
                                    ObjectMapper objectMapper,
                                    RepositoryService repositoryService) {
        this.bpmnDesignService = bpmnDesignService;
        this.objectMapper = objectMapper;
        this.repositoryService = repositoryService;
    }

    @PutMapping("/{processKey}/generic-form-config")
    @Operation(summary = "Update Generic Form Config", description = "Configures the whitelist in ibpms_bpmn_process_design")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateGenericFormConfig(
            @PathVariable("processKey") String processKey,
            @Valid @RequestBody GenericFormConfigUpdateRequest request,
            Authentication authentication) {

        String userId = authentication != null ? authentication.getName() : "anonymous";
        String whitelistJson;
        try {
            whitelistJson = objectMapper.writeValueAsString(request.getWhitelist());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        }

        bpmnDesignService.updateGenericFormConfig(processKey, whitelistJson, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * CA-17: Diccionario de Variables BPMN.
     * Retorna nombres de Input/Output parameters declarados en los UserTasks
     * de la última versión desplegada del proceso.
     */
    @GetMapping("/{processKey}/variables")
    @Operation(summary = "Get BPMN Variable Dictionary", description = "CA-17: Returns Input/Output parameter names from deployed BPMN UserTasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getBpmnVariables(@PathVariable("processKey") String processKey) {
        try {
            org.camunda.bpm.engine.repository.ProcessDefinition pd = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionKey(processKey)
                    .latestVersion()
                    .singleResult();

            if (pd == null) {
                return ResponseEntity.ok(List.of());
            }

            BpmnModelInstance model = repositoryService.getBpmnModelInstance(pd.getId());
            Set<String> variableNames = new LinkedHashSet<>();

            Collection<UserTask> userTasks = model.getModelElementsByType(UserTask.class);
            for (UserTask task : userTasks) {
                Collection<CamundaInputOutput> ioMappings = task.getChildElementsByType(CamundaInputOutput.class);
                for (CamundaInputOutput io : ioMappings) {
                    for (CamundaInputParameter input : io.getCamundaInputParameters()) {
                        variableNames.add(input.getCamundaName());
                    }
                    for (CamundaOutputParameter output : io.getCamundaOutputParameters()) {
                        variableNames.add(output.getCamundaName());
                    }
                }
            }

            return ResponseEntity.ok(new ArrayList<>(variableNames));
        } catch (Exception e) {
            // Proceso no desplegado o error al parsear — retornar vacío
            return ResponseEntity.ok(List.of());
        }
    }
}
