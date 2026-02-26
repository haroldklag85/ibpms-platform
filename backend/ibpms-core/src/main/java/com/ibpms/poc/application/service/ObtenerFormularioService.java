package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.FormActionDTO;
import com.ibpms.poc.application.dto.FormComponentDTO;
import com.ibpms.poc.application.dto.FormSchemaDTO;
import com.ibpms.poc.application.port.in.ObtenerFormularioUseCase;
import org.camunda.bpm.engine.TaskService;
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

    public ObtenerFormularioService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    @Transactional(readOnly = true)
    public FormSchemaDTO obtenerFormulario(String taskId) {
        // Validación de existencia en el motor real
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new jakarta.persistence.EntityNotFoundException("Tarea no encontrada: " + taskId);
        }

        // Mock exacto según la arquitectura ui_components_schema.md
        FormSchemaDTO schema = new FormSchemaDTO();
        schema.setFormId(task.getFormKey() != null ? task.getFormKey() : "frm_dinamico");
        schema.setTitle("Atención de Tarea: " + task.getName());
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

        schema.setComponents(List.of(inputRut, inputCountry, textAreaAi));

        FormActionDTO btnSubmit = new FormActionDTO();
        btnSubmit.setId("btn_submit");
        btnSubmit.setType("submit");
        btnSubmit.setLabel("Aprobar y Continuar");
        btnSubmit.setTheme("primary");

        schema.setActions(List.of(btnSubmit));

        return schema;
    }
}
