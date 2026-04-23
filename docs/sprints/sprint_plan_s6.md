# Sprint 6 — Certificación E2E + UAT del MVP (Madurez 100%)

> **Objetivo:** Estabilizar y certificar las 18 US del MVP mediante pruebas E2E y UAT exhaustivas contra backend real, identificando, documentando y remediando todos los bugs y gaps residuales.  
> **Rama:** `sprint-6/uat-certification` (rama larga, merge a `main` al cierre)  
> **Modelo de Gobernanza:** Multi-Agente (Backend + Frontend + QA) con handoffs formales  
> **Enfoque de pruebas:** Journey crítico → barrido por US (mixto E2E + CA funcional)  
> **Infraestructura:** Docker Compose real (PostgreSQL + Redis + Camunda + RabbitMQ + ClamAV)  
> **Fecha de inicio:** 2026-04-19  
> **Sprint dinámico:** El scope puede crecer por hallazgos de bugs. Se documentan iteraciones internas (6.1, 6.2, ...).  
> **Autor:** Arquitecto Líder SW

---

## 1. Inventario MVP (18 US Operativas)

| # | US | Título | Épica | Estado Post-S5.1 |
|:-:|:--:|--------|:-----:|:-----------------:|
| 1 | US-000 | Resiliencia PII & Error Handling | A | ✅ Operativa |
| 2 | US-001 | Workdesk (Tareas Pendientes) | A | ✅ Operativa |
| 3 | US-002 | Claim Task (Reclamar Tarea) | A | ✅ Operativa |
| 4 | US-003 | iForm Maestro (Instanciar Formulario) | B | ✅ Operativa |
| 5 | US-004 | Webhook Intake (O365 Listener) | A | ✅ Operativa |
| 6 | US-005 | BPMN Modeler & Deploy | B | ✅ Operativa |
| 7 | US-007 | DMN Viewer | B | ✅ Operativa |
| 8 | US-008 | Kanban (Mover Tarjeta) | A | ⚠️ ~10% Scaffolding |
| 9 | US-017 | CQRS Event Sourcing | A | ❌ No Desarrollada |
| 10 | US-028 | Simulador Zod (QA Sandbox) | B | ✅ Operativa |
| 11 | US-029 | Ejecución y Envío de Formulario | B | ✅ Operativa |
| 12 | US-030 | Hub Ágil (Proyectos/Sprints/Kanban) | A | ✅ Operativa (~85%) |
| 13 | US-034 | RabbitMQ Orquestación | F | ✅ Operativa |
| 14 | US-036 | RBAC & EntraID | E | ✅ Operativa |
| 15 | US-038 | Multi-Rol & Sync EntraID | E | ✅ Operativa |
| 16 | US-039 | Formulario Genérico Base | B | ✅ Operativa |
| 17 | US-043 | SLA Global Config | E | ✅ Operativa |
| 18 | US-048 | Internal IdP | E | ✅ Operativa |

---

## 2. Journeys UAT Disponibles

| Journey | Título | US Involucradas | Escenarios | Criticidad |
|:-------:|--------|:---------------:|:----------:|:----------:|
| **J-02** | Primer Uso E2E — Forms → BPMN → Deploy → Ejecutar | US-003, US-005, US-028, US-039, US-029, US-017, US-001, US-002 | 32 | 🔴 ALTA |
| **J-03** | Intake Externo — Webhook O365 → Triaje | US-004, US-034, US-001, US-002 | 17 | 🔴 ALTA |
| **J-04** | Operario MVP — Tarea → Formulario → Completar → CQRS | US-001, US-002, US-029, US-017, US-000 | 13 | 🔴 ALTA |
| **J-05** | Gobernanza Administrativa — RBAC → Multi-Rol → IdP → SLA | US-048, US-036, US-038, US-025, US-043 | 24 | 🔴 ALTA |
| **J-06** | Hub Ágil — Planificación → Kanban → SLA → Cierre | US-030, US-008, US-043, US-001 | 27 | 🟠 ALTA |
| **J-07** | Arquitecto IA — DMN → BPMN → Copilot → Deploy | US-005, US-007, US-027, US-003, US-036 | 30 | 🟠 ALTA |
| **J-08** | Resiliencia — CQRS, Circuit Breaker, DLQ, Event Sourcing | US-000, US-034, US-017, US-029, US-001, US-004 | 26 | 🟠 ALTA |
| **J-SEC** | Pentest Transversal — IDOR, XSS, PII, Rate-Limit | US-000, US-036, US-038, US-007, US-027, US-002, US-004 | 25 | 🔴 CRÍTICA |
| — | US-001 Sprint1 | US-001 | Individual | 🔴 |
| — | US-002 Claiming | US-002 | Individual | 🔴 |
| — | US-029 Form Submit | US-029 | Individual | 🔴 |

