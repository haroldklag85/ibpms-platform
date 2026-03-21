package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.FormActionDTO;
import com.ibpms.poc.application.dto.FormComponentDTO;
import com.ibpms.poc.application.dto.FormSchemaDTO;
import com.ibpms.poc.application.port.in.ObtenerFormularioUseCase;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Aplicación — Server-Driven UI
 * En un escenario real leería el formId desde Camunda (task.getFormKey())
 * y buscaría el JSON exacto en BD o Storage.
 * Para V1 PoC, retornamos un mock basado en ui_components_schema.md.
 */
@Service
public class ObtenerFormularioService implements ObtenerFormularioUseCase {

    private final TaskService taskService;
    private final HistoryService historyService;

    public ObtenerFormularioService(TaskService taskService, HistoryService historyService) {
        this.taskService = taskService;
        this.historyService = historyService;
    }

    @Override
    @Transactional(readOnly = true)
    public FormSchemaDTO obtenerFormulario(String taskId) {
        boolean isHistory = false;
        String formKey = "frm_dinamico";
        String taskName = "Tarea Histórica";
        java.util.Map<String, Object> processVariables = new java.util.HashMap<>();

        java.util.Date taskStartTime = null;

        // Validación de existencia en el motor real
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            // CA-37: Fallback al Historial Inmutable
            HistoricTaskInstance hTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            if (hTask == null) {
                throw new jakarta.persistence.EntityNotFoundException("Tarea no encontrada ni activa ni en el historial (CA-37): " + taskId);
            }
            isHistory = true;
            taskStartTime = hTask.getStartTime();
            if (hTask.getName() != null) taskName = hTask.getName();
            
            // Cargar variables históricas
            List<HistoricVariableInstance> hVars = historyService.createHistoricVariableInstanceQuery().processInstanceId(hTask.getProcessInstanceId()).list();
            for (HistoricVariableInstance v : hVars) {
                processVariables.put(v.getName(), v.getValue());
            }
        } else {
            if (task.getFormKey() != null) formKey = task.getFormKey();
            if (task.getName() != null) taskName = task.getName();
            taskStartTime = task.getCreateTime();
            processVariables = taskService.getVariables(taskId);
        }

        // CA-78 (US-003): Versionado In-Flight Absoluto.
        // El motor recupera `taskStartTime`. Aquí se le exige a FormDesignRepository extraer
        // estrictamente el Esquema Zod V1/V2 que existía publicado en esa fecha cronológica,
        // aislando esta instancia de trabajo de futuros despliegues destructivos (Ej: V3).
        
        // Mock exacto según la arquitectura ui_components_schema.md
        FormSchemaDTO schema = new FormSchemaDTO();
        schema.setFormId(formKey);
        schema.setTitle("Atención de Tarea: " + taskName);
        schema.setVersion("1.0");
        schema.setLayout("vertical");

        FormComponentDTO inputRut = new FormComponentDTO();
        inputRut.setId("customer_id");
        inputRut.setType("text");
        inputRut.setLabel("Número de Identificación");
        inputRut.setPlaceholder("Ej: 10203040");
        inputRut.setRequired(true);

        FormComponentDTO inputCountry = new FormComponentDTO();
        inputCountry.setId("country_id");
        inputCountry.setType("catalog_select");
        inputCountry.setLabel("País de Residencia");
        inputCountry.setRequired(true);
        inputCountry.setCatalogSource("/api/v1/catalogs/countries"); // API Gateway / APIM route

        FormComponentDTO textAreaAi = new FormComponentDTO();
        textAreaAi.setId("ai_draft_suggested");
        textAreaAi.setType("textarea");
        textAreaAi.setLabel("Borrador M365 NLP (Revisar y Editar)");
        textAreaAi.setRows(6);
        textAreaAi.setDefaultValue("Por favor modifique este texto generado por el LLM antes de aprobar...");

        List<FormComponentDTO> componentsList = java.util.Arrays.asList(inputRut, inputCountry, textAreaAi);

        // CA-27: Data Binding -> Inject Camunda instance variables into form DefaultValues
        // CA-37: Soporte Visor Histórico Inmutable (Set Readonly)
        for (FormComponentDTO comp : componentsList) {
            if (processVariables.containsKey(comp.getId())) {
                comp.setDefaultValue(processVariables.get(comp.getId()));
            }
            if (isHistory) {
                comp.setReadonly(true);
            }
        }

        schema.setComponents(componentsList);

        if (isHistory) {
            // Un form histórico no debe permitir submits
            schema.setActions(java.util.Collections.emptyList());
        } else {
            FormActionDTO btnSubmit = new FormActionDTO();
            btnSubmit.setId("btn_submit");
            btnSubmit.setType("submit");
            btnSubmit.setLabel("Aprobar y Continuar");
            btnSubmit.setTheme("primary");
            schema.setActions(List.of(btnSubmit));
        }

        // CA-43: Inyectar variables nativas para Data Binding
        schema.setPrefillData(processVariables);

        return schema;
    }
}
