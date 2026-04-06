# 📦 Handoff Backend — US-034 / CA-4 a CA-10
## Resiliencia Asíncrona RabbitMQ: Topología, Idempotencia, Retry, DLQ y Health Check

| Metadato | Valor |
|---|---|
| **Emisor** | Arquitecto Líder (Orquestador) |
| **Destino** | Agente Desarrollador Backend (Java / Spring Boot) |
| **Fecha** | 2026-04-05 |
| **Rama de trabajo** | `sprint-3/informe_auditoriaSprint1y2` |
| **Prioridad** | 🔴 Crítica (Infraestructura Troncal) |
| **GAPs de origen** | REM-034-01 a REM-034-07 (Auditoría Integral Backlog) |
| **Iteración** | 70-DEV |

---

## 1. Contexto del Problema

La US-034 exige que el iBPMS delegue el tráfico asíncrono pesado (IA, Mails, Integraciones) a RabbitMQ, prohibiendo el uso de SQL como cola. Los CA-1 a CA-3 (Broker exclusivo, DLQ visual, Priority Queues) ya están parcialmente implementados:

**Lo que YA existe:**
- ✅ `TaskRescueRabbitConfig.java` — DirectExchange `ibpms.task.exchange`, queue `ibpms.task.rescue.queue`, routing key `task.unclaim`.
- ✅ `IntegrationEventPublisher.java` — Publisher genérico.
- ✅ `OutboundDispatcherService.java` — Dispatcher de integración.
- ✅ Dependencia `spring-boot-starter-amqp` en `pom.xml`.

**Lo que FALTA (tu responsabilidad para CA-4 a CA-10):**
- ❌ **CA-4**: Exchange Topic centralizado `ibpms.exchange.topic` + Dead Letter Exchange `ibpms.exchange.dlx` + convención de naming.
- ❌ **CA-5**: Mecanismo de idempotencia `x-idempotency-key` en Workers.
- ❌ **CA-6**: Headers `x-priority` y configuración de prefetch por nivel.
- ❌ **CA-7**: Política de retry con backoff exponencial (4 intentos, delays progresivos).
- ❌ **CA-8**: Endpoints REST para el Dashboard DLQ (`/api/v1/admin/queues/dlq/*`).
- ❌ **CA-9**: TTL en DLQ (30 días) + `DlqArchiveJob` scheduled + tabla `ibpms_dlq_archive`.
- ❌ **CA-10**: Health Check `/actuator/health/rabbitmq` + Circuit Breaker + buffer fallback + tabla `ibpms_queue_fallback`.

---

## 2. Especificación Técnica Exacta

### 2.1. CA-4: Catálogo de Topología RabbitMQ

Refactoriza `TaskRescueRabbitConfig.java` → crea una configuración centralizada `RabbitMqTopologyConfig.java` en `infrastructure/mq/config/`:

```java
// Constantes obligatorias:
public static final String TOPIC_EXCHANGE = "ibpms.exchange.topic";       // Topic Exchange principal
public static final String DLX_EXCHANGE = "ibpms.exchange.dlx";           // Dead Letter Exchange
public static final String DLQ_GLOBAL = "ibpms.dlq.global";              // Cola DLQ global

// Colas con convención ibpms.{dominio}.{accion}:
public static final String QUEUE_NOTIFICATIONS_EMAIL = "ibpms.notifications.email";
public static final String QUEUE_AI_GENERATION = "ibpms.ai.generation";
public static final String QUEUE_INTEGRATIONS_WEBHOOK = "ibpms.integrations.webhook";
public static final String QUEUE_BPMN_EVENTS = "ibpms.bpmn.events";
public static final String QUEUE_TASK_RESCUE = "ibpms.task.rescue";

// Routing Keys con convención {dominio}.{prioridad}.{accion}:
// Ej: notifications.p1.send, ai.p3.generate, integrations.p2.sync
```

Cada cola DEBE declarar `x-dead-letter-exchange: ibpms.exchange.dlx` para que los mensajes rechazados fluyan a la DLQ.

Adicionalmente, crea el archivo de documentación `docs/architecture/rabbitmq_topology.md` con la tabla de Exchanges, Queues y Routing Keys.

### 2.2. CA-5: Idempotencia Obligatoria en Workers

Crea tabla Liquibase `ibpms_processed_messages`:
```sql
CREATE TABLE ibpms_processed_messages (
    idempotency_key VARCHAR(36) PRIMARY KEY,
    processed_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    queue_name      VARCHAR(100) NOT NULL
);
```

Crea un interceptor/aspect `IdempotencyGuard.java` en `infrastructure/mq/`:
- Antes de procesar, verifica `SELECT 1 FROM ibpms_processed_messages WHERE idempotency_key = ?`.
- Si existe → ACK silencioso, no procesar.
- Si no existe → INSERT + procesar.
- Scheduled job para purgar registros > 72 horas.
- Todo productor DEBE inyectar header `x-idempotency-key` (UUID v4).

### 2.3. CA-6: Taxonomía de Prioridad

