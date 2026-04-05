# 📦 Handoff QA — US-034 / CA-4 a CA-10
## Validación de Resiliencia RabbitMQ: Idempotencia, Retry, DLQ y Health Check

| Metadato | Valor |
|---|---|
| **Emisor** | Arquitecto Líder (Orquestador) |
| **Destino** | Agente QA (Vitest / Playwright) |
| **Fecha** | 2026-04-05 |
| **Rama de trabajo** | `sprint-3/informe_auditoriaSprint1y2` |
| **Prioridad** | 🟡 Alta |
| **Iteración** | 70-DEV |
| **Dependencias** | Backend CA-4 a CA-10 + Frontend CA-8 deben estar COMPLETADOS |

---

## 1. Alcance de Validación

Debes verificar que los entregables del Backend y Frontend cumplen con los contratos técnicos de los CA-4 a CA-10 de la US-034.

---

## 2. Casos de Prueba Obligatorios

### 2.1. Backend — Tests de Integración (Vitest o Spring Test)

#### CA-4: Topología
- [ ] Verificar que `RabbitMqTopologyConfig.java` declara: `ibpms.exchange.topic` (Topic), `ibpms.exchange.dlx`, `ibpms.dlq.global`.
- [ ] Verificar que TODAS las colas declaran `x-dead-letter-exchange: ibpms.exchange.dlx`.
- [ ] Verificar existencia de `docs/architecture/rabbitmq_topology.md`.

#### CA-5: Idempotencia
- [ ] Verificar que tabla `ibpms_processed_messages` existe en scripts Liquibase.
- [ ] Test: Enviar un mensaje con `x-idempotency-key: "abc-123"` → debe procesarse.
- [ ] Test: Enviar el MISMO mensaje con `x-idempotency-key: "abc-123"` → debe hacer ACK silencioso sin reprocesar.

#### CA-6: Prioridades
- [ ] Verificar enum `MessagePriority` con valores P1, P2, P3 y prefetch counts correctos.
- [ ] Verificar que el header `x-priority` se inyecta en los MessageProperties.

#### CA-7: Retry Backoff
- [ ] Verificar configuración de retry: 4 intentos con delays 0ms/5s/30s/2min.
- [ ] Test: Simular `IOException` → debe reintentar 4 veces.
- [ ] Test: Simular `ValidationException` → debe ir directo a DLQ sin reintentos.
- [ ] Verificar headers en DLQ: `x-original-queue`, `x-delivery-count`, `x-last-error-message`.

#### CA-8: Endpoints DLQ Admin
- [ ] `GET /api/v1/admin/queues/dlq/summary` retorna estructura esperada.
- [ ] `POST /api/v1/admin/queues/dlq/retry` funciona y registra en `ibpms_audit_log`.
- [ ] `DELETE /api/v1/admin/queues/dlq/purge` requiere body con `justification` ≥ 20 chars.
- [ ] Verificar que acceso sin rol `ADMIN_IT` → HTTP 403.

#### CA-9: TTL y Archivado
- [ ] Verificar `x-message-ttl: 2592000000` en `ibpms.dlq.global`.
- [ ] Verificar existencia de tabla `ibpms_dlq_archive` en Liquibase.
- [ ] Verificar que `DlqArchiveJob` existe como `@Scheduled`.

#### CA-10: Health Check
- [ ] `GET /actuator/health/rabbitmq` retorna status UP cuando RabbitMQ está activo.
- [ ] Verificar existencia de tabla `ibpms_queue_fallback` en Liquibase.
- [ ] Verificar que el Circuit Breaker existe como bean/componente Spring.

### 2.2. Frontend — Tests Playwright/Vitest

#### CA-8: DLQ Dashboard
- [ ] Verificar que `DlqDashboard.vue` NO contiene datos MOCK hardcodeados.
- [ ] Verificar que `alert()` nativo NO existe en el componente.
- [ ] Verificar existencia de modal de confirmación para Purga (con textarea de justificación).
- [ ] Verificar existencia de modal de confirmación para Reintentar (con advertencia de idempotencia).
- [ ] Verificar que la ruta está protegida con `requiredRole: 'ADMIN_IT'`.

---

## 3. Contrato de No-Regresión QA

> [!CAUTION]
> - **PROHIBIDO** escribir código productivo (Java/Vue). Solo tests.
> - **PROHIBIDO** ejecutar tests sin que Backend y Frontend hayan terminado primero.
> - **PROHIBIDO** aprobar si los endpoints DLQ no registran en `ibpms_audit_log`.

---

## 4. Definition of Done QA

- [ ] Al menos 1 test de integración Spring Boot por cada CA del Backend (CA-4 a CA-10).
- [ ] Al menos 1 test Vitest/Playwright para el Dashboard DLQ refactorizado.
- [ ] Reporte de cobertura generado.
- [ ] Zero fallos críticos.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza empaquetando obligatoriamente con `git stash save "temp-qa-US034-CA4-CA10"`.
