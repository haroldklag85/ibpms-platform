# Handoff Backend — Sprint 5, Iteración 4 (Blindaje: QA Defensivo y Flujos Negativos)

> **Fecha:** 2026-04-18 | **Iteración:** Sprint5-Iter4 | **Arquitecto:** Líder
> **US Objetivo:** US-002 (CA-21 al CA-28), US-029 (CA-31 al CA-37), US-007 (CA-21 al CA-24)
> **Rama de trabajo:** `main`

---

## 1. Contexto Estratégico

La Iteración 4 es la fase **final y defensiva** del Sprint 5. Su propósito es **blindar el sistema contra el fracaso**: caídas de red, despojos de tarea por supervisores, timeouts de SLA, condiciones de carrera en operaciones masivas, y estados inconsistentes. Aquí no se construyen flujos felices — se construyen **fortalezas contra el caos**. Todo el código producido en las Iteraciones 1, 2 y 3 ahora debe ser capaz de graceful degradation.

---

## 2. Alineación Arquitectónica

| ADR | Impacto en Iteración 4 |
|-----|------------------------|
| ADR-001 (Hexagonal) | Las excepciones de dominio (`ForceUnclaimException`, `SlaTimeoutException`) se crean en `domain/exception/`. Los Controllers solo las transmutan a HTTP. |
| ADR-003 (Camunda 7) | El Force-Unclaim debe sincronizar con Camunda `taskService.setAssignee(null)` atómicamente. |
| ADR-009 (PostgreSQL) | Nuevas columnas (`unclaim_reason`, `unclaimed_by`, `unclaimed_at`) deben gestionarse vía Liquibase changeset. |
| ADR-010 (Pirámide) | Tests `@WebMvcTest` para cada endpoint negativo. `@DataJpaTest` con Testcontainers para validar rollbacks. |
| ADR-011 (CQRS) | Los logs de auditoría de despojo (`force-unclaim`) se persisten como eventos inmutables (write-side). |

---

## 3. Alcance Técnico (CAs Backend Iter4)

### 3.1 US-002 — Caminos Infelices de Reclamación (CA-21 al CA-28)

- **CA-21 (Rollback Optimistic UI):** Implementar endpoint `POST /api/v1/workbox/tasks/{id}/rollback-claim` que revierte un claim fallido por timeout de red. El servicio debe verificar que el `assignee` actual sigue siendo el solicitante antes de liberar. Retornar HTTP 409 si otro usuario ya reclamó.

- **CA-22 (Separación Visual Bandeja/Cola):** Endpoint `GET /api/v1/workbox/tasks?view=PERSONAL|POOL` que filtre tareas por: `PERSONAL` = asignadas al usuario autenticado, `POOL` = disponibles para el grupo/rol del usuario. Usar `@Query` con predicado dinámico.

- **CA-23 (Agregación WebSocket Masiva):** Cuando se ejecute un `bulk-claim` o `bulk-unclaim`, NO emitir N eventos WebSocket individuales. Implementar un agregador que emita un único evento `TASKS_BULK_UPDATED` con la lista de `taskIds` afectados. Usar un buffer temporal de 200ms.

- **CA-24 (Umbrales Configurables SLA):** Endpoint `GET /api/v1/config/sla-thresholds` que retorne los umbrales del semáforo (verde \< X horas, amarillo \< Y horas, rojo >= Y horas). Valores leídos de `application.yml` con `@ConfigurationProperties`.

- **CA-25 (Recálculo SLA Visibilitychange):** Endpoint `GET /api/v1/workbox/tasks/{id}/sla-status` que recalcule en caliente el estado SLA (`GREEN`/`YELLOW`/`RED`/`EXPIRED`) basado en `created_at` + `sla_hours` de la tarea. NO cachear — siempre calcular al instante.

- **CA-26 (Relleno WebSocket post-Remove):** Cuando una tarea sale de la vista de un operador (por claim de otro), emitir evento WebSocket `TASK_POOL_REFRESH` indicándole al frontend que debe re-fetch la primera página del pool para rellenar el espacio vacío.

- **CA-27 (Vocabulario Completo WS):** Consolidar el vocabulario de eventos WebSocket en un `enum WebSocketEventType`: `TASK_CLAIMED`, `TASK_UNCLAIMED`, `TASK_COMPLETED`, `TASK_EXPIRED`, `TASK_POOL_REFRESH`, `TASKS_BULK_UPDATED`, `TASK_FORCE_UNCLAIMED`. Documentar cada uno con JavaDoc.

- **CA-28 (Prevención Carrera "Atender Siguiente"):** Implementar endpoint `POST /api/v1/workbox/tasks/claim-next` que seleccione la tarea más antigua del pool (`ORDER BY created_at ASC`) usando `SELECT FOR UPDATE SKIP LOCKED LIMIT 1` y la asigne atómicamente. Retornar la tarea reclamada o HTTP 204 si el pool está vacío. PROHIBIDO usar Redis para esta operación — debe ser puro PostgreSQL.

