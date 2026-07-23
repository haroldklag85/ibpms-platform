# 🔬 Auditoría Forense Exhaustiva: UAT J-02 vs Tests E2E (v2)

**Fecha:** 2026-05-13 | **Auditor:** 🧠 ARQUITECTO LÍDER (Antigravity)
**UAT SSOT:** `docs/uat/casos_uso_uat_j02.md` (v5 — 57 escenarios, 1427 líneas)
**Directorio Auditado:** `frontend/e2e/certification/` (33 archivos)

## 🔴 VEREDICTO GLOBAL

| Métrica | Valor |
|---------|-------|
| **Total CUs en UAT** | **57** (40 positivos + 17 negativos) |
| **✅ CUBIERTO** | **0** (0%) |
| **⚠️ PARCIAL** | **14** (24.6%) |
| **❌ SIN TEST** | **35** (61.4%) |
| **🔵 NO APLICA** | **8** (14.0%) |
| **Cobertura Efectiva** | **~12%** (parciales ponderados al 50%) |

> **Conclusión:** La cobertura real es **catastrófica**. Ni un solo CU está CUBIERTO al 100%. Los 14 parciales son superficiales — la mayoría valida solo la carga de la página sin asertar la lógica de negocio descrita en el UAT.

---

## FASE 1: DISEÑO DE FORMULARIOS (US-003)

| # | CU | Veredicto | Evidencia |
|---|-----|-----------|-----------|
| 1 | **CU-J02-01** — Crear iForm Maestro "Auditoría de Siniestro" | ⚠️ PARCIAL | `us003-form-designer-persistence.e2e.spec.ts` L9-55: Crea un formulario **Simple** (no Maestro), no arrastra 16 componentes, no configura stages INTAKE/ANALYSIS/DECISION, no valida propiedades avanzadas (P-07/08/09). Solo hace clic en "Probar (Submit)" y valida modal `201 CREATED`. |
| 2 | **CU-J02-02** — Crear formularios Simple (FORM-02 y FORM-04) | ❌ SIN TEST | No existe test que cree FORM-02 (Veredicto Escalamiento) ni FORM-04 (Firma Director). El test us003 crea UN formulario genérico sin nombre ni configuración de firma digital (CA-31). |
| 3 | **CU-J02-03** — Crear iForm Maestro "Evaluación de Daños" (FORM-03) | ❌ SIN TEST | No existe test que cree FORM-03. No se validan hidden tokens (CA-47), GPS (CA-61), ni field_array (CA-41). |
| 4 | **CU-J02-04** — Generar y validar esquema Zod (4 formularios) | ❌ SIN TEST | No existe test que navegue a la pestaña Zod en Monaco IDE, ejecute el QA Sandbox Fuzzer, ni valide errores de validación cruzada. |

**Resumen Fase 1:** 0/4 cubiertos. 1 parcial (us003 toca solo la superficie). 3 sin test.

---

## FASE 2: CREACIÓN DMN + MODELADO BPMN (US-005, US-007, US-028)

