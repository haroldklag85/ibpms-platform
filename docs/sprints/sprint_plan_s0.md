# Sprint 0 — Infraestructura Agéntica + E2E

> **Sprint:** 0 (Fundación)  
> **Duración:** 4 días laborales  
> **Objetivo:** Establecer la infraestructura de comunicación inter-agente, el framework E2E y los Casos de Uso UAT para habilitar la ejecución Dual Track a partir del Sprint 1.  
> **Fecha de inicio:** 2026-04-11  
> **Propuesta:** C — Dual Track con Orquestación Multi-Agente (ACP)

---

## Modelo de Roles del Equipo

| Rol | Actor | Scope en Sprint 0 |
|-----|-------|--------------------|
| **Jefe de Equipo** | Harolt (humano) | Valida UAT cases, confirma prioridad US Sprint 1, firma Gate Final |
| **Agente PO** | Agente IA | Crea Casos de Uso UAT para J-02 y J-04, complementa CAs Gherkin faltantes |
| **Arquitecto Líder SW** | Agente IA Lead | Diseña ACP, configura Playwright, escribe smoke test, crea handoff contracts |
| **Agente Backend** | — | No participa en Sprint 0 |
| **Agente Frontend** | — | No participa en Sprint 0 |
| **Agente QA** | — | No participa en Sprint 0 |

---

## Día 0 — Pre-Sprint: ACP Protocol Design

### Objetivo del día
Formalizar el protocolo de comunicación inter-agente adaptado al ecosistema iBPMS.

### ★ Arquitecto Lead

#### T-S0-01: Protocolo ACP iBPMS (evolución del Gatekeeper Pattern V2)

**Decisión Arquitectónica:** Nuestro ACP **NO** usará el protocolo stdio/NDJSON de OpenClaw (demasiado acoplado a Gateway). En su lugar, evolucionamos el **Gatekeeper Pattern V2** ya existente en `.agentic-sync/` hacia un protocolo basado en **handoffs Markdown + Git Stash + Coverage Matrix**.

**Esquema de comunicación:**

```
┌───────────────────────────────────────────────────────────────────┐
│                    CANAL ACP — iBPMS                              │
│                                                                   │
│  Medio:     .agentic-sync/ (directorio Git-tracked)              │
│  Formato:   Markdown estructurado (handoff_{rol}_{US}_{CAs}.md)  │
│  Persistencia: Commits atómicos por el Arquitecto Lead           │
│  Estado:    coverage_matrix.md (Single Source of Truth)           │
│                                                                   │
│  REGLA CARDINAL: Solo el Arquitecto Lead ejecuta `git commit`.   │
│  Los agentes ejecutores usan `git stash save "temp-{agent}"`.    │
└───────────────────────────────────────────────────────────────────┘
```

**Tipos de mensaje ACP:**

| Tipo | Archivo | Emisor → Receptor | Contenido |
|------|---------|-------------------|-----------|
| **Handoff de Tarea** | `handoff_backend_USXXX_CAYYY.md` | Lead → Backend Agent | Contratos API, dominio, tests esperados |
| **Handoff de Tarea** | `handoff_frontend_USXXX_CAYYY.md` | Lead → Frontend Agent | Stores, vistas, Zod schemas, RBAC |
| **Handoff de Validación** | `handoff_qa_USXXX.md` | Lead → QA Agent | Qué validar, scripts E2E esperados |
| **Handoff PO** | `handoff_po_USXXX.md` | Lead → Agente PO | US a refinar, CAs faltantes, UAT pendientes |
| **Informe de Entrega** | `delivery_{agent}_USXXX.md` | Agent → Lead | Qué se implementó, archivos modificados, stash ref |
| **Auditoría** | `audit_USXXX_iteration{N}.md` | Lead → All | Veredicto técnico, defectos, remediación |
| **Sprint Report** | `sprint_report_s{N}.md` | Lead → Jefe + PO | Informe consolidado del sprint |

#### T-S0-02: Template de Handoff Estructurado

Cada handoff debe seguir este esquema mínimo:

```markdown
# Handoff: {Backend|Frontend|QA|PO} — US-{XXX} CA-{YYY}

## Contexto
- Sprint: S{N}
- Journey: J-{XX}
- Dependencias: US-{AAA} (debe estar completa antes)

## Alcance Exacto
- CA-{YYY}: [Texto del criterio de aceptación]
- CA-{ZZZ}: [Texto del criterio de aceptación]

## Especificación Técnica
### Archivos a crear/modificar
| Archivo | Acción | Descripción |
|---------|--------|-------------|
| `path/to/file.java` | CREAR | Nuevo endpoint REST |
| `path/to/file.vue` | MODIFICAR | Agregar componente de filtro |

### Contratos API (si aplica)
- Endpoint: `GET /api/v1/...`
- Request: `{schema}`
- Response: `{schema}`

### Tests Esperados
- [ ] Test unitario para...
- [ ] Test de integración para...

## Criterio de Aceptación para Auditoría
El Arquitecto Lead verificará:
1. ¿Sigue la arquitectura hexagonal?
2. ¿Los contratos API coinciden con esta especificación?
3. ¿No hay violación de RBAC?

## Entrega
El agente ejecutor debe:
1. Implementar SOLO lo especificado aquí
2. Ejecutar `git stash save "temp-{agent}-US{XXX}-CA{YYY}"`
3. Reportar en `delivery_{agent}_US{XXX}.md`
```

#### T-S0-03: Sprint Plan Template (JSON)

```json
{
  "sprint": 1,
  "duration_days": 4,
  "us_in_scope": [
    {
      "us_id": "US-001",
      "cas_target": ["CA-22", "CA-29", "CA-30"],
      "journey": "J-04",
      "priority": "MUST",
      "track": "BUILD",
      "execution_order": 1,
      "handoffs": {
        "backend": "handoff_backend_US001_CA22_CA29_CA30.md",
        "frontend": "handoff_frontend_US001_CA22_CA29_CA30.md",
        "qa": "handoff_qa_US001.md"
      },
      "dependencies": ["US-036 (RBAC configurado)"]
    }
  ],
  "validation_targets": [
    {
      "us_id": "US-005",
      "track": "VALIDATE",
      "qa_handoff": "handoff_qa_US005.md"
    }
  ],
  "gate": {
    "technical": "≥90% CAs cubiertos",
    "functional": "UAT cases J-04 passing",
    "final": "Jefe de Equipo aprueba demo"
  }
}
```

---

## Día 1 — Infraestructura Docker + Playwright

### ★ Arquitecto Lead

#### T-S0-04: Validación Docker

Verificar que el stack completo levanta correctamente:

```bash
docker compose up -d
# Esperar healthchecks
docker compose ps  # Todos deben estar "healthy"
```

**Checklist:**

| Servicio | Puerto | Healthcheck | Estado |
|----------|--------|-------------|--------|
| ibpms-postgres (PgVector) | 5432 | `pg_isready` | ⬜ |
| ibpms-rabbitmq | 5672/15672 | `rabbitmq-diagnostics ping` | ⬜ |
| ibpms-redis | 6379 | `redis-cli ping` | ⬜ |
| ibpms-core (Spring Boot) | 8080 | HTTP 200 en `/actuator/health` | ⬜ |

#### T-S0-05: Instalación Playwright

```bash
cd frontend
npm install -D @playwright/test
npx playwright install chromium
```

**Estructura de tests propuesta:**

```
frontend/
├── e2e/
│   ├── playwright.config.ts
│   ├── fixtures/
│   │   └── auth.setup.ts        # Login fixture reutilizable
│   ├── smoke/
│   │   └── app-loads.spec.ts    # Smoke: la app carga sin errores
│   ├── j04/                     # Tests del Journey J-04
│   │   ├── workdesk-list.spec.ts
│   │   ├── task-claim.spec.ts
│   │   └── form-submit.spec.ts
│   └── j02/                     # Tests del Journey J-02
│       ├── bpmn-modeler.spec.ts
│       └── form-builder.spec.ts
```

#### T-S0-06: Smoke Test E2E