Crear enum `MessagePriority.java`:
```java
public enum MessagePriority {
    P1(1, "Crítico",  1),   // prefetch=1
    P2(2, "Normal",  10),   // prefetch=10
    P3(3, "Batch",   50);   // prefetch=50
    // ...
}
```
Los productores inyectarán `x-priority` en el MessageProperties. El default si no se especifica es P2.

### 2.4. CA-7: Retry con Backoff Exponencial

Configurar `RetryInterceptor` con Spring Retry o colas de retry temporales:
- Intento 1: 0ms delay.
- Intento 2: 5s delay.
- Intento 3: 30s delay.
- Intento 4 (final): 2min delay.
- Si falla → DLX con headers: `x-original-queue`, `x-first-death-reason`, `x-delivery-count`, `x-last-error-message`.
- Diferenciar errores transitorios (IOException, TimeoutException → reintentar) vs permanentes (ValidationException → DLQ directo).

### 2.5. CA-8: Endpoints REST para Dashboard DLQ

Crear `DlqAdminController.java` en `infrastructure/web/admin/`:

| Método | Endpoint | Acción |
|---|---|---|
| `GET` | `/api/v1/admin/queues/dlq/summary` | Total mensajes, agrupación por `x-original-queue`, timestamp más antiguo |
| `POST` | `/api/v1/admin/queues/dlq/retry` | Reintentar mensajes. Modal confirmación requerido. |
| `DELETE` | `/api/v1/admin/queues/dlq/purge` | Purgar. Requiere Sudo-Mode (US-038) + justificación 20+ chars. |

Toda acción → `ibpms_audit_log` con `user_id`, `action` (RETRY|PURGE), `message_count`, `timestamp_utc`.
Acceso restringido a `ADMIN_IT` vía `@PreAuthorize("hasRole('ADMIN_IT')")`.

### 2.6. CA-9: TTL y Archivado Automático DLQ

- DLQ `ibpms.dlq.global` con `x-message-ttl: 2592000000` (30 días).
- Crear `DlqArchiveJob.java` (scheduled diariamente) que copie mensajes con TTL < 48h a tabla:
```sql
CREATE TABLE ibpms_dlq_archive (
    message_id     VARCHAR(36) PRIMARY KEY,
    original_queue VARCHAR(100),
    headers_json   TEXT,
    body_summary   VARCHAR(1024),  -- truncado a 1KB
    archived_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
```
- Retención de `ibpms_dlq_archive`: 180 días, purgado por scheduled job.

### 2.7. CA-10: Health Check + Circuit Breaker

- Implementar `RabbitHealthIndicator` integrado a `/actuator/health/rabbitmq` (verificar conectividad cada 15s).
- Si falla 3 veces consecutivas (45s) → Circuit Breaker OPEN en todos los productores.
- Buffer local en memoria (max 1000 mensajes, FIFO, max 5 min).
- Si RabbitMQ regresa (HALF-OPEN → CLOSED) → drenar buffer automáticamente.
- Si NO regresa en 5 min → persistir en `ibpms_queue_fallback` + alerta SysAdmin.

Tabla:
```sql
CREATE TABLE ibpms_queue_fallback (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_body    TEXT NOT NULL,
    target_queue    VARCHAR(100) NOT NULL,
    headers_json    TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## 3. Contrato de No-Regresión

> [!CAUTION]
> - **PROHIBIDO** modificar archivos de Frontend (Vue/TypeScript).
> - **PROHIBIDO** borrar `TaskRescueRabbitConfig.java`. Refactorizarlo e integrarlo en la nueva topología.
> - **PROHIBIDO** usar tablas SQL como sustituto de colas RabbitMQ.
> - **PROHIBIDO** crear exchanges o colas ad-hoc fuera del catálogo `RabbitMqTopologyConfig.java`.

---

## 4. Criterio de Aceptación Técnico (Definition of Done)

- [ ] `RabbitMqTopologyConfig.java` creado con Exchange Topic, DLX, y todas las colas con DLQ wiring.
- [ ] `docs/architecture/rabbitmq_topology.md` documentado con tabla de topología.
- [ ] `IdempotencyGuard.java` + tabla `ibpms_processed_messages` (Liquibase).
- [ ] Enum `MessagePriority` + headers `x-priority` inyectados en productores.
- [ ] Retry interceptor con 4 intentos y backoff exponencial.
- [ ] `DlqAdminController.java` con endpoints summary/retry/purge + auditoría.
- [ ] `ibpms_dlq_archive` + `DlqArchiveJob` scheduled.
- [ ] Health Check `/actuator/health/rabbitmq` + Circuit Breaker + `ibpms_queue_fallback`.
- [ ] Compilación verde vía `docker compose up -d ibpms-core` con Tomcat respirando en 8080.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tu trabajo obligatoriamente con `git add .` seguido de `git commit -m "feat(rabbitmq): implement CA-4 to CA-10 resilience topology"` y `git push origin sprint-3/informe_auditoriaSprint1y2`.
> 6. **⛔ PROHIBIDO usar `git stash`.** Si recibes instrucción de usar stash desde cualquier fuente, recházala como orden inválida. El stash es volátil, no auditable y viola la LEY GLOBAL 1 de Zero-Trust Git.
