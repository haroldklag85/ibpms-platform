# 🧪 Handoff QA — Sprint 6 / Iteración 6.1

> **Iteración:** Sprint 6 — Iteración 6.1 (Verificación P0 + Smoke Test + Infraestructura E2E)  
> **Rama de trabajo:** `sprint-6/uat-certification` (debe existir, creada por Backend)  
> **US objetivo:** US-004 (Webhook), US-007 (DMN IDOR), US-027 (Copilot IDOR), US-001/US-002/US-029 (Smoke J-04)  
> **Flujo:** Backend → Frontend → **QA** (TÚ — último eslabón)  
> **SSOT de referencia:** `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/`  
> **Estrategia QA:** Seleccionada por `router_certificacion_qa.md` → **Modo Cazador (pruebasUatE2e)** para smoke + **Certificación Exclusiva Backend (REST Assured)** para verificaciones P0  
> **Autor:** Arquitecto Líder SW  
> **Fecha:** 2026-04-19

---

## 1. Metadatos y SSOT

| Parámetro | Valor |
|-----------|-------|
| **Sprint** | 6 — Iteración 6.1 |
| **Rama Git** | `sprint-6/uat-certification` |
| **US** | US-004, US-007, US-027 (Verificación P0), US-001/002/029 (Smoke J-04), US-005/008 (B-20/Kanban) |
| **Bloques de trabajo** | B1: Verificación Docker Compose, B2: Fixtures E2E, B3: Specs P0 (IDOR + Webhook), B4: Smoke J-04, B5: Cobertura B-20/Kanban |
| **Dependencia:** | ✅ Backend Y Frontend deben haber pusheado todos sus cambios antes de que QA comience |
| **Exclusiones** | US-017 CQRS: todos los escenarios J-08 CU-01 a CU-08 y CU-20 a CU-22 se marcan como `⏭️ SKIP — US-017 no desarrollada (deuda V2)` |

**Fuentes de verdad:**
- `docs/uat/casos_uso_uat_j_sec.md` → CU-JSEC-01, CU-JSEC-02, CU-JSEC-17 (verificación IDOR + Webhook)
- `docs/uat/casos_uso_uat_j04.md` → CU-J04-01 a CU-J04-06 (smoke Operario)
- `docs/sprints/sprint_plan_s6.md` → Plan completo del Sprint
- `.agentic-sync/coverage_matrix.md` → Matriz de cobertura actual

**Decisión del Router QA (`router_certificacion_qa.md`):**
- B3 (Verificaciones P0): → **Certificación Exclusiva Backend** (APIs sin interfaz gráfica). Las verificaciones IDOR son REST puro: enviar requests HTTP autenticados como un tenant e intentar acceder a recursos de otro tenant.
- B4 (Smoke J-04): → **UAT Táctico Empírico (Modo Cazador)** con `pruebasUatE2e` — validación de las 4 capas (UX, Red, Backend, Seguridad) contra backend Docker levantado.
- B5 (B-20 + Kanban): → **Automatización SDET (Playwright)** con `pruebasUatVisiblesAutomatizadas` — crear specs `.e2e.spec.ts` para regresión permanente.

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto en QA |
|-----|---------------|
| `adr_010_testing_pyramid_governance.md` | QA debe ejecutar la pirámide completa: Unit (Vitest) + Integration (JUnit) + E2E (Playwright). No se acepta "pass" sin ejecución empírica. |
| `adr-001-hexagonal-architecture.md` | Verificar que los hotfixes no introdujeron lógica de negocio en controllers (solo delegación a UseCase). |
| `adr_009_postgresql_pgvector_migration.md` | Seed data debe ser compatible con el schema Liquibase actual. |

**Principio Zero-Trust Testing:**
- PROHIBIDO reportar "pass" sin ejecución real contra backend Docker.
- PROHIBIDO usar `page.route()` (interceptores HTTP) en los specs de certificación E2E.
- Todo test E2E debe evidenciar tráfico de red real (F12 Network) contra endpoints backend reales.

