# 📋 Solicitud de Revisión — Agente Frontend US-002 PM-01

> **Emisor:** Agente Frontend Especialista
> **Destinatario:** Arquitecto Líder
> **Fecha:** 2026-06-04
> **Rama:** `sprint-8/pm-01/us-002-claim`

---

## Resumen del Plan

He analizado exhaustivamente los 4 archivos objetivo y los 3 archivos de test existentes. El plan cubre **CA-16, CA-18, CA-19, CA-20 + Tests Vitest**.

### Divergencias Detectadas (Handoff vs Código Real)

> ⚠️ **IMPORTANTE**: El handoff asume una estructura de código que NO COINCIDE con la realidad. He adaptado todos los snippets prescriptivos:

| Aspecto | Handoff dice | Código Real | Adaptación |
|---------|-------------|-------------|-----------|
| Store pattern | Composition API | **Options API** | Uso `state()`, `actions:{}` |
| Styling | CSS scoped custom | **Tailwind utility classes** | Clases Tailwind coherentes |
| WebSocket | Native WS + `lastWsEvent` | **STOMP/SockJS** | Case en switch handler STOMP |
| Audit field | `actionType` | `action` | Mapeo sobre `event.action` |
| Test files | Crear nuevos | **Ya existen** | AMPLIAR archivos existentes |
| CA-18 | No implementado | **Parcialmente implementado** | Mejorar con nombre usuario |

### Cambios Planificados (7 archivos)

1. **`useWorkdeskStore.ts`** — +`ghostWarning` state, +`extendTimeout()` action, +`GHOST_WARNING` case en WS switch, +`dismissGhostWarning()`, +helpers toast DOM
2. **`ClaimAuditTrail.vue`** — Reemplazo de `getDotColor()`/`getActionBadge()` por `ACTION_STYLE_MAP` con 6 tipos + 2 legacy keys, template con iconos+labels
3. **`TaskPreviewModal.vue`** — +Banner CA-16 (nota interna con `mensajeInterno`), +CA-18 mejora (nombre usuario `claimedByName`), +`formatTimeAgo()` helper
4. **`Workdesk.vue`** — +Ghost Warning Toast (Transition, Teleport) con 2 botones acción, +`handleSaveDraft()` function
5. **`TaskPreviewModal.spec.ts`** — +4 tests (CA-16×2, CA-18×2)
6. **`ClaimAuditTrail.spec.ts`** — +3 tests (CA-20×3)
7. **`useWorkdeskStore.spec.ts`** — +3 tests (CA-19×3)

### Orden de Ejecución
Store → ClaimAuditTrail → TaskPreviewModal → Workdesk → Tests → Build → Git

---

## Solicitud Formal

Arquitecto Líder: solicito su aprobación para proceder con la ejecución del plan descrito. Las adaptaciones al código real son necesarias dado que los snippets prescriptivos del handoff no aplican directamente por diferencias en el stack (Options API, Tailwind, STOMP).

**¿Aprueba la ejecución?**

---

_Agente Frontend Especialista — Antigravity_
