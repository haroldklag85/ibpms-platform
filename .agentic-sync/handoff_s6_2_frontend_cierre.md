# 🎨 Handoff Frontend — Cierre Sprint 6.2 (P0)

> Emisor: Arquitecto Líder | Fecha: 2026-04-20 | Meta: ≥95% PASS rate UAT

## Diagnóstico del Arquitecto
El PASS rate actual es 10% (5/48). La causa raíz son 3 defectos en tu código:

1. **Login Break-Glass tiene la API call COMENTADA** (`Login.vue` L243)
2. **Redirect post-login va a `/` en vez de `/workdesk`** (`Login.vue` L246)
3. **Workdesk.vue tiene CERO `data-testid`** (886 líneas sin ninguno)

## BLOQUE 1: Fix Login Break-Glass — BLOQUEANTE

En `src/views/Login.vue`, función `handleEmergencyLogin()`:

1. DESCOMENTAR la llamada real POST al backend (L243)
2. Almacenar el JWT real: `const { token } = response.data; authStore.login(token);`
3. CAMBIAR `router.push('/')` → `router.push('/workdesk')`

## BLOQUE 2: Inyectar 22 data-testid en Workdesk.vue — BLOQUEANTE

Inyecta EXACTAMENTE estos nombres en los elementos indicados:

- `<table>` principal → `data-testid="task-list"` (L245)
- `<thead>` → `data-testid="task-list-header"` (L246)
- Cada `<tr>` de tarea → `:data-testid="'task-row-' + (task.unifiedId || task.originalTaskId)"` (L257)
- Botón claim → `:data-testid="'claim-button-' + (task.unifiedId || task.originalTaskId)"` (L312)
- Aside métricas → `data-testid="metrics-panel"` (L352)
- Métrica Total → `data-testid="metric-total-tasks"` (L358)
- Métrica Vencidas → `data-testid="metric-overdue-tasks"` (L367)
- Métrica Por Expirar → `data-testid="metric-expiring-tasks"` (L377)
- CQRS status → `data-testid="metric-cqrs-status"` (L389)
- Input búsqueda → `data-testid="workdesk-search-input"` (L82)
- Empty state div → `data-testid="empty-state"` (L232)
- Banner degradación → `data-testid="degradation-banner"` (L128)
- SLA pill → `:data-testid="'sla-pill-' + (task.unifiedId || task.originalTaskId)"` (L282)
- Toggle delegación (botón DELEGATED) → `data-testid="toggle-delegation"` (L40)
- Banner delegación → `data-testid="delegation-banner"` (L107)
- Botón skipeo → `data-testid="btn-skipeo"` (L429)
- Select motivo skip → `data-testid="select-skip-reason"` (L458)
- Textarea detalle → `data-testid="textarea-skip-detail"` (L468)
- Botón confirmar skip → `data-testid="confirm-skip"` (L474)
- Form container → `data-testid="form-container"` (L421)
- Botón form submit → `data-testid="form-submit"` (L432)
- Botón Force Routing → `data-testid="btn-force-routing"` (L221)

## BLOQUE 3: Fix SkipReason values

Los `<option>` values del select de skipeo están en ESPAÑOL. Cambiar a INGLÉS:

- `CLIENTE_NO_RESPONDE` → `CLIENT_NO_RESPONSE`
- `REQUIERE_DOCUMENTACION` → `REQUIRES_DOCUMENTATION`
- `FUERA_DE_AREA` → `OUT_OF_AREA`
- `OTRO` → `OTHER`

Y cambiar la condición v-if del detalle: `skipForm.reason === 'OTRO'` → `skipForm.reason === 'OTHER'`

## VALIDACIÓN
- `npm run build` → exit code 0
- Commit: `fix(frontend): CR-1/2/3 login real + 22 data-testid + SkipReason`
- Push: `sprint-6/uat-certification`