| # | CU | Veredicto | Evidencia |
|---|-----|-----------|-----------|
| 5 | **CU-J02-05** — Crear tabla DMN "Decide_Claim_Coverage" | ⚠️ PARCIAL | `us007-dmn-preflight.spec.ts` L17-21: Inyecta XML DMN pre-fabricado vía `localStorage.setItem('ibpms_dmn_draft_v1', ...)`. **NO interactúa con la UI** para crear inputs/outputs/reglas. El test L28-46 solo valida el botón "Probar DMN" y la respuesta de simulación. |
| 6 | **CU-J02-06** — Importar proceso BPMN desde archivo | ⚠️ PARCIAL | `b20-dmn-dropdown.e2e.spec.ts` L19-33: Inyecta XML BPMN vía `window.__modelerInstance.importXML()` programáticamente. Importa un XML minimalista (1 solo BusinessRuleTask), no el proceso complejo de 22 elementos. No usa el botón "⬆️ Importar" del UI. |
| 7 | **CU-J02-07** — Vincular formularios a User Tasks vía `camunda:formKey` | ❌ SIN TEST | **Ningún archivo** hace clic en un User Task del canvas BPMN y selecciona un formulario en el Panel de Propiedades. El test b20 (L37-44) selecciona un BusinessRuleTask para DMN dropdown, pero **no** un UserTask para formKey. |
| 8 | **CU-J02-08** — Vincular DMN al BusinessRuleTask | ⚠️ PARCIAL | `b20-dmn-dropdown.e2e.spec.ts` L37-44: Hace clic en `[data-type="bpmn:BusinessRuleTask"]`, verifica que aparece un dropdown DMN con opciones. **Pero** no selecciona una tabla DMN específica ni valida que `camunda:decisionRef` se persista en el XML. |
| 9 | **CU-J02-09** — Exportar proceso BPMN y diagrama | ❌ SIN TEST | No existe test que haga clic en "Exportar .bpmn", "Exportar PNG" o "Exportar PDF". |

**Resumen Fase 2:** 0/5 cubiertos. 3 parciales (todos con atajos de inyección). 2 sin test.

---

## FASE 3: DEPLOY + PRE-FLIGHT (US-005)

| # | CU | Veredicto | Evidencia |
|---|-----|-----------|-----------|
| 10 | **CU-J02-10** — Pre-Flight valida proceso complejo | ⚠️ PARCIAL | `us005-bpmn-modeler-persistence.e2e.spec.ts` L24-41: Valida que al escribir un nombre de proceso, el auto-save dispara un draft HTTP 200. Pero **no** valida Pre-Flight de formKeys, decisionRef, ni complejidad ≤ MAX_NODES. El canvas está vacío (sin XML importado). |
| 11 | **CU-J02-11** — Release Manager despliega proceso | ⚠️ PARCIAL | `us005` L43-77: Hace clic en botón deploy, **pero** usa `btn.removeAttribute('disabled')` (L59) para forzar la habilitación — esquivando la validación Pre-Flight. Llena el modal de justificación y confirma, pero el `catch(() => null)` en L76 **silencia cualquier error**. |

**Resumen Fase 3:** 0/2 cubiertos. 2 parciales (ambos con trampas DOM).

---

## FASE 4: EJECUCIÓN E2E — 4 FLUJOS (US-001, US-002, US-029)

| # | CU | Veredicto | Evidencia |
|---|-----|-----------|-----------|
| 12 | **CU-J02-F1-01** — Iniciar caso siniestro (Happy Path) | 🔵 NO APLICA | Requiere Motor Camunda desplegado con el proceso `Process_InsuranceClaim` activo. No existe Mock Worker ni stub para iniciar instancias BPMN. Funcionalidad de "Gestor de Instancias" no implementada en Frontend. |
| 13 | **CU-J02-F1-02** — Analista N1 completa auditoría | 🔵 NO APLICA | Depende de F1-01 (instancia activa). Requiere formulario iForm Maestro con stages reales renderizados desde Camunda. |
| 14 | **CU-J02-F1-03** — Perito A y B evalúan daños (Multi-Instance) | 🔵 NO APLICA | Requiere multi-instance running en Camunda con 2 sesiones simultáneas. |
| 15 | **CU-J02-F1-04** — Mensaje policial + Parallel Join | 🔵 NO APLICA | Requiere API de mensajería Camunda `/engine-rest/message`. |
| 16 | **CU-J02-F1-05** — Sub-Process: Script + Firma Director | 🔵 NO APLICA | Requiere Script Task ejecutado en motor + formulario Firma Director. |
| 17 | **CU-J02-F1-06** — Reserva Fondos + Pago + Cierre | 🔵 NO APLICA | Requiere Mock Workers `reserve-funds` y `rollback-funds` no implementados. |
| 18 | **CU-J02-F2-01** — Rechazo automático por DMN | 🔵 NO APLICA | Requiere motor Camunda con DMN publicada ejecutando reglas reales. |
| 19 | **CU-J02-F3-01** — Timeout + Escalamiento | 🔵 NO APLICA | Requiere simulación de timer PT72H→PT5S en Camunda Job API. |
| 20 | **CU-J02-F4-01** — Error de pago + Compensación | ❌ SIN TEST | Aunque depende de infraestructura, el test RBAC anti-spoofing (`us007` L54-88) demuestra que se pueden hacer pruebas de backend directo. Falta test equivalente para compensation flow. |