**Total de escenarios UAT documentados: ~194+**

---

## 3. Brechas Críticas Pre-existentes (Inventario de Deuda a Resolver)

### 🔴 Prioridad P0 (Bloqueadores)

| # | Brecha | US | Journey | Acción |
|---|--------|:--:|:-------:|--------|
| B-01 | ~~IDOR tenantId hardcodeado en `DmnGovernanceController`~~ | US-007 | J-SEC | ✅ **CERRADO en S5.1** — Todos los endpoints ya usan `SecurityContextUtils.getTenantId()` |
| B-02 | IDOR tenantId hardcodeado en `BpmnCopilotController` | US-027 | J-SEC | Hotfix `SecurityContextUtils.getTenantId()` |
| B-03 | `EmailWebhookController` legacy bypasea pipeline de seguridad | US-004 | J-SEC | Deprecar o encadenar a `WebhookIntakeService` |
| B-16 | US-017 completamente sin desarrollar (Event Store, CQRS) | US-017 | J-08 | Excluir del scope E2E S6 — documentar como deuda V2 |
| B-17 | Tabla `form_event_store` no existe | US-017 | J-08 | Misma decisión que B-16 |

### 🟠 Prioridad P1 (Funcionales)

| # | Brecha | US | Journey | Acción |
|---|--------|:--:|:-------:|--------|
| B-06 | KanbanView.vue hardcodeado con mocks | US-008 | J-06 | Implementar state machine real + endpoint PATCH |
| B-04 | Seudonimización PII pre-LLM no evidenciada | US-007/027 | J-SEC | Implementar interceptor PII → LLM |
| B-18 | Tabla `task_drafts` no existe | US-017 | J-08 | Vincular con decisión B-16 |
| B-07 | Sin tabla `ibpms_time_logs` | US-008 | J-06 | Creación + componente `<UniversalSlaTimer>` |

### 🟡 Prioridad P2 (Polish)

| # | Brecha | US | Journey |
|---|--------|:--:|:-------:|
| B-08 | Badge "Inactivo X días" no implementado | US-030 | J-06 |
| B-09 | Alertas tempranas SLA (80%) | US-043 | J-06 |
| B-10 | `<UniversalSlaTimer>` como componente agnóstico | US-008 | J-06 |
| B-11 | Borradores DMN GC no implementado | US-007 | J-07 |
| B-12 | Seudonimización PII pre-LLM DMN | US-007 | J-07 |
| B-13 | Entity/DDL mismatch data mappings | US-005 | J-07 |
| B-14 | Contrato API /deploy incompleto | US-005 | J-07 |
| **B-20** | **Vinculación DMN↔BPMN no visual (decisionRef manual/invisible)** — formKey tiene dropdown, decisionRef no | US-005/007 | J-02, J-07 |

---

## 4. Estructura de Iteraciones (Journey Crítico → Expansión)

> **Nota:** Los documentos UAT (J-02 a J-SEC) están en refinamiento con el PO. Las iteraciones se describen de manera general. Cuando el PO complete el refinamiento, se actualizará el detalle de los casos de prueba ejecutables.

### Iteración 6.1 — Hotfixes P0 + Infraestructura E2E Real + Deuda Técnica DMN-BPMN

> **Estado:** ✅ **COMPLETADA — SELLADA OFICIALMENTE (2026-04-19)**
> **Commits:** `a388b7b9` (seal QA observations) → `ca20cdb5` (fix Login selectors + re-run)
> **Veredicto:** PASS CON OBSERVACIONES | **E2E:** 4/7 PASS (57%)
> **Cierre formal:** [`docs/sprints/cierre_iteracion_s6_1.md`](../sprints/cierre_iteracion_s6_1.md)

