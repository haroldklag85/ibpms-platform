# E2E Test Plan — iBPMS V1

> **Framework:** Playwright v1.59+ (Chromium)  
> **Entorno:** Vite dev server `:3000` → proxy `/api` → Spring Boot `:8080/api/v1`  
> **Ejecución:** `npx playwright test` desde `/frontend`  
> **Fecha:** 2026-04-13  
> **Autor:** Arquitecto Lead + QA Agent

---

## Estructura de Directorios E2E

```
frontend/e2e/
├── smoke/                      ← Sprint 0 (Gate Técnico)
│   └── app-loads.spec.ts       ✅ IMPLEMENTADO (4 tests)
├── j04/                        ← Sprint 1 (Journey J-04: Tarea → Form → CQRS)
│   ├── workdesk-list.spec.ts
│   ├── task-claim.spec.ts
│   ├── form-fill.spec.ts
│   ├── form-submit.spec.ts
│   └── cqrs-persistence.spec.ts
├── j02/                        ← Sprint 2 (Journey J-02: BPMN → Form → Deploy)
│   ├── bpmn-modeler.spec.ts
│   ├── iform-designer.spec.ts
│   ├── process-deploy.spec.ts
│   └── first-task-flow.spec.ts
├── j03/                        ← Sprint 3 (Journey J-03: RBAC → Governance)
│   ├── rbac-config.spec.ts
│   ├── route-gaslighting.spec.ts
│   └── sidebar-permissions.spec.ts
├── regression/                 ← Regresión acumulativa
│   └── full-suite.spec.ts
└── helpers/
    ├── auth.helpers.ts         ← JWT mock, login bypass
    ├── api.helpers.ts          ← API mocking patterns
    └── fixtures/               ← Test data
        ├── sample-bpmn.xml
        ├── sample-form.json
        └── sample-user.json
```

---

## Mapping de Tests por Gate de Sprint

### Sprint 0 — Gate Técnico

| Test File | ID | Descripción | Estado |
|-----------|:--:|-------------|:------:|
| `smoke/app-loads.spec.ts` | S0-SMOKE-01 | Backend health UP | ✅ |
| `smoke/app-loads.spec.ts` | S0-SMOKE-02 | Frontend sin JS errors | ✅ |
| `smoke/app-loads.spec.ts` | S0-SMOKE-03 | Vue app monta | ✅ |
| `smoke/app-loads.spec.ts` | S0-SMOKE-04 | Backend info responds | ✅ |

**Gate criteria:** 4/4 tests verdes → ✅ COMPLETADO

---

### Sprint 1 — Gate Técnico (J-04)

| Test File | ID | UAT Mapping | Descripción |
|-----------|:--:|:-----------:|-------------|
| `j04/workdesk-list.spec.ts` | S1-E2E-01 | CU-J04-01 | Tareas ordenadas por SLA |
| `j04/workdesk-list.spec.ts` | S1-E2E-02 | CU-J04-01 | Semáforo SLA con colores correctos |
| `j04/task-claim.spec.ts` | S1-E2E-03 | CU-J04-02 | Reclamar tarea exitosamente |
| `j04/task-claim.spec.ts` | S1-E2E-04 | CU-J04-10 | Concurrencia: solo 1 gana |
| `j04/form-fill.spec.ts` | S1-E2E-05 | CU-J04-03 | Formulario carga con campos |
| `j04/form-fill.spec.ts` | S1-E2E-06 | CU-J04-04 | Autoguardado en LocalStorage |
| `j04/form-fill.spec.ts` | S1-E2E-07 | CU-J04-05 | Upload de archivo (Upload-First) |
| `j04/form-submit.spec.ts` | S1-E2E-08 | CU-J04-06 | Envío con validación Zod |
| `j04/form-submit.spec.ts` | S1-E2E-09 | CU-J04-NEG-01 | Validación con campos vacíos |
| `j04/cqrs-persistence.spec.ts` | S1-E2E-10 | CU-J04-07 | Evento CQRS inmutable |
| `j04/cqrs-persistence.spec.ts` | S1-E2E-11 | CU-J04-08 | Confirmación RYOW |
| `smoke/app-loads.spec.ts` | REGRESSION | — | Regresión S0 |

**Gate criteria:** ≥10/11 new tests verdes + 4/4 regresión S0 = 14+ tests

---

### Sprint 2 — Gate Técnico (J-02)