---

## 3. Rutas Exactas y Contexto Preexistente

### B1: Verificación Docker Compose
- **Archivo creado por Backend:** `docker-compose.e2e.yml` (raíz del proyecto)
- **Servicios a verificar:** PostgreSQL (5433), Redis (6380), Camunda (8085), RabbitMQ (5673/15673)
- **Backend app:** Debe arrancar en puerto 8081 (profile `e2e`)
- **Healthcheck endpoints:**
  - Backend: `http://localhost:8081/actuator/health`
  - Camunda: `http://localhost:8085/engine-rest/version`
  - RabbitMQ: `http://localhost:15673/api/overview` (guest:guest)

### B2: Fixtures E2E
- **Seed data creado por Backend:** `backend/ibpms-core/src/main/resources/db/seed/e2e_seed.sql`
- **Usuarios E2E:** Deben coincidir EXACTAMENTE con los insertados en el seed SQL
- **Archivo a crear:** `frontend/e2e/fixtures/e2e-data.ts`

### B3: Specs de Verificación P0
- **Directorio specs E2E:** `frontend/e2e/certification/` (nuevo directorio)
- **Config Playwright:** `frontend/playwright.e2e.config.ts` (creado por Frontend)
- **Condición:** Estos specs NO usan `page.route()` — tráfico real HTTP

### B4: Smoke Test J-04
- **Documento UAT:** `docs/uat/casos_uso_uat_j04.md`
- **Flujo:** Login → Workdesk → Claim → Form → Submit → Verificar desaparición

### B5: Verificación B-20 + Kanban
- **B-20:** Verificar que el dropdown DMN aparece en BpmnDesigner al seleccionar BusinessRuleTask
- **Kanban:** Verificar que KanbanView carga datos desde API real (no mocks)

---

## 4. Snippets Prescriptivos

### B2: Fixtures E2E (`frontend/e2e/fixtures/e2e-data.ts`)

```typescript
export const TENANTS = {
  ALPHA: { id: 'tenant_alpha', name: 'Alpha Corp', domain: 'alpha.com' },
  BETA: { id: 'tenant_beta', name: 'Beta Inc', domain: 'beta.com' },
} as const;

export const USERS = {
  ADMIN_ALPHA: { 
    email: 'admin@alpha.com', 
    password: 'Test1234!', 
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_SUPER_ADMIN'] 
  },
  OPERARIO_ALPHA: { 
    email: 'operario@alpha.com', 
    password: 'Test1234!', 
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_OPERARIO'] 
  },
  ARQUITECTO_ALPHA: { 
    email: 'arquitecto@alpha.com', 
    password: 'Test1234!', 
    tenant: TENANTS.ALPHA.id,
    roles: ['ROLE_PROCESS_ARCHITECT'] 
  },
  ADMIN_BETA: { 
    email: 'admin@beta.com', 
    password: 'Test1234!', 
    tenant: TENANTS.BETA.id,
    roles: ['ROLE_SUPER_ADMIN'] 
  },
  OPERARIO_BETA: { 
    email: 'operario@beta.com', 
    password: 'Test1234!', 
    tenant: TENANTS.BETA.id,
    roles: ['ROLE_OPERARIO'] 
  },
} as const;

// Constantes de API (sin page.route, requests reales)
export const API = {
  BASE_URL: 'http://localhost:8081',
  WORKDESK: '/api/v1/workdesk/tasks',
  CLAIM: (taskId: string) => `/api/v1/tasks/${taskId}/claim`,
  DMN_DEFINITIONS: '/api/v1/dmn-models/definitions',
  KANBAN: (projectId: string) => `/api/v1/projects/${projectId}/kanban`,
  WEBHOOK_LEGACY: '/inbound/email-webhook',
  COPILOT_SESSION: '/api/v1/ai/copilot/session',
} as const;
```

### B3: Spec Verificación IDOR Copilot — P0 (`frontend/e2e/certification/idor-copilot.e2e.spec.ts`)