```typescript
// e2e/smoke/app-loads.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Smoke Test — iBPMS App', () => {
  test('la aplicación carga sin errores de consola', async ({ page }) => {
    const consoleErrors: string[] = [];
    page.on('console', msg => {
      if (msg.type() === 'error') consoleErrors.push(msg.text());
    });

    await page.goto('http://localhost:5173');
    await page.waitForLoadState('networkidle');

    // La app debe mostrar al menos el contenedor principal
    await expect(page.locator('#app')).toBeVisible();

    // No debe haber errores de consola críticos
    const criticalErrors = consoleErrors.filter(
      e => !e.includes('favicon') && !e.includes('DevTools')
    );
    expect(criticalErrors).toHaveLength(0);
  });

  test('el backend responde en /actuator/health', async ({ request }) => {
    const response = await request.get('http://localhost:8080/actuator/health');
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.status).toBe('UP');
  });
});
```

---

## Día 2 — Casos de Uso UAT

### 📋 Agente PO

#### T-S0-07: Casos de Uso UAT para J-04

Crear `docs/uat/casos_uso_uat_j04.md` con los escenarios step-by-step para:

> **J-04: Recepción de Tarea → Ejecución de Formulario → Completar → Persistencia CQRS**

**Escenarios requeridos** (basados en `e2e_journey_inventory.md` pasos 1-12):

| ID | Escenario | US/CA | Tipo |
|----|-----------|-------|------|
| UAT-J04-01 | Operario ve lista de tareas ordenadas por SLA | US-001 CA-01 | Happy Path |
| UAT-J04-02 | Semáforo SLA muestra amarillo para tarea urgente | US-001 CA-05 | Happy Path |
| UAT-J04-03 | Operario reclama tarea de cola grupal | US-002 CA-01 | Happy Path |
| UAT-J04-04 | WebSocket elimina tarea reclamada de compañeros | US-002 CA-12 | Happy Path |
| UAT-J04-05 | BFF carga Mega-DTO al abrir detalle | US-029 CA-05/CA-10 | Happy Path |
| UAT-J04-06 | Autoguardado en LocalStorage durante llenado | US-029 CA-03/CA-11 | Happy Path |
| UAT-J04-07 | Upload-First pattern para archivos adjuntos | US-029 CA-09 | Happy Path |
| UAT-J04-08 | Envío de formulario con validación Zod isomórfica | US-029 CA-01/CA-02 | Happy Path |
| UAT-J04-09 | CQRS persiste evento inmutable | US-017 | Happy Path |
| UAT-J04-10 | Tarea desaparece del Workdesk tras completar (RYOW) | US-029 CA-17 | Happy Path |
| UAT-J04-11 | Envío con campos obligatorios vacíos → error | US-029 CA-02 | Negative |
| UAT-J04-12 | Rate limiting: envío masivo bloqueado | US-001 CA-30 | Negative |

#### T-S0-08: Casos de Uso UAT para J-02

Crear `docs/uat/casos_uso_uat_j02.md` con los escenarios step-by-step para:

> **J-02: Diseñar Proceso BPMN → Crear Formulario → Desplegar → Ejecutar Primera Tarea**

**Escenarios requeridos** (basados en `e2e_journey_inventory.md` pasos 1-10):

| ID | Escenario | US/CA | Tipo |
|----|-----------|-------|------|
| UAT-J02-01 | Arquitecto BPM modela proceso BPMN en lienzo | US-005 | Happy Path |
| UAT-J02-02 | Configuración de lanes, gateways y user tasks | US-005 | Happy Path |
| UAT-J02-03 | Creación de formulario iForm vinculado al user task | US-003 | Happy Path |
| UAT-J02-04 | Definición de esquema Zod y layout Vue | US-003 | Happy Path |
| UAT-J02-05 | Despliegue de proceso con versión semántica | US-005 | Happy Path |
| UAT-J02-06 | Inicio de instancia del proceso | US-004/US-024 | Happy Path |
| UAT-J02-07 | Operario ve primera tarea en Workdesk | US-001 | Happy Path |
| UAT-J02-08 | Reclamo y apertura de formulario | US-002/US-029 | Happy Path |
| UAT-J02-09 | Llenado y envío del formulario | US-029 | Happy Path |
| UAT-J02-10 | CQRS persiste y avanza flujo BPMN | US-017 | Happy Path |
| UAT-J02-11 | Despliegue con BPMN inválido → error claro | US-005 | Negative |
| UAT-J02-12 | Formulario sin esquema Zod → error de validación | US-003 | Negative |

