# 🧪 Handoff QA — US-017 STABILIZE (Sprint PM-01, Slot 5) — DIFERIDO

> **Fecha de creación**: 2026-06-09  
> **Sprint**: PM-01 | **Slot**: 5 | **Cadena**: 2 — Core Workdesk  
> **US**: US-017 — Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing)  
> **Branch**: `sprint-8/pm-01/us-017-stabilize`  
> **Estado**: ⏸️ DIFERIDO — Ejecutar DESPUÉS de que Backend y Frontend completen y hagan push  

---

## CAs a Validar (26 total)

### Backend (CA-01 a CA-18):
| CA | Escenario Gherkin | Endpoint a Testear |
|----|-------------------|--------------------|
| CA-01 | Separación CQRS + Event Sourcing | `POST /api/v1/workbox/tasks/{id}/complete` → Verificar que graba en `form_event_store` |
| CA-02 | Solo DTO minificado va a Camunda | Verificar que `ACT_RU_VARIABLE` NO tiene payload completo |
| CA-03 | Rollback Saga | Simular fallo Camunda → verificar `FORM_SUBMIT_ROLLED_BACK` |
| CA-04 | Auto-Claim grupo | Enviar `POST /complete` sin assignee → verificar auto-claim |
| CA-06 | Schema Event Store | Verificar tabla `form_event_store` tiene las 3 columnas de tipos |
| CA-07 | Endpoints Draft | `GET/PUT/DELETE /draft` — CRUD completo |
| CA-10 | Rollback inmutable | El evento original NO se borra → evento compensatorio creado |
| CA-14 | Rate-limiting | Enviar 7 PUT /draft en 1 minuto → el 7mo recibe HTTP 429 |
| CA-15 | Event Reference | Response de `/complete` incluye `eventReference` de 12 chars |

### Frontend (CA-19 a CA-26):
| CA | Escenario | Verificación Visual |
|----|-----------|---------------------|
| CA-19 | Debounce 5s | Toast NO aparece si sync < 5s |
| CA-20 | Posición inferior izquierda | Verificar CSS |
| CA-21 | Sin jerga técnica | Textos: "Guardando cambios", "Sin conexión" |
| CA-22 | No-bloqueante | Interacción posible con toast visible |
| CA-23 | Modo degradado | Icono 🔴/🟡 activo |
| CA-24 | Reconexión auto | Sin botón "Reintentar" |
| CA-25 | Feedback verde 3s | Toast verde, desaparece en 3s |
| CA-26 | Error > toast | HTTP 500 oculta toast de conexión |

---

## Tests E2E Existentes

Los siguientes archivos de test ya existen y deben ejecutarse:
- `e2e/certification/us017-cqrs-event-sourcing.spec.ts`
- `e2e/certification/us017-cqrs-toast.spec.ts`
- `e2e/certification/us017-connection-toast.e2e.spec.ts`
- `e2e/certification/phase8-11-degradation-cqrs.spec.ts`

---

## Referencia Obligatoria

Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.
