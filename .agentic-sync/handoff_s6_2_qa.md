# 🧪 Handoff QA — Sprint 6 / Iteración 6.2

> **Iteración:** Sprint 6 — Iteración 6.2 (Journey J-04 v2 — Certificación E2E Operario MVP)  
> **Rama de trabajo:** `sprint-6/uat-certification` (git pull origin sprint-6/uat-certification primero)  
> **US objetivo:** US-001, US-002, US-008, US-017, US-029, US-036, US-039, US-043, US-051  
> **Flujo:** Backend + Frontend → **QA**  
> **SSOT de referencia:** `docs/uat/casos_uso_uat_j04.md` (v2 — 45 escenarios)  
> **Plan aprobado:** `docs/sprints/sprint_plan_s6.md` §Iteración 6.2  
> **Autor:** Arquitecto Líder SW  
> **Fecha:** 2026-04-19

---

## 1. Metadatos y SSOT

| Parámetro | Valor |
|-----------|-------|
| **Sprint** | 6 — Iteración 6.2 |
| **Rama Git** | `sprint-6/uat-certification` |
| **Journey** | J-04 v2 — Operario MVP (45 escenarios, 12 fases) |
| **US** | US-001, US-002, US-008, US-017, US-029, US-036, US-039, US-043, US-051 |
| **Dependencia** | Backend T-1 a T-8 + Frontend T-7 deben estar pusheados ANTES de ejecutar specs |
| **Infraestructura** | Docker Compose E2E con seed data operacional (T-1 y T-2) |
| **Prerequisito** | It. 6.1 SELLADA ✅ |

**Fuentes de verdad:**
- **📋 LEER PRIMERO:** [`docs/uat/casos_uso_uat_j04.md`](../../docs/uat/casos_uso_uat_j04.md) (v2) — 904 líneas, 45 escenarios, 12 fases
- `docs/sprints/sprint_plan_s6.md` → Plan Iteración 6.2
- `.agentic-sync/coverage_matrix.md` → Estado actual de cobertura

---

## 2. Gobernanza Obligatoria

> [!CAUTION]
> **PROHIBICIONES ABSOLUTAS (Zero-Trust E2E):**
> - ❌ **PROHIBIDO** usar `page.route()` para interceptar/mockear respuestas API
> - ❌ **PROHIBIDO** usar `page.evaluate()` para inyectar estado en stores
> - ❌ **PROHIBIDO** mockear WebSocket, Camunda, o cualquier servicio backend
> - ✅ **OBLIGATORIO** ejecutar todo contra backend REAL (Docker Compose E2E)
> - ✅ **OBLIGATORIO** incluir `data-testid` en todos los selectores (no CSS classes)

---

## 3. Pre-Ejecución

### 3.1 Pull y Setup
```bash
git pull origin sprint-6/uat-certification
cd frontend && npm install
```

### 3.2 Levantar Infraestructura
```bash
docker-compose -f docker-compose.e2e.yml up -d
# Esperar healthchecks: postgres, redis, camunda, rabbitmq
# Verificar que seed-e2e.sql se ejecutó: docker logs postgres-e2e | grep "seed"
```

### 3.3 Verificar Login E2E
```bash
# Test rápido de login con break-glass
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"analista_n1","password":"Test123!"}'
# Debe retornar JWT con tenant_id=tenant_alpha
```

---

## 4. Bloques de Specs (6 Tareas QA)

### T-9: Re-run Lotes B3-B5 de It. 6.1 (🔴 P0)

Primero verificar que el data seed resolvió los fallos:

```bash
npx playwright test e2e/smoke-j04-operario.e2e.spec.ts
npx playwright test e2e/b20-dmn-dropdown.e2e.spec.ts
npx playwright test e2e/kanban-board.e2e.spec.ts
```

**Criterio:** Si los 3 pasan (7/7 total), el bloqueante de It. 6.1 está resuelto.

---

### T-10: Specs F1-F2 — Bandeja + Claim + Ejecución (🔴 P0)

**Archivo:** `frontend/e2e/j04-f1-f2-bandeja-ejecucion.e2e.spec.ts`

**Escenarios a implementar (12 tests):**

