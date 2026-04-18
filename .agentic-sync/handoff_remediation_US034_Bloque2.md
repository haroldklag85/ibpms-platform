# Handoff de Remediación QA: US-034 Bloque 2 (Resiliencia Avanzada AMQP)

## 📌 Contexto y Gravedad Institucional
Durante la Auditoría de la Epica F (US-034) Bloque 2 (Criterios de Aceptación CA-6 al CA-10), se han identificado GAPs estructurales graves en el ecosistema AMQP de Spring Boot. 
Actualmente el broker RabbitMQ está expuesto a fallos en cascada estructurales, carece de trazabilidad de auditoría en purgas críticas y carece de soporte de enrutamiento resiliente mediante Circuit Breakers en capa productora. 

Para alcanzar el estado "All-Green" exigido corporativamente, debes neutralizar los 5 incidentes de seguridad e inestabilidad a nivel Backend.

## 🎯 Objetivo de este Handoff
El Agente Backend Developer Senior debe intervenir el núcleo AMQP (`AmqpConfig`, `DlqAdminController`, `MqMaintenanceJob` y productores/consumidores de la plataforma para codificar la red de seguridad de reintentos asimétricos y el circuito de tolerancia a fallos.

## 🛠 Tareas Críticas de Ejecución (Micro-Arquitectura Backend)

**1. CA-6: Taxonomía de Niveles y Prefetch (Capacity Planning)**
*   **Problema:** Operamos con un flujo ciego. No existen límites de concurrencia.
*   **Acción:** Define 3 `RabbitListenerContainerFactory` independientes en `AmqpConfig.java`. 
    *   `p1ContainerFactory`: Prefetch Count de `1`. (Latencia crítica)
    *   `p2ContainerFactory`: Prefetch Count de `10`. (Normal)
    *   `p3ContainerFactory`: Prefetch Count de `50`. (Batch AI/Background)
*   Asigna estas factories a los `@RabbitListener` respectivos en el código según las responsabilidades de las colas.

**2. CA-7: Clasificación de Excepciones en Backoff**
*   **Problema:** El `RetryOperationsInterceptor` existente hace Backoff indiscriminado.
*   **Acción:** Modifica `AmqpConfig`. Implementa un `ConditionalRejectingErrorHandler` o asocia una regla de clasificación al RetryPolicy.
    *   *Fallos Permanentes* (ej: Validación JSON de negocio) -> Envío Directo al DLX sin reintentos.
    *   *Fallos Transitorios* (ej: Timeout BDD/WebHook) -> Ejecuta el Backoff Exp (1s.. 2min).

**3. CA-8: Rastro Forense en Gestión DLQ**
*   **Problema:** Los SysAdmins borran mensajes de la DLQ en `/purge` sin dejar huella loggable en SQL, vulnerando el esquema de Compliance iBPMS Zero-Trust.
*   **Acción:** Edita `DlqAdminController.java`. Inyecta el servicio/repositorio de Auditoría (`ibpms_audit_log` o la Entidad de logs operacionales). 
*   Persiste una tupla de auditoría detallando (UserID, Acción[Purge|Retry], Timestamp, Cantidad Mensajes) **antes** de retornar el ResponseEntity al UI.

**4. CA-9: Reglas de Archivo Legal DLQ (30 Días)**
*   **Problema:** `MqMaintenanceJob.java` purga BBDD, pero no "rescata" mensajes de la RabbitMQ global a punto de morir.
*   **Acción:** Agrega un nuevo método `@Scheduled` en `MqMaintenanceJob.java` que involucre inyectar `RabbitTemplate`. Debes leer físicamente (sondeo con `receive()`) la cola `ibpms.dlq.global`, identificar mensajes que crucen el umbral de longevidad, mapearlos al DTO SQL `ibpms_dlq_archive` invocando al Repositorio JPA, y ejecutarles el "Acknowledge" manual para borrarlos de la cola Rabbit sin destruir la Data de auditoría.

**5. CA-10: Fallback Multi-Cluster Transaccional (Circuit Breaker)**
*   **Problema:** Si el clúster AMQP muere (Conexión Rehusada), el core lanza 500s.
*   **Acción:** Crea un Wrapper de Productores (ej: `RabbitProducerComponent`) que contenga el `template.convertAndSend(...)`.
*   Anótalo con Restilience4J: `@CircuitBreaker(name="rabbitCluster", fallbackMethod="fallbackToSql")`.
*   Diseña el método de fallback: Si RabbitMQ detona 3 RequestExceptions, invoca el repositorio y persiste transaccionalmente el mensaje en la tabla `ibpms_queue_fallback`.

## ⚠️ Reglas de Gobernanza
- Manten el `pom.xml` inamovible (salvo extrema necesidad de imports Spring Retry o Resilience4j base que falten, aunque Resilience4J ya existe por el CrmAdapter).
- Todo código debe cumplir directivas **Hexagonales**: Interfaces para los dominios (Fallback/Logging) si procede.
- Corre la suite de pruebas tras cada intervención para garantizar "All-Green" local (ej: `mvn clean test`).

## 🏁 Criterio de Éxito
- Ningún test previo se quiebra.
- Resilience4J protege AMQP y los interceptores son conscientes de Exceptions Transitorias vs Permanentes. Envía la confirmación al Lead QA cuando hayas logrado neutralizar las 5 anomalías del Bloque 2.
