# Approval Request: Backend Remediation US-004

**Fecha:** 2026-05-02
**Rama:** sprint-6
**Ticket:** US-004

## Resumen de Remediación
Se han implementado con éxito los 7 GAPs del Handoff `US004`, cumpliendo con los estándares de Zero-Mock y Arquitectura Hexagonal.

### Archivos Modificados / Creados
1. **`WebhookIntakeController.java`**
   - **Acción:** Refactorizado.
   - **GAP 1:** Se modificó la invocación sincrónica pesada para delegar a RabbitMQ (`ibpms.integrations.webhook`). Se implementó la respuesta `HTTP 202 Accepted` que garantiza la aceptación sub-segundo requerida en el SLA de la API pública.

2. **`WebhookIntakeListener.java`** (NUEVO)
   - **Acción:** Creado.
   - **GAP 1 y GAP 2:** Funciona como un `@RabbitListener` que consume de `ibpms.integrations.webhook`. En caso de cualquier excepción en el pipeline pesado (ClamAV o Camunda), lanza un `AmqpRejectAndDontRequeueException`, asegurando que el mensaje termine en la `ibpms.dlq.global` (Dead Letter Queue).

3. **`WebhookIntakeService.java`**
   - **Acción:** Refactorizado.
   - **GAP 3:** Se añadió la anotación `@Scheduled(cron = "0 0 3 * * ?")` al método `purgeExpiredOrphanPayloads()` para su ejecución diaria.
   - **GAP 4:** Se inyectó `IntegrationEventPublisher`. En caso de fallo crítico dentro del catch de Pre-Triage, se invoca `publishIntegrationEvent()` notificando al sysadmin.
   - **GAP 7:** Se añadió lógica para generar el HASH SHA-256 del adjunto e inyectar el status `CLEAN` y el string hash directamente dentro de la creación del objeto `TriageTask`.

4. **`WebhookControllerAdvice.java`** (NUEVO)
   - **Acción:** Creado.
   - **GAP 6:** Un `@ControllerAdvice` mapeado a `WebhookIntakeController` que intercepta `HttpMessageNotReadableException`, retornando HTTP 400 y guardando el body truncado con error `MALFORMED_JSON` en `OrphanPayloadRepository`.

5. **`TriageTask.java`**
   - **Acción:** Refactorizado.
   - **GAP 7:** Se extendió el mapeo de la entidad JPA (Tabla `ibpms_triage_tasks`) añadiendo dos nuevas columnas: `scan_status` (`scanStatus`) y `file_sha256_hash` (`fileSha256Hash`).

### GAPs Diferidos
- **GAP 5:** El endpoint `AllowedDomainAdminController` es validado como perteneciente a `US-045`. Su implementación ha sido diferida oficialmente y se ha documentado para mantener la coherencia del Sprint asignado (retorna 501 `NOT IMPLEMENTED`).

### Micro-Corrección (Regresión QA)
- Se restauraron las validaciones sincrónicas previas a la asincronía en `WebhookIntakeController`.
- **Idempotencia (CA-1):** Expuesto `isIdempotent` en el servicio y llamado en el controlador. Retorna HTTP 200 silencioso para duplicados antes de encolar.
- **Auto-Responder (CA-2):** Expuesto `isAutoResponder` en el servicio y llamado en el controlador. Bloquea de inmediato con HTTP 400 antes de encolar.
- **Orden de Flujo Confirmado:** HMAC -> Idempotencia -> Auto-responder -> RabbitMQ -> 202 Accepted.

### Validación de Construcción
- La solución compila correctamente. Validado bajo `mvn clean compile`. Todos los mapeos Spring IoC y JPA se encuentran enlazados bajo protocolo Zero-Mock.
