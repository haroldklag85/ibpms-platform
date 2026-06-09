# 🧪 Handoff QA — Cierre Sprint 6.2 (P1)

> Emisor: Arquitecto Líder | Fecha: 2026-04-20 
> ⚠️ EJECUTAR DESPUÉS de que Frontend y Backend hayan hecho push

## PRE-REQUISITO OBLIGATORIO
```bash
git pull origin sprint-6/uat-certification
```

## BLOQUE 1: Fix selector mismatch Kanban

En `j04-f7-kanban.e2e.spec.ts`, reemplazar TODOS los selectores de columna:

- `[data-testid="column-TODO"]` → `[data-testid="kanban-column-TODO"]`
- `[data-testid="column-IN_PROGRESS"]` → `[data-testid="kanban-column-IN_PROGRESS"]`
- `[data-testid="column-BLOCKED"]` → `[data-testid="kanban-column-BLOCKED"]`
- `[data-testid="column-DONE"]` → `[data-testid="kanban-column-DONE"]`

## BLOQUE 2: Implementar 11 tests.skip() vacíos

### F1-F2 (implementar 3, mantener 2 SKIP):
- CU-J04-10: Fill required fields → submit → toast success
- CU-J04-11: RYOW: tarea desaparece del Workdesk después de completar
- CU-J04-12: Panel métricas Total = N-1 post-completar
- SKIP: CU-J04-08 (Autoguardado — D-02: US-028 no implementado)
- SKIP: CU-J04-09 (Upload — D-02: US-028 no implementado)

### F4-F6 (implementar 2, mantener 2 SKIP):
- CU-J04-21: Director ve detalles tarea del asistente en modo delegación
- CU-J04-22: Click "Mis Tareas" → volver a vista propia
- SKIP: CU-J04-23/24 (Force Routing — D-03: requiere toggle admin previo)

### F7 (implementar 1):
- CU-J04-31: Happy path TODO → IN_PROGRESS → DONE (sin pasar por BLOCKED)

### F8-F12 (implementar 5, mantener 5 SKIP):
- CU-J04-35: Verificar banner degradación visible (data-testid="degradation-banner")
- CU-J04-42: GET /api/v1/tasks/skip-audit → verificar registros audit
- NEG-01: Submit con form vacío → botón disabled
- NEG-04: Login PERITO_A → intentar delegación director_1 → assert 403
- NEG-07: Sin rol admin → navegar /admin → redirect o 404
- SKIP: CU-J04-36/37 (Camunda restart — D-04: control Docker)
- SKIP: CU-J04-38 (5min inactividad — D-04: no automatizable)
- SKIP: NEG-02 (timeout red — D-04: no automatizable)
- SKIP: NEG-03 (50MB upload — D-02: US-028 pendiente)

### Formato de SKIP justificado:
```typescript
test('CU-J04-08 | Autoguardado', async ({ page }) => {
  test.skip(true, 'D-02: US-028 Autoguardado no implementado en V1');
});
```

## BLOQUE 3: Actualizar documentación
1. `coverage_matrix.md` → 44+ líneas (una por CU-J04)
2. `cierre_iteracion_s6_2.md` → actualizar veredicto con nuevo pass rate

## BLOQUE 4: Ejecución final
```bash
npx playwright test e2e/certification/ --reporter=html
```
Meta: ≥42/44 PASS (95%). Los 9 SKIP justificados no cuentan como FAIL.

## VALIDACIÓN
- Screenshot del reporte HTML de Playwright
- Commit: `test(qa): S6.2 implement 11 tests + fix selectors + close findings`
- Push: `sprint-6/uat-certification`
