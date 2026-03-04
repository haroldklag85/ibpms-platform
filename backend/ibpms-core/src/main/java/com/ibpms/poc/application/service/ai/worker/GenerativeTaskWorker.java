package com.ibpms.poc.application.service.ai.worker;

import com.ibpms.poc.application.service.ai.CognitiveOrchestratorService;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente External Task de Camunda (CA-24).
 * En modo standalone (hilo separado) va por long-polling o se encarga de no
 * bloquear el thread transaccional de Camunda.
 */
@Component
@ExternalTaskSubscription(topicName = "ai-generative-task")
public class GenerativeTaskWorker implements ExternalTaskHandler {

    private final CognitiveOrchestratorService orchestrator;

    public GenerativeTaskWorker(CognitiveOrchestratorService orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

        // CA-6: Verificación de límite de tokens / presupuesto IA
        Integer tokenBudget = externalTask.getVariable("tokenBudget");
        if (tokenBudget != null && tokenBudget <= 0) {
            // Aborta sub-transacción mediante BPMN Error (No Retriable)
            externalTaskService.handleBpmnError(
                    externalTask,
                    "ERR_TOKEN_LIMIT_EXHAUSTED",
                    "El presupuesto de Tokens de LLM para esta instancia fue agotado.");
            return;
        }

        String systemPrompt = externalTask.getVariable("systemPrompt");
        if (systemPrompt == null) {
            systemPrompt = "Clasifique el texto proporcionado como estrictamente confidencial o público."; // Default
                                                                                                           // fallback
        }

        try {
            // Manda todos los metadatos (pre-packaged context) a la IA y falla transparente
            // s/n
            String aiResponse = orchestrator.executeGenerativeTask(systemPrompt, externalTask.getAllVariables());

            Map<String, Object> returnVariables = new HashMap<>();
            returnVariables.put("aiJsonResponse", aiResponse);

            // Completamos el external task en el motor devolviendo la salida
            externalTaskService.complete(externalTask, returnVariables);

        } catch (Exception e) {
            // Re-encolar evento en Camunda por Falla Temporal (LLM Providers down global)
            // (CA-14)
            externalTaskService.handleFailure(
                    externalTask,
                    "LLM_GLOBAL_FAILURE",
                    e.getMessage(),
                    3, // Retries
                    10000L // Cool-down 10 segundos
            );
        }
    }
}