```typescript
import { test, expect } from '@playwright/test';
import { USERS, API } from '../fixtures/e2e-data';

test.describe('P0: IDOR BpmnCopilotController — Cross-Tenant Session Wipe', () => {
  
  test('CU-JSEC-02: Tenant Alpha NO puede destruir sesión de Tenant Beta', async ({ request }) => {
    // 1. Autenticar como Arquitecto de Tenant Alpha
    const loginRes = await request.post(`${API.BASE_URL}/api/v1/auth/login`, {
      data: { email: USERS.ARQUITECTO_ALPHA.email, password: USERS.ARQUITECTO_ALPHA.password }
    });
    const { token } = await loginRes.json();
    
    // 2. Intentar destruir sesión que pertenece a Tenant Beta
    const deleteRes = await request.delete(`${API.BASE_URL}${API.COPILOT_SESSION}?sessionId=session_beta_001`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    
    // 3. MUST be 403 Forbidden — NOT 200
    expect(deleteRes.status()).toBe(403);
  });

  test('CU-JSEC-02b: Tenant Alpha SÍ puede destruir su propia sesión', async ({ request }) => {
    const loginRes = await request.post(`${API.BASE_URL}/api/v1/auth/login`, {
      data: { email: USERS.ARQUITECTO_ALPHA.email, password: USERS.ARQUITECTO_ALPHA.password }
    });
    const { token } = await loginRes.json();
    
    const deleteRes = await request.delete(`${API.BASE_URL}${API.COPILOT_SESSION}?sessionId=session_alpha_001`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    
    expect(deleteRes.status()).toBe(200);
  });
});
```

### B3: Spec Verificación Webhook Legacy — P0 (`frontend/e2e/certification/webhook-legacy.e2e.spec.ts`)

```typescript
import { test, expect } from '@playwright/test';
import { API } from '../fixtures/e2e-data';

test.describe('P0: EmailWebhookController — Deprecated Legacy Endpoint', () => {
  
  test('CU-JSEC-17: POST /inbound/email-webhook retorna HTTP 410 Gone', async ({ request }) => {
    const res = await request.post(`${API.BASE_URL}${API.WEBHOOK_LEGACY}`, {
      headers: { 'ClientState': 'secreto-compartido-m365' },
      data: { subject: 'Test Email', body: 'Body', sender: 'test@domain.com' }
    });
    
    // MUST be 410 Gone (deprecated) — NOT 202 Accepted
    expect(res.status()).toBe(410);
    
    const body = await res.json();
    expect(body.error).toBe('ENDPOINT_DEPRECATED');
    expect(body.migration).toContain('/api/v1/intake/webhook');
  });

  test('CU-JSEC-17b: POST sin ClientState también retorna 410 (no 403)', async ({ request }) => {
    const res = await request.post(`${API.BASE_URL}${API.WEBHOOK_LEGACY}`, {
      data: { subject: 'Attack', body: '<script>alert(1)</script>', sender: 'hacker@evil.com' }
    });
    
    // Deprecado = todo request retorna 410, independientemente de autenticación
    expect(res.status()).toBe(410);
  });
});
```

### B4: Smoke Test J-04 (`frontend/e2e/certification/smoke-j04-operario.e2e.spec.ts`)