### 3.2 US-029 — Casos Negativos de Submit (CA-31 al CA-37)

- **CA-31 (Timeout HTTP Graceful):** Si el procesamiento del submit excede 30 segundos (configurable), el servicio debe retornar HTTP 504 Gateway Timeout con cuerpo RFC 7807 y registrar el evento parcial en `form_event_store` con status `TIMEOUT`. El formulario NO se marca como completado.

- **CA-32 (Retry Idempotente):** El cliente puede reintentar el submit con el mismo `x-idempotency-key`. Si el evento ya fue procesado exitosamente, retornar HTTP 200 con el resultado original (no re-procesar). Si el evento anterior fue `TIMEOUT`, permitir el reintento.

- **CA-33 (Validación Condicional por Etapa):** Si el formulario es multi-step (wizard), el endpoint `POST /api/v1/workbox/tasks/{id}/validate-step` debe validar solo los campos de la etapa actual, no todo el schema Zod. Recibir `stepIndex` como query param.

- **CA-34 (Aduana de Archivos Adjuntos):** Antes del submit final, ejecutar validación de adjuntos: tamaño máximo (configurable), extensiones permitidas (whitelist), y scan ClamAV si disponible. Retornar HTTP 422 con detalle del archivo rechazado.

- **CA-35 (Sesión Duplicada):** Si se detecta que el mismo `userId` tiene dos sesiones activas editando el mismo `taskId` (mediante tabla `active_form_sessions`), retornar HTTP 409 Conflict con mensaje: "Edición activa detectada en otra sesión. Cierra la otra pestaña."

- **CA-36 (Pre-Aviso Caducidad Borrador):** Endpoint `GET /api/v1/workbox/tasks/{id}/draft-ttl` que retorne los segundos restantes antes de que el borrador expire. Si TTL <= 0, retornar HTTP 410 Gone.

- **CA-37 (Captura Global HTTP 5xx):** En el `GlobalExceptionHandler`, asegurar que TODA excepción no mapeada retorne RFC 7807 con `type: "about:blank"`, `status: 500`, `detail: "Error interno del servidor"`. PROHIBIDO filtrar stack traces al cliente (PII de infraestructura).

### 3.3 US-007 — Salvaguarda DMN (CA-21 al CA-24)

- **CA-21 (Validación Post-Minificación XML):** Después de generar el DMN XML, ejecutar una validación SAX/StAX que confirme: (a) XML bien formado, (b) namespace `https://www.omg.org/spec/DMN/20191111/MODEL/` presente, (c) al menos un `<decision>` con `<decisionTable>`. Retornar HTTP 422 si falla.

- **CA-22 (Hit Policy No Autorizada):** Rechazar con HTTP 403 si el XML generado contiene `hitPolicy` de tipos no autorizados (configurables). Default: permitir solo `UNIQUE`, `FIRST`, `PRIORITY`. Rechazar `COLLECT`, `RULE_ORDER` a menos que el usuario tenga rol `ROLE_DMN_ADMIN`.

- **CA-23 (Rate Limiting Simulador):** Aplicar rate limiting de 10 solicitudes/minuto al endpoint `POST /api/v1/dmn/simulate` usando `@RateLimiter` de Resilience4j o un filtro personalizado. Retornar HTTP 429 con header `Retry-After`.

- **CA-24 (Timeout SLA Generación):** Si la generación DMN (IA o mock) excede 15 segundos, interrumpir con `CompletableFuture.orTimeout()` y retornar HTTP 504. Registrar el prompt truncado en tabla de auditoría para análisis posterior.

---

## 4. Reglas de Gobernanza Mandatorias

- **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
- **TDD:** Aplica estrictamente `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor).
- **Clean Code:** Aplica `.agents/skills/clean_code_standards/SKILL.md`.
- **Reconciliación:** Al finalizar, ejecuta internamente el workflow `.agent/workflows/reconciliacionCoberturaCa.md` para verificar que cada CA tiene commit asociado.
- **Router QA:** Tu código será evaluado bajo `.agent/workflows/router_certificacion_qa.md` — prepárate para auditoría estática (Nivel B.1).
- **Cierre Deuda:** Todo CA implementado debe seguir la trazabilidad exigida por `.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` (Fase 5: Coverage Matrix).

---

## 5. NFR/QA Strategy

- Tests de integración `@WebMvcTest` para TODOS los endpoints negativos (4xx, 5xx).
- Test `@DataJpaTest` con Testcontainers para `claim-next` (verificar SKIP LOCKED con queries concurrentes).
- Test de rate limiting con `@SpringBootTest` para el simulador DMN.
- El Quality Gate exige `mvn clean package` en BUILD SUCCESS antes de declarar cierre.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