| Test | CU | Validación Principal |
|------|:--:|---------------------|
| 1 | CU-J04-01 | Workdesk carga en ≤2s con DataGrid 5+1 columnas, ≥1 tarea |
| 2 | CU-J04-02 | Panel métricas: Total Tareas, Vencidas (badge pulsante), Por Expirar, CQRS status |
| 3 | CU-J04-03 | Ordenamiento SLA: ⚫ vencidas primero → ⚡ rojo → ⏳ amarillo → ✔️ verde |
| 4 | CU-J04-04 | 4 niveles semáforo simultáneos con heartbeat reactivo (timeStore) |
| 5 | CU-J04-05 | Facetas con contadores + búsqueda debounce 500ms + empty state gamificado |
| 6 | CU-J04-06 | Claim tarea: POST /claim → 200 → toast green → redirección formulario |
| 7 | CU-J04-07 | iForm Maestro: Mega-DTO BFF carga en ≤2s, 16 componentes |
| 8 | CU-J04-08 | Autoguardado: llenar 5 campos → cerrar browser → reabrir → banner restaurar → datos presentes |
| 9 | CU-J04-09 | Upload evidencia: .jpg 3MB → barra progreso → thumbnail → segundo archivo |
| 10 | CU-J04-10 | Completar tarea: Zod client + server → POST /complete → 200 → Camunda avanza |
| 11 | CU-J04-11 | RYOW: tarea desaparece del Workdesk en ≤1s tras completar |
| 12 | CU-J04-12 | Panel métricas DESPUÉS: Total = N-1, verificar diferencia exacta |

**Login para F1-F2:** `analista_n1` con break-glass toggle:
```typescript
await page.goto('/login')
await page.click('[data-testid="break-glass-toggle"]')
await page.fill('[data-testid="email-input"]', 'analista_n1@tenant-alpha.com')
await page.fill('[data-testid="password-input"]', 'Test123!')
await page.click('[data-testid="login-submit"]')
```

---

### T-11: Specs F3 — Multi-Instance 2 Browsers (🟠 P1)

**Archivo:** `frontend/e2e/j04-f3-multi-instance.e2e.spec.ts`

**Escenarios (5 tests):**

| Test | CU | Validación |
|------|:--:|------------|
| 1 | CU-J04-13 | Perito A login en browser 1 → Workdesk → reclama su tarea MI |
| 2 | CU-J04-14 | Perito B login en browser 2 → Workdesk → reclama su tarea MI |
| 3 | CU-J04-15 | Ghost Deletion: Perito B claim tarea grupal → tarea desaparece en browser 1 (WebSocket) con animación slide-out |
| 4 | CU-J04-16 | Ambos completan evaluaciones simultáneamente → MI converge |
| 5 | CU-J04-17 | Concurrencia atómica: 2 claims simultáneos vía API → solo 1 gana (200 vs 409) |

**Técnica 2 browsers:**
```typescript
const browser1 = await chromium.launch()
const context1 = await browser1.newContext()
const page1 = await context1.newPage()
// Login perito_a en page1

const browser2 = await chromium.launch()
const context2 = await browser2.newContext()
const page2 = await context2.newPage()
// Login perito_b en page2
```

---

### T-12: Specs F4-F6 — Delegación + Force Route + Skipeo (🟠 P1)

**Archivo:** `frontend/e2e/j04-f4-f6-delegacion-skipeo.e2e.spec.ts`

**Escenarios (9 tests):**

| Test | CU | Actor | Validación |
|------|:--:|:-----:|------------|
| 1 | CU-J04-20 | Director | Toggle delegación → banner amber → tareas del Analista N1 visibles |
| 2 | CU-J04-21 | Director | En modo delegación, puede ver detalles de tarea del asistente |
| 3 | CU-J04-22 | Director | "Volver a mis tareas" → banner desaparece → tareas propias (Directors) |
| 4 | CU-J04-23 | Admin | Activa forceRouting → Analista ve pantalla forzada (botón 🚀) |
| 5 | CU-J04-24 | Analista | "Atender Siguiente" → claim atómico por SLA → formulario abierto |
| 6 | CU-J04-25 | Analista | Skipeo motivo 1: "Cliente no responde" → audit trail inmutable |
| 7 | CU-J04-26 | Analista | Skipeo motivo 2: "Requiere documentación" → audit registrado |
| 8 | CU-J04-27 | Analista | Skipeo motivo 3: "Fuera de mi área" → nueva tarea asignada |
| 9 | CU-J04-28 | Analista | Skipeo motivo 4: "Otro" → validación ≥10 chars → botón disabled si corto |

---

### T-13: Specs F7 — Kanban Board (🟠 P1)

**Archivo:** `frontend/e2e/j04-f7-kanban.e2e.spec.ts`

**Escenarios (4 tests):**

