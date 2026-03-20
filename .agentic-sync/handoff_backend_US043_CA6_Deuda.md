# Handoff Backend - Cierre Deuda Técnica (US-043: CA-6)

## Propósito
Saldar la deuda técnica del Criterio de Aceptación 6 (Alertas Preventivas de Quiebre de Nivel). Implementar el disparador de *Early Warning* cuando una tarea consuma el 80% de su SLA (o falten 2 horas) enviando una señal asíncrona que, por ahora, se materializará en un flag transaccional en la BD, preparando el terreno puro para el Motor de Notificaciones (US-049).

## Criterios de Aceptación Cubiertos (Backend)
* **CA-6 (Alertas Preventivas Early Warning):** 
    - Crear un mecanismo en Camunda (idealmente un `Timer Boundary Event` *non-interrupting* inyectado en las UserTasks o un Job global periódico).
    - Al detonar el 80% del SLA configurado, el motor ejecutará un `JavaDelegate` o Listener (`EarlyWarningSlaListener`).
    - Este Listener estampará un flag lógico (Ej: `camundaTaskService.setVariable(taskId, "isSlaAtRisk", true)`) en el contexto de la tarea para proveer inmediatez visual.
    - Emitirá un Evento de Dominio interno de Spring Boot (`SlaAtRiskEvent`), el cual será la bisagra perfecta para que, en un futuro, el Motor US-049 la capture y mande Push/Emails.

## Tareas Java (Prioridad 1 - Tarea Bloqueante)
1. Modificar el interceptor de creación de Tareas (o el XML maestro) para inyectar/calcular el Timer de Early Warning (80% del DueDate original).
2. Crear el Listener Asíncrono `EarlyWarningSlaListener` que reciba este evento, asiente la variable `"isSlaAtRisk": true` y publique el evento de dominio a nivel Spring.