**Objetivo:** Parchear las 3 vulnerabilidades P0 activas, levantar la infraestructura Docker Compose para pruebas contra backend real, y cerrar la deuda técnica de usabilidad en la vinculación DMN↔BPMN para alinearla al principio low-code de la plataforma.

| Tarea | US | Agente | Tipo | Estado |
|-------|:--:|:------:|:----:|:------:|
| ~~Hotfix IDOR `DmnGovernanceController` (tenantId)~~ | US-007 | — | Remediación P0 | ✅ Cerrado en S5.1 |
| Hotfix IDOR `BpmnCopilotController` (tenantId) | US-027 | Backend+QA | Remediación P0 | ✅ Cerrado (Role prefix + tenant propagation + Anti-IDOR startsWith) |
| Deprecar `EmailWebhookController` → HTTP 410 Gone | US-004 | Backend+QA | Remediación P0 | ✅ Cerrado (E2E 2/2 PASS) |
| Configurar `docker-compose.e2e.yml` (PG + Redis + Camunda + RabbitMQ) | Infra | Backend | Infraestructura | ✅ Cerrado |
| Crear seed data y fixtures E2E (tenants, users, JWT mock) | Infra | QA | Infraestructura | ✅ Cerrado (`e2e-data.ts`) |
| Adaptar Playwright config para backend real (remover `page.route()` mocks) | Infra | QA | Infraestructura | ✅ Cerrado |
| **B-20: Dropdown visual DMN en BpmnDesigner (decisionRef)** | US-005/007 | Frontend | Deuda Técnica | ✅ Cerrado |
| **B-20: Endpoint `/api/v1/dmn/definitions` para catálogo DMN** | US-007 | Backend | Deuda Técnica | ✅ Cerrado |
| Login.vue `data-testid` para E2E | UI | Arquitecto | Remediación | ✅ Cerrado (break-glass-toggle + 3 inputs) |
| Limpieza `System.out.println` en SecurityContextUtils | Higiene | QA | Remediación | ✅ Cerrado |

#### Resultados E2E Empíricos (Playwright Chromium)

| Lote | Spec | Result | Causa FAIL |
|:----:|------|:------:|------------|
| B1 | `idor-copilot.e2e.spec.ts` (2 tests) | ✅ PASS | — |
| B2 | `webhook-legacy.e2e.spec.ts` (2 tests) | ✅ PASS | — |
| B3 | `smoke-j04-operario.e2e.spec.ts` (1 test) | ❌ FAIL | Timeout en `task-list` — BD sin seed data operacional |
| B4 | `b20-dmn-dropdown.e2e.spec.ts` (1 test) | ❌ FAIL | Timeout en `bpmn-canvas` — BD sin DMN definitions |
| B5 | `kanban-board.e2e.spec.ts` (1 test) | ❌ FAIL | Timeout en `kanban-card` — BD sin Kanban cards |

**Análisis:** Capa Security (API-level) = 100% ✅. Capa UI (Browser-level) = 0% ❌ por ausencia de data seed operacional en BD.

**Deuda residual transferida a Iteración 6.2:** Implementar SQL/API seed de datos operacionales para que los specs UI encuentren elementos visibles.

---

### Iteración 6.2 — Data Seed E2E + Journey J-04 v2 (Operario MVP Certificación Integral)

> **Documento refinado:** [`docs/uat/casos_uso_uat_j04.md`](../uat/casos_uso_uat_j04.md) (v2 — 2026-04-19)
> **Prerrequisito:** It. 6.1 SELLADA ✅ + Data Seed operacional

**Objetivo:** (1) Resolver el bloqueante P0 de data seed E2E que causó fallos en B3-B5 de It. 6.1. (2) Certificar el Journey completo del Operario MVP con 45 escenarios (vs 13 del plan original).

**Flujo expandido:** `Bandeja (SLA+Facetas+Métricas) → Claim → iForm (autoguardado+Zod) → Multi-Instance (2 navegadores) → Delegación → Force Routing → Skipeo ×4 → Kanban (D&D+Block+GenForm) → Degradación BPMN → Inactividad → Director Firma → CQRS → Observabilidad`