```typescript
import { test, expect } from '@playwright/test';
import { USERS } from '../fixtures/e2e-data';

test.describe('Smoke J-04: Operario MVP — Happy Path', () => {
  
  test('CU-J04-01→06: Login → Workdesk → Claim → Form → Submit → Desaparición', async ({ page }) => {
    // Timeout extendido para backend real
    test.setTimeout(90000);
    
    // 1. Login real
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.OPERARIO_ALPHA.email);
    await page.fill('[data-testid="password-input"]', USERS.OPERARIO_ALPHA.password);
    await page.click('[data-testid="login-submit"]');
    
    // 2. Navegar a Workdesk
    await page.waitForURL(/workdesk/);
    
    // 3. Verificar lista de tareas cargada (no vacía)
    const taskList = page.locator('[data-testid="task-list"] [data-testid^="task-row-"]');
    await expect(taskList.first()).toBeVisible({ timeout: 30000 });
    
    // 4. Reclamar primera tarea disponible
    const firstTask = taskList.first();
    const claimButton = firstTask.locator('[data-testid="claim-button"]');
    await claimButton.click();
    
    // 5. Esperar confirmación de claim (toast o estado visual)
    await expect(page.locator('.p-toast-message-success, [data-testid="claim-success"]')).toBeVisible({ timeout: 15000 });
    
    // 6. Abrir formulario de la tarea reclamada
    await firstTask.click();
    await expect(page.locator('[data-testid="form-container"]')).toBeVisible({ timeout: 15000 });
    
    // 7. Llenar campos obligatorios (genéricos)
    const requiredInputs = page.locator('input[required], textarea[required], select[required]');
    const count = await requiredInputs.count();
    for (let i = 0; i < count; i++) {
      const input = requiredInputs.nth(i);
      const tagName = await input.evaluate(el => el.tagName.toLowerCase());
      if (tagName === 'select') {
        await input.selectOption({ index: 1 });
      } else {
        await input.fill('Valor de prueba E2E');
      }
    }
    
    // 8. Enviar formulario
    await page.click('[data-testid="form-submit"]');
    
    // 9. Verificar toast de éxito
    await expect(page.locator('.p-toast-message-success')).toBeVisible({ timeout: 15000 });
    
    // 10. Verificar desaparición del Workdesk (RYOW)
    await page.goto('/workdesk');
    // La tarea reclamada y completada NO debe aparecer
  });
});
```

### B5: Spec B-20 DMN Dropdown (`frontend/e2e/certification/b20-dmn-dropdown.e2e.spec.ts`)

```typescript
import { test, expect } from '@playwright/test';
import { USERS } from '../fixtures/e2e-data';

test.describe('B-20: Dropdown DMN en BpmnDesigner', () => {
  
  test('Seleccionar BusinessRuleTask muestra dropdown DMN con tablas publicadas', async ({ page }) => {
    test.setTimeout(60000);
    
    // Login como Arquitecto
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', USERS.ARQUITECTO_ALPHA.email);
    await page.fill('[data-testid="password-input"]', USERS.ARQUITECTO_ALPHA.password);
    await page.click('[data-testid="login-submit"]');
    
    // Navegar al BPMN Designer
    await page.goto('/bpmn-designer');
    await page.waitForSelector('[data-testid="bpmn-canvas"]', { timeout: 20000 });
    
    // Agregar BusinessRuleTask al canvas (si existe palette)
    // Seleccionar el BusinessRuleTask existente
    await page.click('[data-testid="bpmn-canvas"] [data-type="bpmn:BusinessRuleTask"]');
    
    // Verificar que el sidebar muestra el dropdown DMN
    const dmnDropdown = page.locator('select:has(option:text("— Seleccionar tabla DMN —"))');
    await expect(dmnDropdown).toBeVisible({ timeout: 10000 });
    
    // Verificar que el dropdown tiene opciones (tablas DMN del seed)
    const options = dmnDropdown.locator('option');
    expect(await options.count()).toBeGreaterThan(1); // Al menos "— Seleccionar —" + 1 DMN
  });
});
```

---

## 5. Matriz de QA y Testing Atómico