| Test | CU | Validación |
|------|:--:|------------|
| 1 | CU-J04-29 | Navegación al Kanban → columnas con ≥3 tareas en TODO |
| 2 | CU-J04-30 | Flujo completo: TODO → IN_PROGRESS → BLOCKED (modal motivo) → IN_PROGRESS → DONE |
| 3 | CU-J04-31 | Happy path directo: TODO → IN_PROGRESS → DONE (2 movimientos, sync OK) |
| 4 | CU-J04-32 | Formulario Genérico: abrir tarea sin formulario → `sys_generic_form` → Aprobar |

**Drag & Drop en Playwright:**
```typescript
const taskCard = page.locator('[data-testid="kanban-card-{id}"]')
const targetColumn = page.locator('[data-testid="kanban-column-IN_PROGRESS"]')
await taskCard.dragTo(targetColumn)
await expect(page.locator('[data-testid="kanban-sync-status"]')).toHaveText('OK')
```

---

### T-14: Specs F8-F12 + Negativos (🟠 P1)

**Archivo:** `frontend/e2e/j04-f8-f12-negativos.e2e.spec.ts`

**Escenarios (14 tests):**

| Test | CU | Validación |
|------|:--:|------------|
| 1 | CU-J04-35 | Degradación: `docker stop camunda` → banner amber + CQRS 🔴 OFFLINE |
| 2 | CU-J04-36 | Kanban sigue operando durante degradación Camunda |
| 3 | CU-J04-37 | Reiniciar Camunda → banner desaparece → CQRS 🟢 ONLINE |
| 4 | CU-J04-38 | Inactividad 5+ min → auto-refresco silencioso al volver |
| 5 | CU-J04-39 | Director: reclama y completa Firma Final (Sub-Process) |
| 6 | CU-J04-41 | Observabilidad: GET /history/task → tareas completadas con timestamps |
| 7 | CU-J04-42 | Audit trail skipeos: 4 registros verificables |
| 8 | NEG-01 | Formulario vacío → Zod client bloquea → borde rojo + mensaje |
| 9 | NEG-02 | Timeout red → "No se pudo enviar" → borrador en LocalStorage |
| 10 | NEG-03 | Upload >50MB → validación client → "Excede límite" |
| 11 | NEG-04 | Delegación IDOR: Perito intenta ver Director → 403 → "Sin permisos" |
| 12 | NEG-05 | Skipeo sin motivo → botón disabled |
| 13 | NEG-06 | Kanban bloqueo sin motivo → botón disabled |
| 14 | NEG-07 | Usuario sin rol → router guard → 404 genérico (no 403) |

> [!IMPORTANT]
> **CU-J04-40 (F11 CQRS) = SKIP.** US-017 no implementada. La tabla `form_event_store` no existe. Documentar como SKIP con justificación `D-01`.

---

### T-15: Cierre y Documentación (🟠 P1)

1. **Actualizar** `.agentic-sync/coverage_matrix.md`:
   - US-008 de ~10% → porcentaje real post-Kanban MVP
   - Nuevos QA checks para US-001, US-002, US-008, US-029

2. **Generar** `docs/sprints/cierre_iteracion_s6_2.md` con:
   - Tabla de resultados E2E por spec file
   - Veredicto: PASS / PASS CON OBSERVACIONES / FAIL
   - Brechas descubiertas con severidad
   - Recomendaciones para It. 6.3

3. **Generar** `docs/qa/coverage_matrix.md` (copia sincronizada)

---

## 5. Convención de Commit

```
test(qa): T-9 re-run B3-B5 con data seed — resultados empíricos
test(qa): T-10 J-04 F1-F2 bandeja + claim + ejecución — 12 specs
test(qa): T-11 J-04 F3 multi-instance 2 browsers — 5 specs
test(qa): T-12 J-04 F4-F6 delegación + force route + skipeo — 9 specs
test(qa): T-13 J-04 F7 kanban D&D + block + GenForm — 4 specs
test(qa): T-14 J-04 F8-F12 + NEG degradación + negativos — 14 specs
docs(qa): T-15 cierre iteración 6.2 + coverage matrix update
```

---

## 6. Resumen Cuantitativo

| Métrica | Valor |
|---------|-------|
| **Total specs a crear** | 12 archivos |
| **Total tests** | ~44 (excl. CU-40 SKIP) |
| **Usuarios E2E** | 4 (analista_n1, perito_a, perito_b, director_1) |
| **Navegadores simultáneos** | 2 (F3) |
| **Fases cubiertas** | 11 de 12 (F11 CQRS = SKIP) |
| **Negativos** | 7 |
| **Criterio mínimo** | ≥30/44 PASS (≥68%) para veredicto APROBADO |