**Resumen Fase 4:** 0/9 cubiertos. 8 NO APLICA (infraestructura Camunda). 1 sin test.

---

## FASE 5: FORMULARIO GENÉRICO EN KANBAN (US-008, US-039)

| # | CU | Veredicto | Evidencia |
|---|-----|-----------|-----------|
| 21 | **CU-J02-K01** — Crear actividad Kanban sin formulario diseñado | ❌ SIN TEST | `us039-panic-buttons.e2e.spec.ts` L20 navega a `sys_generic_form` con un `fakeTaskId`, pero el formulario no carga (L25: `expect.soft`). No valida MetadataGrid ni Resultado Gestión. |

**Resumen Fase 5:** 0/1 cubiertos. 1 sin test.

---

## FASE 6: OBSERVABILIDAD (US-001, US-005)

| # | CU | Veredicto | Evidencia |
|---|-----|-----------|-----------|
| 22 | **CU-J02-OBS-01** — Dashboard BAM muestra 4 flujos | ❌ SIN TEST | No existe test que navegue al Dashboard BAM. |
| 23 | **CU-J02-OBS-02** — Historial del motor completo | ❌ SIN TEST | No existe test que consulte `/engine-rest/history/process-instance`. |
| 24 | **CU-J02-OBS-03** — Audit Log del Modeler | ❌ SIN TEST | No existe test que abra el panel "📝 Auditoría" del Modeler. |

**Resumen Fase 6:** 0/3 cubiertos. 3 sin test.

---

## FASE 7A: WORKDESK — Grilla Unificada y SLA Vivo (US-001)

| # | CU | Veredicto | Evidencia |
|---|-----|-----------|-----------|
| 25 | **CU-J02-W01** — Paginación server-side | ⚠️ PARCIAL | `phase1-workdesk.spec.ts` L8-18: Valida que al menos 1 `[data-testid^="task-row-"]` es visible. **No** valida paginación (page=2), ni ordenamiento SLA ascendente, ni máximo 15 tarjetas. |
| 26 | **CU-J02-W02** — Búsqueda server-side con debounce | ⚠️ PARCIAL | `phase1-workdesk.spec.ts` L55-76: Busca "Workdesk Task" y "XYZNOEXISTE". Valida empty state "¡Bandeja Vacía!". **Pero** no valida debounce (300ms), ni paginación reiniciada, ni `pg_trgm`. |
| 27 | **CU-J02-W03** — Filtros facetados con contadores | ❌ SIN TEST | No existe test que seleccione filtros "BPMN"/"Kanban", valide chips removibles ni contadores `facets`. |
| 28 | **CU-J02-W04** — Semáforo SLA vivo (Ticking Engine) | ⚠️ PARCIAL | `phase1-workdesk.spec.ts` L36-53: Busca badges `sla-badge-green/yellow/gray`. **Pero** no valida transiciones 🟡→🔴 en vivo, ni verifica ausencia de `setInterval` por tarjeta. |
| 29 | **CU-J02-W05** — Recálculo SLA tras pestaña inactiva | ❌ SIN TEST | No existe test que simule `visibilitychange` ni recálculo post-inactividad. |
| 30 | **CU-J02-W06** — Consolidación BPMN + Kanban en grilla | ❌ SIN TEST | No existe test que valide badges ⚡/📋, barra de progreso (CA-23), ni columnas rígidas. |
| 31 | **CU-J02-W07** — KeepAlive y navegación de retorno | ❌ SIN TEST | No existe test que navegue ida/vuelta validando persistencia de filtros y scroll. |
| 32 | **CU-J02-W08** — WebSocket: Desaparición + relleno | ❌ SIN TEST | No existe test que valide WS `REMOVE` con animación CSS ni relleno automático. |
| 33 | **CU-J02-W09** — Delegación segura (Toggle vista) | ⚠️ PARCIAL | `us001-workdesk-delegation.spec.ts` L52-108: Valida IDOR 403 en delegación. **Pero** usa `page.route()` para mockear `/auth/me` (L55-59) — **viola Zero-Mock**. No valida banner "Estás viendo el escritorio de..." ni toggle legítimo. |
| 34 | **CU-J02-W10** — Anti Cherry-Picking "Atender Siguiente" | ⚠️ PARCIAL | `us001-workdesk-delegation.spec.ts` L27-49 + `us001-hexagonal-compliance.e2e.spec.ts` L49-74: Valida Feature Toggle y POST `/attend-next` retornando ≠500. **Pero** no valida que la grilla desaparece ni CTA gigante, y el test hexagonal es API-only sin UI. |

