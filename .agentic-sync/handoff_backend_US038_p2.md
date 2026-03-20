# Handoff Backend - Iteración 45 (US-038: CA-6 a CA-10)

## Propósito
Implementar la Gobernanza y Resiliencia de Tareas en el motor BPMN. Esto abarca bloquear transacciones anti-éticas (Segregación de Funciones), liberar tareas atrapadas por despidos usando mensajería asíncrona garantizada, y establecer trazabilidad logística (Log MDC).

## Criterios de Aceptación Cubiertos (Backend)
* **CA-6 (Segregación de Funciones - SoD):** 
    - Al invocar la completitud de una tarea (`BpmTaskService.completeTask`), verificar que el `userId` actual **no coincida** con el `initiator` (creador) del proceso, si el proceso dicta reglas rígidas (evaluar viabilidad de control global o por flag).
    - En V1, aplicar la regla dura: `Creator_ID != Approver_ID` para instancias específicas, lanzando un `HttpStatus.FORBIDDEN`.
    - Disparar un Spring Event genérico de Auditoría (`SecurityAnomalyEvent`).
* **CA-7 y CA-8 (El Exorcismo de Tareas - RabbitMQ Resiliency):** 
    - Al detectar la Desactivación de un Empleado o el Inicio de una Delegación programada.
    - Lanzar un mensaje a RabbitMQ: `{"action": "UNCLAIM_ALL", "userId": "marcos.perez"}`.
    - Consumidor RabbitMQ: Captura el mensaje, busca todas las Tareas activas en Camunda asignadas a dicho usuario (`taskService.createTaskQuery().taskAssignee("marcos.perez").list()`), iterando y ejecutando `taskService.setAssignee(tarea.getId(), null)`.
    - Si la BD Camunda falla, RabbitMQ aplicará la política de Reintentos (DLQ), garantizando que los "Casos Zombies" sean liberados.
* **CA-9 (Trazabilidad Quirúrgica / Correlation-ID):** 
    - Inyectar un Filtro Web (Ej: `MdcLogFilter`). Generar o propagar el header `X-Trace-ID`. Insertarlo en el *Mapped Diagnostic Context* (`MDC.put("traceId", traceId)`) para que SLF4J imprima el Rastro unificado en todos los logs de consola/archivo.

## Exclusiones V2
- Se excluye la exportación de Traces vía OpenTelemetry/Jaeger hacia un entorno microservicios (CA-9). La trazabilidad quedará estrictamente acoplada a Logs locales y Headers HTTP en la respuesta para depuración.

## Tareas Java (Prioridad 1 - Ejecución Inmediata)
1. Codificar el Interceptor SoD para el `BpmTaskService` (Juez y Parte).
2. Levantar Productor y Consumidor RabbitMQ para desasignación masiva (Unclaim).
3. Añadir el Filtro MDC para correlación de Logs.
