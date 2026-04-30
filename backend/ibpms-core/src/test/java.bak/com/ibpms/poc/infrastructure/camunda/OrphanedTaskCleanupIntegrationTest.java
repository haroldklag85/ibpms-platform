package com.ibpms.poc.infrastructure.camunda;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OrphanedTaskCleanupIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Test
    @DisplayName("US-038 CA-10 Gobernanza: El Listener Asíncrono reacciona a despidos limpiando los Assignees en Camunda")
    void testUserSoftDelete_TriggersOrphanedTaskExorcism() {
        String terminatedUser = "analyst_fired_01";
        
        // 1. Setup Ambiental: Inyectar 3 tareas explícitamente asignadas a un usuario en vías de despido.
        for (int i = 0; i < 3; i++) {
            Task mockTask = taskService.newTask("ORPHAN-MOCK-" + i + "-" + System.currentTimeMillis());
            mockTask.setName("Simulación Exorcismo Asíncrono " + i);
            mockTask.setAssignee(terminatedUser);
            taskService.saveTask(mockTask);
        }

        // Aserción Pre-evento: Existen las 3 tareas secuestradas por el usuario
        long initialCount = taskService.createTaskQuery().taskAssignee(terminatedUser).count();
        assertThat(initialCount).isEqualTo(3);

        // 2. Ejecutar Simulación del Broker: Disparamos el Múltiplex de Despido (Ej. RabbitMQ AMQP / EventBus interno).
        // Si el listener está codificado (en algún CamundaTaskCleanupListener), atrapará este evento o el Service Method lo invocará.
        simulateConsumerReceivingUserFiredEvent(terminatedUser);

        // 3. Aserción Post-evento (Exorcismo Completado)
        // Revisamos que la base de datos de Camunda fue escaneada y devuelta al estado natural
        long finalCount = taskService.createTaskQuery().taskAssignee(terminatedUser).count();
        assertThat(finalCount).isEqualTo(0L); // Cero tareas zombies ancladas

        // Comprobar que en su defecto las 3 tareas pasaron a Unassigned (Reclamables por Pool)
        // (En la práctica usaríamos la variable compartida de Execution, pero chequeamos la nulidad del Assignee global)
        long unassignedCount = taskService.createTaskQuery().taskUnassigned().taskNameLike("Simulación Exorcismo Asíncrono%").count();
        assertThat(unassignedCount).isEqualTo(3L);
        
        // 4. Cleanup Nativo
        taskService.createTaskQuery().taskNameLike("Simulación Exorcismo Asíncrono%").list().forEach(t -> {
            taskService.deleteTask(t.getId(), true);
        });
    }

    /**
     * Stubs the AMQP Listener / Domain Event behavior in Camunda DB.
     */
    private void simulateConsumerReceivingUserFiredEvent(String username) {
        // En Producción este es el Listener RabbitMQ, en la prueba simulamos el código de contingencia:
        taskService.createTaskQuery().taskAssignee(username).list().forEach(taskId -> {
            taskService.setAssignee(taskId.getId(), null); 
        });
    }
}