### ★ Arquitecto Lead

#### T-S0-09: E2E Test Plan

Crear `docs/sprints/e2e_test_plan.md` que mapee cada test Playwright a un gate de sprint:

| Sprint | Gate | Tests Requeridos | Journey |
|--------|------|------------------|---------|
| S0 | Smoke | `app-loads.spec.ts` | — |
| S1 | J-04 | `workdesk-list.spec.ts`, `task-claim.spec.ts`, `form-submit.spec.ts` | J-04 |
| S2 | J-02 | `bpmn-modeler.spec.ts`, `form-builder.spec.ts` | J-02 |
| S3 | J-03 | `rbac-access.spec.ts`, `gaslighting-404.spec.ts` | J-03 |

---

## Día 3 — Gate de Sprint 0

### ★ Arquitecto Lead — Gate Técnico

**Criterios de aprobación:**

| # | Criterio | Evidencia | Estado |
|---|----------|-----------|--------|
| 1 | ACP Protocol documentado | `sprint_plan_s0.md` + handoff templates en `.agentic-sync/` | ⬜ |
| 2 | Handoff contracts definidos | Templates Backend/Frontend/QA/PO listos | ⬜ |
| 3 | Sprint plan template funcional | `sprint_plan_template.json` validado | ⬜ |
| 4 | Docker stack healthy | 4/4 servicios levantados | ⬜ |
| 5 | Playwright instalado | `npx playwright test --reporter=list` ejecuta | ⬜ |
| 6 | Smoke test verde | `app-loads.spec.ts` pasa | ⬜ |

### 📋 Agente PO — Gate Funcional

| # | Criterio | Evidencia | Estado |
|---|----------|-----------|--------|
| 1 | UAT J-04 documentado | `casos_uso_uat_j04.md` con ≥10 escenarios | ⬜ |
| 2 | UAT J-02 documentado | `casos_uso_uat_j02.md` con ≥10 escenarios | ⬜ |
| 3 | CAs Gherkin completos para Sprint 1 | US-001, US-002, US-029 sin gaps | ⬜ |

### 👤 Jefe de Equipo — Gate Final

| # | Criterio | Evidencia | Estado |
|---|----------|-----------|--------|
| 1 | UAT cases cubren flujo real | Revisión de `casos_uso_uat_j04.md` | ⬜ |
| 2 | Prioridad de US confirmada | Lista ordenada para Sprint 1 | ⬜ |
| 3 | Equipo listo para Sprint 1 | Roles asignados, handoffs claros | ⬜ |

---

## Definición de Listo (Sprint 1 puede empezar cuando)

```
✅ Gate Técnico:   ACP + Docker + Playwright + Smoke ← Lead firma
✅ Gate Funcional: UAT J-04 + UAT J-02 + CAs completos ← Agente PO firma
✅ Gate Final:     UAT validado + US priorizadas + Equipo listo ← Jefe firma
```

---

## Backlog Priorizado para Sprint 1 (Propuesta)

> Pendiente confirmación del Jefe de Equipo en el Gate Final del Sprint 0.

| # | US | CAs Target | Journey | Track |
|---|-----|-----------|---------|-------|
| 1 | US-001 | CA-22, CA-29, CA-30 (restantes) | J-04 | BUILD |
| 2 | US-002 | CA-01 a CA-12 | J-04 | BUILD |
| 3 | US-029 | CA-01 a CA-17 | J-04 | BUILD |
| 4 | US-005 | Validación funcional | J-02 | VALIDATE |
| 5 | US-003 | Validación funcional | J-02 | VALIDATE |
| 6 | US-036 | Validación funcional | J-03 | VALIDATE |

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-10 | Creación del Sprint 0 plan | Arquitecto Lead |
