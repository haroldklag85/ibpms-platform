# 🧪 Handoff QA (APLAZADO) — US-002 (Claim/Unclaim de Tareas)

> **Sprint:** PM-01 | **Slot:** 1 | **Cadena:** 2 (Core Workdesk)
> **Rama de trabajo:** `sprint-8/pm-01/us-002-claim`
> **Emisor:** Arquitecto Líder | **Fecha:** 2026-06-03
> **Destinatario:** Agente QA
> **Estado:** ⏸️ **APLAZADO** — No ejecutar hasta que Backend y Frontend estén completados

---

## Motivo del Aplazamiento

El PM-IA indicó que el handoff de QA se aplaza para después de la ejecución de Backend y Frontend. Este archivo se mantiene como placeholder para documentar el alcance de validación esperado.

---

## Alcance de Validación al Activarse

### CAs a Validar (Pirámide Completa)

| CA | Descripción | Capa de Test | Prioridad |
|----|-------------|:------------:|:---------:|
| CA-1 | Reclamo Simultáneo (SKIP LOCKED) | E2E + Concurrencia | 🔴 Alta |
| CA-2 | Bulk Claim (lote de hasta 20) | E2E + Parcial Failure | 🔴 Alta |
| CA-4 | Liberación con Mensaje Interno | E2E | 🟡 Media |
| CA-5 | Modo Solo Lectura (Preview) | E2E | 🟡 Media |
| CA-6 | Ghost Job Timeout (Auto-Unclaim) | Integración | 🔴 Alta |
| CA-7 | Amnesia Transaccional (Modal) | E2E | 🟡 Media |
| CA-8 | Despojo Forzoso por Supervisor | E2E + RBAC | 🔴 Alta |
| CA-9 | Trazabilidad Forense (Audit Trail) | E2E | 🟡 Media |
| CA-10 | Resiliencia Offline (Optimistic UI) | E2E + Network | 🟡 Media |
| CA-11 | SKIP LOCKED (BD) | Integración | 🔴 Alta |
| CA-12 | WebSocket Post-Commit | E2E + Multi-session | 🔴 Alta |
| CA-13 | Validación Perimetral team_id | E2E + RBAC | 🔴 Alta |
| CA-14 | OpenAPI Annotations | Contrato (Swagger UI) | 🟢 Baja |
| CA-15 | Ghost Timeout per-tenant | Integración | 🟡 Media |
| CA-16 | Banner Nota Interna | E2E | 🟡 Media |
| CA-17 | Cleanup Archivos Transitorios | Integración | 🟢 Baja |
| CA-18 | Alerta Readonly ante Reclamo | E2E + WebSocket | 🟡 Media |
| CA-19 | Extensión Timeout (máx 2) | E2E + Business Rule | 🟡 Media |
| CA-20 | Motivos Enriquecidos Timeline | E2E | 🟢 Baja |
| CA-21 | Rollback Optimistic UI | E2E + Network | 🟡 Media |
| CA-22 | Separación Bandeja/Cola (Tabs) | E2E | 🟢 Baja |
| CA-23 | Claim-Next Atómico (Aggregated WS) | E2E | 🟡 Media |

### Deuda de Tests Detectada

| Hallazgo | Severidad | Acción Requerida |
|----------|:---------:|-----------------|
| `TaskClaimControllerTest.java.disabled` — test muerto, referencia clase inexistente | 🟡 | Eliminar |
| `WorkboxTaskControllerTest.java` — solo 2 tests (rollback) | 🔴 | Ampliar cobertura |
| `us002-workbox-kanban.spec.ts` — usa `route.fulfill()` mocks | 🔴 | Reescribir con backend real |
| Sin tests de concurrencia para SKIP LOCKED | 🔴 | Crear test multi-thread |

### Políticas de Ejecución

1. **ZERO-MOCK obligatorio**: Todos los tests E2E deben ejecutar contra backend real (puerto 8080) + PostgreSQL Dockerizado (puerto 5433).
2. **Evidencia adjunta**: Cada CA validado debe incluir screenshot o log como evidencia.
3. **Skill obligatorio**: `.agents/skills/qa_e2e_validation_audit/SKILL.md`
4. **Cobertura mínima DoD**: ≥ 80% de CAs validados E2E.