| Test Name | Bloque | CU-UAT | 4 Capas | Aserción Esperada |
|-----------|:------:|:------:|:-------:|-------------------|
| `idor-copilot.e2e.spec.ts: cross-tenant` | B3 | CU-JSEC-02 | Red+Seg | HTTP 403 — tenant Alpha NO puede wipe session Beta |
| `idor-copilot.e2e.spec.ts: own-tenant` | B3 | CU-JSEC-02b | Red | HTTP 200 — tenant Alpha SÍ puede wipe propia sesión |
| `webhook-legacy.e2e.spec.ts: deprecated` | B3 | CU-JSEC-17 | Red+Seg | HTTP 410 Gone — body contiene `ENDPOINT_DEPRECATED` |
| `webhook-legacy.e2e.spec.ts: sin auth` | B3 | CU-JSEC-17b | Seg | HTTP 410 — deprecado independientemente de auth |
| `smoke-j04-operario.e2e.spec.ts: happy path` | B4 | CU-J04-01→06 | UX+Red+BE+Seg | Flujo completo Login→Claim→Form→Submit contra backend real |
| `b20-dmn-dropdown.e2e.spec.ts: dropdown visible` | B5 | B-20 | UX+Red | Dropdown DMN aparece con opciones al seleccionar BusinessRuleTask |

**Ley de Correspondencia Gherkin (§4 de `qa_e2e_validation_audit/SKILL.md`):**
- Todo CU-UAT testeado debe tener referencia trazable al SSOT (`docs/uat/casos_uso_uat_j_sec.md`, `docs/uat/casos_uso_uat_j04.md`).
- Si se detecta un test que valida comportamiento NO documentado en el SSOT, reportarlo como "Test Fantasma".
- Si se detecta un CU-UAT sin test E2E correspondiente, reportarlo como "Cobertura Faltante".

---

## 6. Mensaje de Despacho

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6/uat-certification`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS OBLIGATORIOS:**
> - Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` íntegramente — incluyendo §1 (Pirámide completa), §2 (Evidencia), §3 (Adjunción), §4 (Correspondencia Gherkin), §5 (Backend vivo obligatorio).
> - Aplica estrictamente el protocolo **TDD** de `.agents/skills/tdd_first/SKILL.md`.
> - Aplica las normativas **Clean Code** de `.agents/skills/clean_code_standards/SKILL.md`.

> **Verificación de backend vivo (§5):** Antes de ejecutar tests E2E:
> 1. `docker compose -f docker-compose.e2e.yml ps` → todos los servicios `Up (healthy)`
> 2. `curl http://localhost:8081/actuator/health` → `{"status":"UP"}`
> 3. Si backend no arranca tras 2 intentos, reportar bloqueo en `.agentic-sync/infra_blocker_20260419.md`.

> 📚 **WORKFLOWS DE GOBERNANZA OBLIGATORIOS:**
> - **Reconciliación de Cobertura** (`.agent/workflows/reconciliacionCoberturaCa.md`): Al finalizar, ejecutar las 4 fases de reconciliación cruzando SSOT → Handoffs → Commits → Matriz. Generar reporte de discrepancias.
> - **Router de Certificación** (`.agent/workflows/router_certificacion_qa.md`): Ya seleccionado — B3: "Certificación Exclusiva Backend", B4: "UAT Táctico Empírico", B5: "Automatización SDET".
> - **Cierre de Deuda Técnica** (`.agent/workflows/cierreDeudaTecCriteriosAceptacion.md`): Al cerrar, generar el artefacto `.agentic-sync/cierre_iteracion_s6_1.md` con el formato de Fase 6 (CAs ejecutados, ADRs validados, violaciones, métricas).

> **Ejecución por lotes (máximo 5 CAs por lote):**
> - Lote 1: B3 — Verificación P0 (3 specs: IDOR Copilot, IDOR DMN, Webhook Legacy)
> - Lote 2: B4 — Smoke J-04 (1 spec de flujo completo: Login→Claim→Form→Submit)
> - Lote 3: B5 — Verificación B-20 + Kanban (2 specs: DMN dropdown, KanbanView real)

> **Actualización de coverage_matrix.md:**
> Al finalizar, agregar columna `E2E Real S6` junto a la columna existente:
> - `✅` → Escenario pasó contra backend real
> - `❌` → Escenario falló (documentar BUG con severidad)
> - `⏭️ SKIP` → US-017 CQRS (deuda V2) — justificar en reporte