**Resumen Fase 7A:** 0/10 cubiertos. 5 parciales. 5 sin test.

---

## FASE 7B: CLAIM TASK — Reclamo, Liberación y Despojo (US-002)

| # | CU | Veredicto | Evidencia |
|---|-----|-----------|-----------|
| 35 | **CU-J02-C01** — Reclamo individual | ❌ SIN TEST | `us002-workbox-kanban.spec.ts` usa **mocks completos** (`route.fulfill()` en L9-35) para toda la bandeja. No existe test Zero-Mock de reclamo individual contra backend real. |
| 36 | **CU-J02-C02** — Concurrencia optimista (2 usuarios) | ❌ SIN TEST | No existe test con 2 contextos de browser simultáneos probando `SELECT FOR UPDATE SKIP LOCKED`. |
| 37 | **CU-J02-C03** — Reclamo masivo (Bulk Claim) | ⚠️ PARCIAL | `us002-workbox-kanban.spec.ts` L56-92: Prueba bulk claim **pero** con datos mockeados (L9-35: `route.fulfill()`) y respuesta mockeada (L74-76). Viola ADR-010 Zero-Mock. Payload OK pero contra dato ficticio. |
| 38 | **CU-J02-C04** — Exploración solo lectura + aviso reclamo | ❌ SIN TEST | No existe test de doble clic para modo lectura, ni WS `REMOVE` durante exploración. |
| 39 | **CU-J02-C05** — Liberación + Amnesia Transaccional | ⚠️ PARCIAL | `us002-workbox-kanban.spec.ts` L94-128: Prueba unclaim con motivo **pero** respuesta mockeada (L116-118). Viola Zero-Mock. No valida purga localStorage, ni reaparición en Cola, ni nota interna. |
| 40 | **CU-J02-C06** — Despojo forzoso por supervisor | ❌ SIN TEST | No existe test de `force-unclaim` con validación `team_id`. |
| 41 | **CU-J02-C07** — Trazabilidad forense pop-up | ❌ SIN TEST | No existe test que abra "Ver Trazabilidad" ni valide timeline vertical. |
| 42 | **CU-J02-C08** — Separación visual Cola vs Bandeja | ⚠️ PARCIAL | `us002-workbox-kanban.spec.ts` L40-54: Valida tabs "Mis Tareas"/"Pool Disponible" y clases CSS. **Pero** datos son mockeados. No valida contadores `[N]`/`[M]` en tiempo real. |

**Resumen Fase 7B:** 0/8 cubiertos. 3 parciales (todos violan Zero-Mock). 5 sin test.

---

## FASE 7C: KANBAN ÁGIL — Tablero, CRUD y Drag & Drop (US-008)