**US involucradas (9):** US-001, US-002, US-008, US-017, US-029, US-036, US-039, US-043, US-051

**Usuarios simultáneos:** 4 (Analista N1, Perito A, Perito B, Director) | **Navegadores:** 2 concurrentes

| Fase | Escenarios | US Testeadas | Capacidad Workdesk |
|:----:|:----------:|:------------:|:------------------:|
| F1: Bandeja Unificada | CU-J04-01 a 05 | US-001, US-043 | W-4 Facetas, W-5 SLA Vivo |
| F2: Claim + Ejecución | CU-J04-06 a 12 | US-002, US-029 | Autoguardado, RYOW, Zod |
| F3: Multi-Instance (2 browsers) | CU-J04-13 a 17 | US-002, US-029 | W-6 Ghost Deletion |
| F4: Delegación Escritorio | CU-J04-20 a 22 | US-001 | W-1 Delegación |
| F5: Enrutamiento Forzoso | CU-J04-23 a 24 | US-001 | W-2 Force Routing |
| F6: Skipeo Justificado (×4 motivos) | CU-J04-25 a 28 | US-002 | W-3 Skipeo |
| F7: Kanban Board (D&D+Block+GenForm) | CU-J04-29 a 32 | US-008, US-039 | Kanban completo |
| F8: Degradación BPMN | CU-J04-35 a 37 | US-001 | W-7 Degradación |
| F9: Inactividad + Auto-Refresco | CU-J04-38 | US-001 | Auto-refresh CA-31 |
| F10: Director Firma Final | CU-J04-39 | US-029 | Sub-Process |
| F11: CQRS Event Store | CU-J04-40 | US-017 | ❌ FALLA esperada |
| F12: Observabilidad | CU-J04-41 a 42 | US-001, US-002 | Audit Trail |
| **Negativos** | NEG-01 a NEG-07 | US-001/002/008/029/036 | Validación + RBAC |

**Total: 45 escenarios** (38 positivos + 7 negativos) — 7 capacidades Workdesk validadas

#### Brechas descubiertas en J-04 v2 (a resolver en esta iteración o documentar)

| # | Brecha | Severidad | US | Acción |
|---|--------|:---------:|:--:|--------|
| B-J04-01 | `form_event_store` no existe → CQRS FALLA | 🔴 P0 | US-017 | Documentar como SKIP (D-01) |
| B-J04-02 | Viewer de tarea es mock | 🟠 P1 | US-029 | Conectar BFF a datos reales |
| B-J04-03 | Delegación `assistantId` hardcoded | 🟠 P1 | US-001 | Implementar relación jerárquica |
| B-J04-04 | `forceRouting` toggle sin endpoint Admin | 🟡 P2 | US-001 | Implementar toggle API |
| B-J04-05 | WebSocket Ghost Deletion sin validación E2E | 🟡 P2 | US-002 | Test E2E con 2 browsers |
| B-J04-06 | Kanban `moveTask` sin validación transiciones | 🟡 P2 | US-008 | State machine backend |
| B-J04-07 | Skipeo `skipAndNext` sin endpoint backend | 🟠 P1 | US-002 | Implementar endpoint |

**Criterio de éxito:** ≥38/45 escenarios ejecutados contra backend real (excl. CU-J04-40 CQRS=SKIP). Brechas P1 remediadas. Bugs catalogados.

---

### Iteración 6.2_1 (Puente) — Cierre de Deuda Técnica (Jackson Recursion) & UI Workdesk

> **Estado:** ✅ **COMPLETADA — SELLADA OFICIALMENTE (2026-04-22)**
> **Veredicto:** PASS E2E Zero-Mock (US-017 CA-19 a CA-26) | BUG-S6-001 CERRADO
> **Cierre formal:** `.agentic-sync/cierre_iteracion_deudaTec_US017_CA19_CA26.md`

**Objetivo:** Planear y ejecutar el cierre de la deuda técnica diferida referente al desbordamiento de memoria por recursividad infinita de Jackson en las entidades de Identity Governance (`UserEntity` <-> `RoleEntity`), bloqueante para la estabilización total de los catálogos en UAT, y certificar la UI del Workdesk (ConnectionToast).

