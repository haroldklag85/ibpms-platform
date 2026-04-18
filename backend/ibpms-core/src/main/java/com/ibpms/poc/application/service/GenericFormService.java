package com.ibpms.poc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.rest.dto.GenericFormContextResponse;
import com.ibpms.poc.application.rest.dto.GenericFormSubmitRequest;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;


@Service
public class GenericFormService {

    private final TaskService taskService;
    private final ObjectMapper objectMapper;
    private final BpmnProcessDesignRepository processDesignRepository;
    // Autowired dependencies simplified for execution
    
    private static final List<String> DEFAULT_WHITELIST = Arrays.asList("Case_ID", "Instance_Name", "Priority", "Created_At");

    public GenericFormService(TaskService taskService,
                              ObjectMapper objectMapper,
                              BpmnProcessDesignRepository processDesignRepository) {
        this.taskService = taskService;
        this.objectMapper = objectMapper;
        this.processDesignRepository = processDesignRepository;
    }

    @Transactional(readOnly = true)
    public GenericFormContextResponse getGenericFormContext(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }

        Map<String, Object> allVariables = taskService.getVariables(taskId);
        
        // Simulating retrieval of whitelist from BpmnProcessDesignRepository using processDefinitionKey
        List<String> whitelist = fetchWhitelist(task.getProcessDefinitionId());
        if (whitelist == null || whitelist.isEmpty()) {
            whitelist = DEFAULT_WHITELIST;
        }

        Map<String, Object> prefillData = new HashMap<>();
        for (Map.Entry<String, Object> entry : allVariables.entrySet()) {
            String key = entry.getKey();
            if (isBlacklisted(key)) continue;
            
            // Check whitelist (case insensitive or exact depending on use case. We do case insensitive)
            boolean inWhitelist = whitelist.stream().anyMatch(w -> w.equalsIgnoreCase(key));
            if (inWhitelist) {
                prefillData.put(key, entry.getValue());
            }
        }

        // Base JSON Schema
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "observations", Map.of("type", "string", "minLength", 10, "maxLength", 2000),
                "attachments", Map.of("type", "array", "maxItems", 5, "items", Map.of("type", "string", "format", "uuid")),
                "management_result", Map.of("type", "string")
        ));
        schema.put("required", Arrays.asList("observations", "management_result"));
        schema.put("additionalProperties", false);

        List<String> allowedResults = Arrays.asList("APPROVED", "REJECTED", "PENDING_INFO", "ESCALATED");

        return new GenericFormContextResponse(schema, prefillData, allowedResults);
    }

    @Transactional
    public void submitGenericForm(String taskId, GenericFormSubmitRequest request, String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }

        // Implicit Locking: User must be Assignee
        if (task.getAssignee() == null || !task.getAssignee().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Implicit locking violation: Only assignee can submit");
        }

        // CA-8 validation
        String panicAction = request.getPanicAction();
        if (panicAction != null && !panicAction.isEmpty()) {
            if (request.getPanicJustification() == null || request.getPanicJustification().length() < 20) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "panicJustification must be >= 20 characters when panicAction is applied");
            }
            
            // Audit Log Registration can be fired here for panicAction
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("generic_form_observations", request.getObservations());
        variables.put("generic_form_result", request.getManagementResult());
        try {
            variables.put("generic_form_attachments", objectMapper.writeValueAsString(request.getAttachmentUuids() != null ? request.getAttachmentUuids() : new ArrayList<>()));
        } catch (JsonProcessingException e) {
            variables.put("generic_form_attachments", "[]");
        }

        if (panicAction != null) {
            switch(panicAction) {
                case "APPROVED":
                    variables.put("generic_form_result", "APPROVED");
                    taskService.complete(taskId, variables);
                    break;
                case "RETURNED":
                    variables.put("generic_form_result", "RETURNED");
                    taskService.complete(taskId, variables);
                    break;
                case "CANCELLED":
                    taskService.handleBpmnError(taskId, "TASK_CANCELLED_BY_OPERATOR", request.getPanicJustification(), variables);
                    break;
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid panic action");
            }
        } else {
            taskService.complete(taskId, variables);
        }
    }

    private boolean isBlacklisted(String key) {
        String lower = key.toLowerCase();
        return lower.startsWith("_internal_") || lower.startsWith("camunda_") || lower.startsWith("zeebe_");
    }

    private List<String> fetchWhitelist(String processDefinitionId) {
        // Obtenemos la llave tecnica del proceso (quitando el version tag de processDefinitionId, por simplicidad en Camunda 7 el key es el technicalId)
        String processKey = processDefinitionId.split(":")[0];
        
        return processDesignRepository.findByTechnicalId(processKey)
            .map(BpmnProcessDesignEntity::getGenericFormWhitelist)
            .map(whitelistStr -> {
                if (whitelistStr != null && !whitelistStr.isBlank()) {
                    try {
                        return objectMapper.readValue(whitelistStr, new TypeReference<List<String>>() {});
                    } catch (Exception e) {
                        return DEFAULT_WHITELIST;
                    }
                }
                return DEFAULT_WHITELIST;
            }).orElse(DEFAULT_WHITELIST);
    }
}