| # | CU | Veredicto | Evidencia |
|---|-----|-----------|-----------|
| 43 | **CU-J02-A01** — Crear tablero ágil con columnas | ❌ SIN TEST | `us008-kanban-zeromock.e2e.spec.ts` L28-51 renderiza columnas existentes pero **no crea** un tablero nuevo. |
| 44 | **CU-J02-A02** — CRUD de tarjetas Kanban | ❌ SIN TEST | No existe test que cree, edite ni elimine tarjetas. |
| 45 | **CU-J02-A03** — Drag & Drop entre columnas + WS | ⚠️ PARCIAL | `us008-kanban-zeromock.e2e.spec.ts` L78-112: Hace `card.dragTo(inProgressColumn)` e intercepta PATCH real. **Bueno**: Zero-Mock real. **Falta**: No valida propagación WS a segunda sesión. |
| 46 | **CU-J02-A04** — Motivo obligatorio al mover a BLOCKED | ⚠️ PARCIAL | `us008-kanban-zeromock.e2e.spec.ts` L117-153: Drag a BLOCKED, llena motivo, valida PATCH con `blockReason`. **Bueno**: Zero-Mock. **Falta**: No valida SLA (reloj sigue corriendo), ni badge tooltip. |
| 47 | **CU-J02-A05** — Time Tracking Play/Stop | ❌ SIN TEST | No existe test que valide timer Play/Stop por columna ni `ibpms_time_logs`. |
| 48 | **CU-J02-A06** — Inmutabilidad en DONE + Single-Assignee | ❌ SIN TEST | No existe test que valide solo-lectura en DONE ni política 1:1. |
| 49 | **CU-J02-A07** — Formulario genérico en tarea sin iForm | ❌ SIN TEST | No existe test que abra tarea Kanban sin formKey y valide `sys_generic_form`. |

**Resumen Fase 7C:** 0/7 cubiertos. 2 parciales (Zero-Mock reales — los mejores de todo el repo). 5 sin test.

---

## ESCENARIOS NEGATIVOS

| # | CU | Veredicto | Evidencia |
|---|-----|-----------|-----------|
| 50 | **CU-J02-NEG-01** — Guardar formulario sin campos | ❌ SIN TEST | No existe test. |
| 51 | **CU-J02-NEG-02** — Perito con datos inválidos | ❌ SIN TEST | No existe test. |
| 52 | **CU-J02-NEG-03** — Desplegar sin formKey | ❌ SIN TEST | No existe test. |
| 53 | **CU-J02-NEG-04** — Designer intenta desplegar sin rol RM | ⚠️ PARCIAL | `us007-dmn-preflight.spec.ts` L54-88: Test Anti-Spoofing valida HTTP ≥400 para usuario sin rol. **Pero** es para DMN `/publish`, no para BPMN `/deploy`. Cubre el concepto RBAC pero no el CU exacto. |
| 54 | **CU-J02-NEG-05** — Import BPMN con decisionRef huérfano | ❌ SIN TEST | No existe test. |
| 55 | **CU-J02-NEG-06** — Formulario Genérico con obs. inválidas | ❌ SIN TEST | `us039-panic-buttons.e2e.spec.ts` valida justificación <20 chars pero para Panic Buttons, no para `sys_generic_form`. |
| 56 | **CU-J02-NEG-07** — Director rechaza liquidación | ❌ SIN TEST | No existe test. |
| 57 | **CU-J02-NEG-08** — Hard Limit paginación >100 | ❌ SIN TEST | No existe test. |
| — | **CU-J02-NEG-09 a NEG-17** (9 escenarios) | ❌ SIN TEST | Ninguno de los 9 negativos restantes (IDOR, Rate Limiting, DTO Sanitización, Cross-team, Optimistic Rollback, 7 columnas, DONE inmutabilidad, Single-Assignee, Time Log inmutabilidad) tiene test E2E. |