**Criterio de éxito:** Modificar las entidades JPA con anotaciones `@JsonIgnore` para aislar los grafos cíclicos. Certificar CA-19 a CA-26 en Zero-Mock E2E. ✅ **CUMPLIDO**.

---

### Iteración 6.3 — Journey J-02 (Primer Uso E2E — Diseñador)

**Objetivo:** Certificar el ciclo completo de diseño: Forms → BPMN → Deploy → Ejecución.

| Fase J-02 | Escenarios | US Testeadas |
|:---------:|:----------:|:------------:|
| Fase 1: Diseño Formulario | CU-01 a CU-05 | US-003 |
| Fase 2: Modelado BPMN + Vinculación | CU-06 a CU-09 | US-005, US-003 |
| Fase 3: Import/Export | CU-10 a CU-12 | US-028 |
| Fase 4: Formulario Genérico | CU-13 a CU-15 | US-039 |
| Fase 5: Deploy + Pre-Flight | CU-16 a CU-17 | US-005 |
| Fase 6: Primera Instancia + Ejecución | CU-18 a CU-21 | US-001, US-002, US-029 |
| Fase 7: CQRS (Excluida — US-017 no desarrollada) | CU-22 | — |
| Fase 8: Observabilidad | CU-23 a CU-25 | US-001, US-005 |
| Negativos | NEG-01 a NEG-07 | Múltiples |

**Criterio de éxito:** 31 escenarios ejecutados (excluyendo CU-22 CQRS). Brechas documentadas.

---

### Iteración 6.4 — Journey J-03 (Intake Webhook) + J-SEC (Pentest)

**Objetivo:** Certificar la puerta de entrada externa (webhooks) y ejecutar el pentest transversal de seguridad.

| Journey | Escenarios | US Testeadas |
|:-------:|:----------:|:------------:|
| J-03 Bloque 1: Recepción y Seguridad Perimetral | CU-01 a CU-05 | US-004 |
| J-03 Bloque 2: Encolamiento RabbitMQ | CU-06 a CU-08 | US-034 |
| J-03 Bloque 3: Pre-Triaje Humano | CU-09 a CU-12 | US-001, US-002, US-004 |
| J-03 Negativos | NEG-01 a NEG-05 | US-004, US-034 |
| J-SEC Fase 1: IDOR | CU-01 a CU-04 | US-007, US-027, US-002 |
| J-SEC Fase 2: XSS | CU-05 a CU-07 | US-007, US-027, US-029 |
| J-SEC Fase 3-7: PII, DoW, Auth, Webhook, Graceful | CU-08 a CU-21 + NEG | US-000, US-036, US-038, US-004 |

**Criterio de éxito:** 42 escenarios ejecutados. Todas las brechas IDOR P0 verificadas como cerradas. Bugs de seguridad catalogados.

---

### Iteración 6.5 — Journey J-05 (Gobernanza) + J-06 (Hub Ágil + US-008)

**Objetivo:** Certificar la infraestructura administrativa (RBAC, IdP, SLA) y la gestión ágil.

| Journey | Escenarios | US Testeadas |
|:-------:|:----------:|:------------:|
| J-05 Bloque 1: Internal IdP | CU-01 a CU-04 | US-048 |
| J-05 Bloque 2: RBAC Zero-Trust | CU-05 a CU-09 | US-036 |
| J-05 Bloque 3: Multi-Rol y EntraID | CU-10 a CU-13 | US-038 |
| J-05 Bloque 4: Visibilidad por Rol | CU-14 a CU-17 | US-025* |
| J-05 Bloque 5: SLA Corporativo | CU-18 a CU-20 | US-043 |
| J-05 Negativos | NEG-01 a NEG-04 | US-036, US-025, US-038 |
| J-06 Fases 1-2: Instanciación + CRUD | CU-01 a CU-07 | US-030 |
| J-06 Fase 3: Kanban (**requiere parche US-008**) | CU-08 a CU-11 | US-008 |
| J-06 Fases 4-6: SLA + Vistas + Cierre | CU-12 a CU-22 | US-030, US-043 |
| J-06 Negativos | NEG-01 a NEG-05 | US-030, US-043 |

