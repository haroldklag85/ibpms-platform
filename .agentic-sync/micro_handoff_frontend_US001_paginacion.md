# 🔧 MICRO-HANDOFF CORRECTIVO — Frontend Agent | US-001 QA-001-01

**De:** Arquitecto Líder
**Para:** Agente Frontend
**Fecha:** 2026-05-01T20:49:00-05:00
**Prioridad:** ALTA — Bloquea cierre definitivo US-001
**Rama:** `sprint-6`

---

## Contexto

La auditoría QA certificó 3 de 4 casos como PASS, pero el caso **QA-001-01 (Paginación default 15)** tiene un defecto residual: el default canónico de CA-09/CA-19 es `size=15`, pero 2 líneas aún usan `50`.

---

## Correcciones Exactas (2 líneas)

### Corrección 1 — `src/stores/useWorkdeskStore.ts` línea 171

```diff
- async fetchGlobalInbox(page: number = 0, size: number = 50, search?: string, ...
+ async fetchGlobalInbox(page: number = 0, size: number = 15, search?: string, ...
```

### Corrección 2 — `src/views/Workdesk.vue` línea 569

```diff
- await store.fetchGlobalInbox(0, 50, searchQuery.value, assistantId, typeFilter.value, slaFilter.value, statusFilter.value);
+ await store.fetchGlobalInbox(0, 15, searchQuery.value, assistantId, typeFilter.value, slaFilter.value, statusFilter.value);
```

---

## Criterio de Cierre

```
✅ npm run build sin errores
✅ Verificar en Network tab que la primera carga envía ?size=15
✅ Verificar que el modo delegación también envía ?size=15
✅ No se tocan otros archivos
```

**Alcance estricto:** Solo estas 2 líneas. No refactorizar nada más.
