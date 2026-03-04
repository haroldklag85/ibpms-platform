package com.ibpms.poc.application.service.ai;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Worker asíncrono para Tareas Generativas (Inteligencia Artificial).
 * Se acopla al tópico "ai-inference-topic" en Camunda a través del patrón
 * External Task Pub/Sub. Camunda NUNCA se bloquea síncronamente esperando al
 * LLM.
 */
@Component
@ExternalTaskSubscription("ai-inference-topic") // Tópico definido en el BPMN
public class GenerativeTaskWorker implements ExternalTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(GenerativeTaskWorker.class);

    private final MockLlmClient llmClient;

    public GenerativeTaskWorker(MockLlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String promptTemplateId = externalTask.getVariable("promptId");
        String userData = externalTask.getVariable("inputData");

        log.info("Adquiriendo tarea externa de Inferencia IA para la instancia: {}",
                externalTask.getProcessInstanceId());

        try {
            // Llamada "costosa" a la red (External LLM endpoint)
            String generatedResponse = llmClient.infer(promptTemplateId, userData);

            // Reportar a Camunda que la tarea finalizó con éxito de forma asíncrona
            externalTaskService.complete(externalTask, Collections.singletonMap("aiResponse", generatedResponse));

            log.info("Inferencia IA finalizada. Tarea asincrónica reportada como completada.");
        } catch (Exception e) {
            // Reportar falla (BPMN Error o Incident)
            log.error("Fallo durante la ejecución de la Inferencia IA", e);
            externalTaskService.handleFailure(
                    externalTask,
                    e.getMessage(),
                    "Technical failure connecting to LLM",
                    0,
                    1000 // retry timeout
            );
        }
    }
}