> **\*US-025:** Incluida como validación cruzada en J-05/J-06 pero no en el inventario de 18 US del MVP como US individual.

**Criterio de éxito:** 51 escenarios ejecutados. US-008 funcional con state machine real.

---

### Iteración 6.6 — Journey J-07 (Arquitecto IA) + J-08 (Resiliencia)

**Objetivo:** Certificar las herramientas IA (DMN, BPMN Copilot) y la infraestructura de resiliencia.

| Journey | Escenarios | US Testeadas |
|:-------:|:----------:|:------------:|
| J-07 Fase 1: DMN Cognitivo | CU-01 a CU-06 | US-007 |
| J-07 Fase 2: Diseño BPMN | CU-07 a CU-11 | US-005 |
| J-07 Fase 3: Copiloto IA | CU-12 a CU-15 | US-027, US-005 |
| J-07 Fases 4-6: Pre-Flight, Deploy, Sandbox | CU-16 a CU-25 | US-005 |
| J-07 Negativos | NEG-01 a NEG-05 | US-007, US-005 |
| J-08 Fases 3-5: RabbitMQ + DLQ + Circuit Breaker | CU-09 a CU-18 | US-034 |
| J-08 Fase 6: Degradación Graceful | CU-19 | US-000, US-001 |
| J-08 Negativos relevantes | NEG-04 | US-034 |
| J-08 Fases 1-2: (**EXCLUIDAS — US-017 no desarrollada**) | CU-01 a CU-08, CU-20 a CU-22 | — |

**Criterio de éxito:** ~41 escenarios ejecutados (excluyendo US-017). Madurez de IA y resiliencia certificada.

---

### Iteración 6.7 — Remediación de Bugs + Regresión Final

**Objetivo:** Cerrar todos los bugs hallados en iteraciones anteriores y ejecutar regresión transversal.

| Tarea | Agente |
|-------|:------:|
| Priorizar y clasificar bugs hallados (P0 → P1 → P2) | Arquitecto Líder |
| Remediar bugs P0 y P1 | Backend + Frontend |
| Re-ejecutar escenarios fallidos | QA |
| Suite de regresión completa (smoke de cada Journey) | QA |
| Actualizar `coverage_matrix.md` con cobertura final | QA |
| Generar reporte de certificación `cierre_sprint_6.md` | Arquitecto Líder |
| Etiquetar release candidate `v1.0.0-rc1` | Arquitecto Líder |

---

## 5. Decisiones Arquitectónicas del Sprint

### D-01: US-017 (CQRS Event Sourcing) — Excluida del Scope E2E
La US-017 está **completamente sin desarrollar** (tabla `form_event_store` no existe, zero código CQRS). Implementarla en Sprint 6 excede el objetivo de certificación de lo que ya existe. Los escenarios J-08 CU-01 a CU-08 y CU-20 a CU-22 se marcan como **SKIP** con justificación documentada. Se documentará como deuda para V2.

### D-02: US-008 (Kanban) — Incluida como Re-implementación Parcial
El KanbanView usa mocks hardcodeados (~10%). El Sprint 6 incluye la implementación mínima de state machine real y endpoint `PATCH` para que los escenarios J-06 CU-08 a CU-11 puedan pasar. El `<UniversalSlaTimer>` es P2 y no bloquea.

### D-03: Infraestructura E2E contra Backend Real
Se abandona el patrón `page.route()` para los tests de certificación. Se creará `docker-compose.e2e.yml` con seed data. Los tests existentes basados en interceptores HTTP se mantienen como tests de componente pero se complementan con specs E2E reales.

### D-04: Rama Larga `sprint-6/uat-certification`
Una sola rama con commits incrementales por iteración. Merge a `main` solo al cierre formal tras regresión green. Tags intermedios opcionales (`v1.0.0-alpha.N`).

