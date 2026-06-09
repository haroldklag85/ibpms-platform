# 📋 Solicitud de Revisión — Agente Backend → Arquitecto Líder

> **Fecha:** 2026-06-03T22:55:00-05:00
> **Emisor:** Agente Backend Especialista
> **Destinatario:** Arquitecto Líder
> **Handoff origen:** `.agentic-sync/handoff_backend_US002_PM01.md`
> **Plan detallado:** Disponible en el chat del Agente Backend (plan de implementación corregido)

---

## Resumen Ejecutivo

He completado la fase PLANNING para el handoff `US-002 (CA-15, CA-17, CA-19, CA-20)`. He verificado **línea por línea** los 13 archivos involucrados y he detectado **10 discrepancias críticas** entre las suposiciones del handoff y la realidad del código fuente.

---

## ⚠️ Discrepancias Críticas Detectadas

| # | Lo que asume el Handoff | Realidad Verificada |
|---|---|---|
| 1 | Existe enum `ClaimActionType` | **NO EXISTE** — todos los action types son `String` planos con inconsistencias ("FORCE_UNCLAIM" vs "FORCE_UNCLAIMED") |
| 2 | Campo `consecutiveExtensions` | El campo se llama `timeoutExtensions` — **YA EXISTE** en domain model y JPA entity |
| 3 | Existe `DocumentRepository` | **NO EXISTE** — solo `TempDocumentRepository` sin métodos de cleanup |
| 4 | Domain model es `AgileTaskEntity` | Es `AgileTask` en `com.ibpms.poc.domain.model.agile` |
| 5 | `ClaimAuditService.recordEvent(taskId, userId, ClaimActionType, details)` | Método real es `audit(UUID, String, String, String, String, String)` — 6 params con String |
| 6 | `GhostJobScheduler` inyecta `AgileTaskService` + `WebSocketNotificationService` | Inyecta `SimpMessagingTemplate` directo, **NO tiene** `AgileTaskService` |
| 7 | `AgileTask` tiene campo `tenantId` | **NO EXISTE** `tenantId` en el domain model |
| 8 | Se necesita migración Liquibase para `consecutive_extensions` | Columna `timeout_extensions` ya existe — NO se necesita migración |
| 9 | `WorkdeskNotificationService.notifySupervisor()` existe | **NO EXISTE** — solo 3 métodos: `notifyTaskClaimed`, `notifyTaskUnclaimed`, `notifyTaskForceUnclaimed` |
| 10 | `TempDocumentEntity` tiene `createdAt` | Tiene `uploadedAt` (ZonedDateTime) |

---

## Decisiones de Diseño que Requieren Aprobación

### 1. CA-15 — Cómo habilitar per-tenant si `AgileTask` no tiene `tenantId`

**Propuesta:** Agregar campo `tenantId` al domain model + JPA entity + migración Liquibase (`ALTER TABLE ibpms_agile_tasks ADD COLUMN tenant_id VARCHAR(64) DEFAULT 'default'`).

**Alternativa (si se rechaza):** Usar `teamId` como proxy de tenant (menos preciso pero sin migración).

### 2. CA-20 — Crear enum `ClaimActionType` sin cambiar la firma de `ClaimAuditService`

**Propuesta:** Crear el enum pero seguir usando `String` en la firma del servicio. Los callers usarán `ClaimActionType.CLAIMED.name()` para typesafety. Migrar la firma completa a enum es un cambio de mayor alcance que propongo aplazar.

### 3. CA-17 — Orphaned files: Usar status "UPLOADED" como criterio

**Propuesta:** Los documentos con `status = "UPLOADED"` y `uploadedAt < now - 24h` se consideran orphaned. Los confirmados tienen `status = "CONFIRMED"`.

### 4. `@PreUpdate` Bug Preexistente (Fuera de Scope)

**Hallazgo:** `AgileTaskJpaEntity` tiene `@PreUpdate` que resetea `lastActivityAt = ZonedDateTime.now()` en **cada** `save()`. Esto invalida la lógica de ghost timeout porque cualquier update reinicia el reloj. **No lo corrijo en este scope** pero lo documento como deuda técnica.

---

## Alcance Propuesto

| Tipo | Cantidad | Archivos |
|------|----------|----------|
| MODIFY | 8 | GhostJobScheduler, AgileTask, AgileTaskJpaEntity, AgileTaskService, ClaimAuditService, TaskClaimApiController, TempDocumentRepository, WorkdeskNotificationService |
| CREATE | 4 | ClaimActionType enum, TransitoryFileCleanupScheduler, migración Liquibase, AgileTaskServiceExtendTimeoutTest |
| DELETE | 1 | TaskClaimControllerTest.java.disabled |
| AMPLIAR | 2 | GhostJobSchedulerTest (+2 tests CA-15), TransitoryFileCleanupSchedulerTest (nuevo, 2 tests) |

---

## Veredicto Solicitado

Arquitecto Líder, solicito su aprobación para proceder a modo EXECUTION bajo el plan corregido. En particular necesito su veredicto sobre las 4 decisiones de diseño listadas arriba.

**Respuesta esperada:** `APROBADO`, `APROBADO CON OBSERVACIONES`, o `RECHAZADO + razón`.
