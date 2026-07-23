# 🚀 HANDOFFS DE REMEDIACIÓN INTEGRAL — US-004 (Rev. 2)

**De:** Arquitecto Líder
**Fecha:** 2026-05-02 (Revisión completa post-validación cruzada)
**Rama:** `sprint-6`

---

## 1. 🛠️ Para: Agente Backend (Prioridad: CRÍTICA — 7 GAPs)

**Rol:** Ingeniero Backend (Spring Boot & Arquitectura Hexagonal)

**Instrucción:** Debes remediar 7 GAPs identificados en la auditoría de la US-004. Los primeros 4 son bloqueantes.

### GAP 1: [ARQ-004-01] Refactor Asíncrono (CA-17) — CRITICAL
El `WebhookIntakeController` ejecuta ClamAV + Camunda sincrónicamente, violando la promesa sub-segundo.
- **Acción:** Modifica `WebhookIntakeController.receiveWebhook()`. Después de validar HMAC (CA-10) e Idempotencia (CA-1), **publica** el payload serializado a la cola RabbitMQ `ibpms.integrations.webhook` (ya declarada en `RabbitMqTopologyConfig` Línea 19) y retorna inmediatamente `HTTP 202 Accepted`.
- **Acción:** Crea un `@RabbitListener` nuevo (ej. `WebhookIntakeListener.java` en `infrastructure/mq/`) que desencole y ejecute el pipeline pesado: Whitelist → Tamaño → ClamAV → Camunda.

### GAP 2: [ARQ-004-02] Dead Letter Queue (CA-6 / CA-11) — CRITICAL
Si Camunda o ClamAV están caídos, los payloads se pierden irrecuperablemente.
- **Acción:** En el nuevo `@RabbitListener`, si Camunda falla, lanza `AmqpRejectAndDontRequeueException` para que el mensaje caiga automáticamente a la DLQ global (`ibpms.dlq.global`), la cual ya tiene configurado un TTL de 30 días.
- **Acción:** Si ClamAV falla (caso `UNAVAILABLE`), NO rechaces directamente. También publica a DLQ para reintento diferido.

### GAP 3: [ARQ-004-03] Cron de Purga (CA-13) — HIGH
El método `purgeExpiredOrphanPayloads()` es dead code.
- **Acción:** Agrega `@Scheduled(cron = "0 0 3 * * ?")` al método.
- **Acción:** Verifica que exista `@EnableScheduling` en la clase principal `Application.java` o en una `@Configuration`.

### GAP 4: [ARQ-004-04] Alerta Sysadmin (CA-5) — HIGH
El Sysadmin no recibe notificación cuando el motor falla.
- **Acción:** Inyecta `IntegrationEventPublisher` (o crea un `NotificationService`) en `WebhookIntakeService`.
- **Acción:** En el bloque `catch (Exception e)` (Línea 178), invoca el servicio para enviar un email de emergencia con: `taskId`, `messageId`, `stackTrace`, `timestamp`.

### GAP 5: [ARQ-004-05] CRUD Whitelist Scaffolding (CA-12) — HIGH
`AllowedDomainAdminController` tiene 3 endpoints que lanzan `NOT_IMPLEMENTED` (HTTP 501).
- **Decisión necesaria:** Si la US-045 es la dueña funcional, documéntalo como diferido con un `@Deprecated`. Si pertenece a US-004, implementa los 3 métodos reales usando `AllowedDomainRepository`. Incluye:
  - Validación de formato de dominio (RegEx).
  - Soft-delete (`is_active = false`).
  - Audit Log por operación.
  - `@PreAuthorize("hasAnyRole('ADMIN_SISTEMA','ADMIN_TENANT')")`.

### GAP 6: [ARQ-004-06] ControllerAdvice para Payloads Malformados (CA-3) — MEDIUM
Si Spring MVC rechaza un JSON malformado antes de llegar al Controller, no se registra en Huérfanos.
- **Acción:** Crea un `@ControllerAdvice` (ej. `WebhookExceptionHandler.java`) que intercepte `HttpMessageNotReadableException` en las rutas `/intake/webhook/**` y persista el body raw en `OrphanPayloadRepository` con motivo `MALFORMED_PAYLOAD`.