### D-05: B-20 — Cierre de Deuda Técnica DMN↔BPMN (Principio Low-Code)
La auditoría de código durante el refinamiento de J-02 reveló una asimetría crítica de usabilidad: la vinculación `formKey` → UserTask opera con un **dropdown visual de 1 clic**, pero la vinculación `decisionRef` → BusinessRuleTask es **manual e invisible** (solo se lee del XML importado). Esto contradice el propósito de transformación digital y filosofía low-code del iBPMS. Se resuelve en la Iteración 6.1 con: (1) Endpoint `/api/v1/dmn/definitions` que lista las tablas DMN publicadas en Camunda, (2) Dropdown visual en el sidebar del BpmnDesigner análogo al de FormKey, y (3) Rehidratación bidireccional del `decisionRef` en el event listener `selection.changed`. La experiencia resultante será: **seleccionar BusinessRuleTask → elegir tabla DMN del dropdown → elegir estrategia de binding → listo**.

---

## 6. Entregables del Sprint

| # | Entregable | Formato |
|:-:|-----------|:-------:|
| 1 | Reporte de certificación | `docs/sprints/cierre_sprint_6.md` |
| 2 | Release candidate tag | `v1.0.0-rc1` en Git |
| 3 | Matriz de cobertura actualizada | `.agentic-sync/coverage_matrix.md` |
| 4 | Bug tracker cerrado | `docs/sprints/sprint_6_bugs.md` |
| 5 | Suite E2E Playwright ejecutable contra Docker | `frontend/e2e/specs/` |
| 6 | Docker Compose para E2E | `docker-compose.e2e.yml` |

---

## 7. Criterio de Éxito Global

- [x] B-01 IDOR DmnGovernanceController cerrado en Sprint 5.1 (confirmado por auditoría forense)
- [x] B-02 IDOR BpmnCopilotController — Remediado en It. 6.1 (role prefix `ibpms_rol_*`, tenant propagation en JwtAuthFilter, Anti-IDOR `startsWith` en RagSessionCleanerUseCase) — E2E PASS 2/2
- [x] B-03 EmailWebhookController deprecado → HTTP 410 Gone — E2E PASS 2/2
- [ ] ≥85% de los ~194 escenarios UAT ejecutados contra backend real
- [ ] Todos los bugs P0 hallados están remediados
- [ ] ≥80% de los bugs P1 hallados están remediados
- [ ] US-008 funcional con state machine real (no mocks)
- [ ] Suite Playwright ejecutable con `npm run test:e2e` contra Docker
- [ ] Tag `v1.0.0-rc1` creado con checklist de MVP listo para producción
- [ ] `coverage_matrix.md` actualizada con estado final de cada CA

---

## 8. Riesgos Identificados

| Riesgo | Mitigación | Impacto |
|--------|-----------|:-------:|
| Hallazgo masivo de bugs en iteraciones 6.2-6.6 que expanda el sprint | Iteraciones dinámicas (6.7, 6.8...). Priorización P0/P1 estricta | 🟠 Alto |
| Docker Compose con Camunda puede ser lento en CI local | Optimizar images, usar Camunda 7 lightweight | 🟡 Medio |
| US-008 requiere más trabajo del estimado (state machine + WebSocket) | Scope mínimo: solo PATCH + propagación. Sin `<UniversalSlaTimer>` | 🟠 Alto |
| Refinamiento PO de UATs pendiente | Iteraciones pueden iniciar con scope general y refinarse en vuelo | 🟡 Medio |

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-19 | Creación inicial del Sprint Plan S6 | Arquitecto Líder SW |
| 2026-04-19 | B-20: Deuda Técnica DMN↔BPMN añadida a It. 6.1 + D-05 + Brecha P2 catalogada | PO + Antigravity |
| 2026-04-19 | Auditoría forense: B-01 (DmnGovernanceController IDOR) confirmado como CERRADO en S5.1. Solo 2 P0 activos (B-02 + B-03). Handoffs reescritos con 6 secciones (architect_handoff_protocol) + 3 workflows de gobernanza | Arquitecto Líder SW |
| 2026-04-19 | **It. 6.1 SELLADA:** B-02 + B-03 CERRADOS. E2E 4/7 PASS (57%). Login.vue data-testid añadidos. SecurityContextUtils limpiado. Anti-IDOR refactorizado a `startsWith`. Cierre formal en `cierre_iteracion_s6_1.md` + `coverage_matrix.md`. Deuda UI (data seed) transferida a It. 6.2 | Arquitecto Líder SW |