**Resumen Negativos:** 0/17 cubiertos. 1 parcial (RBAC tangencial). 16 sin test.

---

## 📊 RESUMEN CONSOLIDADO POR FASE

| Fase | Total CUs | ✅ | ⚠️ | ❌ | 🔵 | % Efectivo |
|------|:---------:|:--:|:--:|:--:|:--:|:----------:|
| F1: Formularios | 4 | 0 | 1 | 3 | 0 | ~6% |
| F2: DMN + BPMN | 5 | 0 | 3 | 2 | 0 | ~15% |
| F3: Deploy | 2 | 0 | 2 | 0 | 0 | ~25% |
| F4: Ejecución 4 Flujos | 9 | 0 | 0 | 1 | 8 | 0% |
| F5: Genérico Kanban | 1 | 0 | 0 | 1 | 0 | 0% |
| F6: Observabilidad | 3 | 0 | 0 | 3 | 0 | 0% |
| F7A: Workdesk | 10 | 0 | 5 | 5 | 0 | ~12% |
| F7B: Claim | 8 | 0 | 3 | 5 | 0 | ~9% |
| F7C: Kanban | 7 | 0 | 2 | 5 | 0 | ~7% |
| Negativos | 17 | 0 | 1 | 16 | 0 | ~1.5% |
| **TOTAL** | **57** | **0** | **14** | **35** | **8** | **~12%** |

---

## 🚨 VIOLACIONES ZERO-MOCK DETECTADAS

| Archivo | Línea | Violación |
|---------|:-----:|-----------|
| `us002-workbox-kanban.spec.ts` | L9-35 | `route.fulfill()` — Mockea toda la bandeja de tareas |
| `us002-workbox-kanban.spec.ts` | L74-76 | `route.fulfill()` — Mockea respuesta bulk-claim |
| `us002-workbox-kanban.spec.ts` | L116-118 | `route.fulfill()` — Mockea respuesta unclaim |
| `us001-workdesk-delegation.spec.ts` | L55-59 | `route.fulfill()` — Mockea `/auth/me` para inyectar delegados |
| `us003-form-designer-persistence.e2e.spec.ts` | L31-40 | `page.route()` — Intercepta y modifica POST `/api/v1/forms` |
| `us007-dmn-preflight.spec.ts` | L17-21 | `localStorage.setItem()` — Inyecta DMN draft pre-fabricado |
| `us005-bpmn-modeler-persistence.e2e.spec.ts` | L59 | `btn.removeAttribute('disabled')` — Bypass de Pre-Flight |

---

## 📋 MANDATOS RESTRICTIVOS PARA REMEDIACIÓN (Iteración 7.2)

### MANDATO 1: Fixtures XML Autorizados
QA puede importar XML pre-construido vía `__modelerInstance.importXML()`, pero debe incluir mínimo: 1 StartEvent, 1 UserTask, 1 BusinessRuleTask, 1 EndEvent.

### MANDATO 2: Interacción Obligatoria con Panel de Propiedades
Tras cargar el fixture, QA DEBE hacer clic en el UserTask SVG, asertar que el Panel lateral abre, y configurar/validar `camunda:formKey`. Repetir para BusinessRuleTask con `decisionRef`.

### MANDATO 3: Creación Real de DMN
Prohibido `localStorage.setItem('ibpms_dmn_draft_v1')`. QA debe crear tabla DMN interactuando con la UI: agregar inputs, outputs y al menos 2 reglas.

### MANDATO 4: Erradicar route.fulfill()
Los 7 usos de `route.fulfill()` detectados deben ser eliminados. Todas las respuestas deben provenir del backend real (seed-e2e.sql).

### MANDATO 5: Eliminar bypass DOM
Prohibido `removeAttribute('disabled')` y `expect.soft()` como silenciadores. Los botones deben habilitarse por la lógica real del componente.

---

> // @Traceability: Auditoría UAT J-02 (T-24) — v2 Exhaustiva