| Test File | ID | UAT Mapping | Descripción |
|-----------|:--:|:-----------:|-------------|
| `j02/bpmn-modeler.spec.ts` | S2-E2E-01 | CU-J02-01 | Modeler BPMN carga |
| `j02/bpmn-modeler.spec.ts` | S2-E2E-02 | CU-J02-02 | Modelar proceso simple |
| `j02/bpmn-modeler.spec.ts` | S2-E2E-03 | CU-J02-03 | Guardar como borrador |
| `j02/iform-designer.spec.ts` | S2-E2E-04 | CU-J02-04 | Crear formulario iForm |
| `j02/iform-designer.spec.ts` | S2-E2E-05 | CU-J02-05 | Generar schema Zod |
| `j02/process-deploy.spec.ts` | S2-E2E-06 | CU-J02-07 | Desplegar proceso |
| `j02/process-deploy.spec.ts` | S2-E2E-07 | CU-J02-NEG-01 | BPMN inválido rechazado |
| `j02/first-task-flow.spec.ts` | S2-E2E-08 | CU-J02-08 | Iniciar instancia |
| `j02/first-task-flow.spec.ts` | S2-E2E-09 | CU-J02-09 | Operario ejecuta primera tarea |
| `j02/first-task-flow.spec.ts` | S2-E2E-10 | CU-J02-10 | Proceso completa E2E |
| `j04/*.spec.ts` | REGRESSION | — | Regresión S1 |

**Gate criteria:** ≥8/10 new tests verdes + 14+ regresión S0+S1 = 22+ tests

---

### Sprint 3 — Gate Técnico (J-03, J-06, J-07)

| Test File | ID | Descripción |
|-----------|:--:|-------------|
| `j03/rbac-config.spec.ts` | S3-E2E-01 | Crear rol en matriz RBAC |
| `j03/rbac-config.spec.ts` | S3-E2E-02 | Asignar permisos a rol |
| `j03/route-gaslighting.spec.ts` | S3-E2E-03 | Ruta protegida muestra 404 (no 403) |
| `j03/sidebar-permissions.spec.ts` | S3-E2E-04 | Sidebar muestra solo rutas autorizadas |
| `j03/sidebar-permissions.spec.ts` | S3-E2E-05 | Anti-FOUC: no flash de rutas prohibidas |
| `j06/kanban-board.spec.ts` | S3-E2E-06 | Tablero Kanban carga y drag-drop |
| `j07/sla-semaphore.spec.ts` | S3-E2E-07 | Semáforo SLA cambia con el tiempo |
| `j02/*.spec.ts, j04/*.spec.ts` | REGRESSION | Regresión S0+S1+S2 |

**Gate criteria:** ≥6/7 new tests verdes + 22+ regresión = 28+ tests

---

## Patrones de Testing Reutilizables

### 1. Auth Bypass (JWT Mock)

```typescript
// helpers/auth.helpers.ts
export async function mockAuth(page: Page, roles: string[] = ['ROLE_SUPER_ADMIN']) {
    await page.route('**/api/v1/auth/me', async route => {
        await route.fulfill({
            json: { username: 'e2e_user', roles, email: 'e2e@ibpms.local' }
        });
    });
    await page.addInitScript(({ roles }) => {
        localStorage.setItem('ibpms_token', 'e2e.jwt.token');
        localStorage.setItem('ibpms_user', JSON.stringify({ username: 'e2e_user', roles }));
        sessionStorage.setItem('ibpms_token', 'e2e.jwt.token');
    }, { roles });
}
```

### 2. API Mock Pattern

```typescript
// helpers/api.helpers.ts
export async function mockTaskList(page: Page, tasks: any[]) {
    await page.route('**/api/v1/tasks*', async route => {
        await route.fulfill({ json: { content: tasks, totalElements: tasks.length } });
    });
}
```

### 3. Screenshot Evidence

```typescript
// Capturar screenshot como evidencia del Gate
await page.screenshot({
    path: `test-results/gate-s${sprintNumber}-${testId}.png`,
    fullPage: true
});
```

---

## Ejecución

```bash
# Ejecutar todos los smoke tests
npx playwright test e2e/smoke/

# Ejecutar tests de un journey específico
npx playwright test e2e/j04/

# Ejecutar regresión completa
npx playwright test e2e/

# Ejecutar con UI para debug
npx playwright test --ui

# Generar reporte HTML
npx playwright test --reporter=html
npx playwright show-report
```

---

## Métricas de Coverage por Sprint

| Sprint | Tests Nuevos | Regresión | Total Acumulado | Gate |
|:------:|:----------:|:---------:|:--------------:|:----:|
| S0 | 4 | 0 | 4 | ✅ |
| S1 | 11 | 4 | 15 | ⬜ |
| S2 | 10 | 15 | 25 | ⬜ |
| S3 | 7 | 25 | 32 | ⬜ |
| S4 | ≥6 | 32 | ≥38 | ⬜ |
| S5 | ≥7 | 38 | ≥45 | ⬜ |
| S6+ | ≥5 | 45 | ≥50 | ⬜ |

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-13 | Creación: estructura E2E, mapping por gate, patrones reutilizables | Arquitecto Lead |