### GAP 7: [ARQ-004-07] Flag scan_status en archivos limpios (CA-11) — MEDIUM
Cuando ClamAV aprueba un archivo, no se persiste el flag `scan_status: CLEAN` ni el hash SHA-256.
- **Acción:** Agregar el campo `scanStatus` y `fileSha256Hash` en la entidad de almacenamiento de adjuntos.

### Violaciones ADR-001 (Hexagonal) — DEUDA TOLERADA V1
1. `WebhookIntakeService` importa `RuntimeService` de Camunda directamente. **Ideal V2:** crear `BpmnEnginePort` en `application/ports/out/`.
2. `WebhookIntakeService` importa `WebhookProperties` de `infrastructure.config`. **Ideal V2:** inyectar como DTO de dominio.
**Decisión:** Se toleran en V1 por pragmatismo. Documentar como deuda técnica V2.

### Gate de Cierre
- Ejecutar `mvn clean compile` sin errores.
- Levantar la app y confirmar que el endpoint de Webhook retorna `HTTP 202 Accepted` sin bloquear.
- Reportar en `approval_request_backend.md`.

---

## 2. 🏗️ Para: Agente Infra/DB (Prioridad: BAJA — Sin acciones)

**Rol:** Ingeniero DevOps y Base de Datos

**Instrucción:** La topología RabbitMQ requerida por la US-004 **ya está implementada** en `RabbitMqTopologyConfig.java`:
- Exchange: `ibpms.exchange.topic` ✅
- Cola: `ibpms.integrations.webhook` (con DLX configurado) ✅
- DLQ: `ibpms.dlq.global` (TTL 30 días) ✅
- Bindings: `integrations.#` → cola webhook ✅

**No tienes acciones pendientes** para esta remediación. Queda como verificativo: asegúrate de que el contenedor de RabbitMQ en `docker-compose.yml` esté levantado y accesible en `localhost:5672` para que el Backend pueda publicar.

---

## 3. 🧪 Para: Agente QA (Prioridad: BLOQUEADA hasta Backend)

**Rol:** Ingeniero de Calidad

**Instrucción:** Una vez que Backend confirme la remediación de los GAPs 1-4, valida empíricamente:

| ID Test | Escenario | Criterio de Aceptación |
|---------|-----------|------------------------|
| **QA-004-01** | Enviar POST a `/intake/webhook` con payload válido | Debe retornar `HTTP 202 Accepted` en < 500ms (CA-17). |
| **QA-004-02** | Apagar Camunda y enviar POST al webhook | El mensaje NO debe perderse. Verificar en RabbitMQ Management UI (`localhost:15672`) que reposa en la DLQ `ibpms.dlq.global` (CA-6). |
| **QA-004-03** | Enviar POST con `messageId` duplicado | Debe retornar `HTTP 200` silencioso sin crear nueva transacción (CA-1). |
| **QA-004-04** | Enviar POST con `senderEmail = no-reply@test.com` | Debe retornar `HTTP 400` con motivo `AUTO_RESPONDER_BLOCKED` (CA-2). |
| **QA-004-05** | Verificar que `purgeExpiredOrphanPayloads` se ejecute a las 3am | Validar con logs o inserción manual de un registro > 30 días (CA-13). |

Genera reporte de certificación cruzada.

---

## 4. 🎨 Para: Agente Frontend (Prioridad: INFORMATIVA)

**Rol:** Ingeniero Frontend (Vue3)

**Instrucción:** No tienes acciones directas sobre la US-004. La auditoría confirma que el Backend provee:
- Modelo `TriageTask` con campos `subject`, `senderEmail`, `attachmentCount`, `status`, `slaDeadline`.
- Endpoint de Pre-Triaje con SLA de 4 horas y semáforo calculable.

Toda la experiencia gráfica de la **Pantalla 16** (Bandeja Inteligente de Intake) queda bajo tu responsabilidad en la **US-029**:
- CA-14: Visor de mensaje + botones [Aprobar/Rechazar con motivo].
- CA-15: Menú desplegable "Tipo de Proceso" al aprobar.
- CA-16: Semáforos SLA (Verde → Amarillo → Rojo).

Prepárate para cuando abramos esa auditoría.
