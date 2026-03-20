package com.ibpms.poc.infrastructure.mq.consumer;

import com.ibpms.poc.infrastructure.mq.config.TaskRescueRabbitConfig;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * CA-08: Consumidor de Rescate Asíncrono de Tareas.
 * Este Worker absorbe el tráfico AMQP y limpia las tareas del motor BPMN.
 */
@Service
public class TaskRescueConsumer {

    private static final Logger log = LoggerFactory.getLogger(TaskRescueConsumer.class);
    private final TaskService camundaTaskService;

    public TaskRescueConsumer(TaskService camundaTaskService) {
        this.camundaTaskService = camundaTaskService;
    }

    @RabbitListener(queues = TaskRescueRabbitConfig.QUEUE_NAME)
    public void consumeRescueMessage(Map<String, String> payload) {
        String action = payload.get("action");
        String userId = payload.get("userId");

        if ("UNCLAIM_ALL".equals(action) && userId != null) {
            log.info("[RABBIT-MQ] Consumiendo Rescate Masivo. Limpiando tareas vivas del Asignatario fantasma: {}", userId);
            
            try {
                // Paginar o buscar en Batch (Omitido tamaño real por PoC)
                List<Task> activeTasks = camundaTaskService.createTaskQuery().taskAssignee(userId).list();
                
                int exonerated = 0;
                for (Task task : activeTasks) {
                    camundaTaskService.setAssignee(task.getId(), null);
                    exonerated++;
                }
                
                log.info("[RABBIT-MQ] Exorcismo completado. {} Tareas devueltas al Pool Público con éxito.", exonerated);
            } catch (Exception e) {
                // En caso de caída de Camunda BD, la Excepción no Atrapada (throw) hará 
                // que Spring AMQP aplique reintentos nativos y finalmente a la Dead-Letter-Queue.
                log.error("[RABBIT-MQ] Fallo crudo interaccionando con Camunda DB. Reencolando mensaje (Retry Policy).", e);
                throw e; 
            }
        }
    }
}
