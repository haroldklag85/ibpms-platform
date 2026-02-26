package com.ibpms.poc.infrastructure.camunda;

import com.ibpms.poc.application.port.in.GenerarPdfOficialUseCase;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Camunda Java Delegate
 * Invocado automáticamente por el BPMN (Service Task) al llegar a un cierre o
 * aprobación.
 * (ServiceTask -> Expression o Class -> AprobarCasoPdfDelegate)
 */
@Component("aprobarCasoPdfDelegate")
public class AprobarCasoPdfDelegate implements JavaDelegate {

    private final GenerarPdfOficialUseCase generarPdfOficialUseCase;

    public AprobarCasoPdfDelegate(GenerarPdfOficialUseCase generarPdfOficialUseCase) {
        this.generarPdfOficialUseCase = generarPdfOficialUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Enlazar Variables de Ejecución del motor BPM Camunda con Java Hexagonal
        String processInstanceId = execution.getProcessInstanceId();

        // Supongamos que guardamos el case_id inicial como variable en Camunda en el
        // CreateExpediente
        String caseId = (String) execution.getVariable("ibpms_case_id");
        if (caseId == null) {
            caseId = processInstanceId; // Fallback
        }

        // Recuperar todo el payload/variables del proceso
        Map<String, Object> variables = execution.getVariables();

        String author = (String) execution.getVariable("aprobador");
        if (author == null) {
            author = "System / BPMN Engine";
        }

        // Detonar generación inmutable y carga a Azure SGDEA
        generarPdfOficialUseCase.generarPdfCierre(caseId, variables, author);
    }
}
