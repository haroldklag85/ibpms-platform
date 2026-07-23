# 🐛 Sprint 7 — Bug Tracker

> **Creado por:** Certificación UAT Manual (Journey J-02)  
> **Fecha de creación:** 2026-05-27  
> **Fuente:** WORKFLOW_CERTIFICACION_MANUAL.md — Protocolo de Brechas  
> **Tester Humano:** Harold  
> **Agente QA:** Antigravity

---

## Resumen

| Severidad | Cantidad | IDs |
|-----------|:--------:|-----|
| 🔴 P0 (Bloqueante) | 0 | — |
| 🟠 P1 (Alta) | 0 | — |
| 🟡 P2 (Media) | 0 | — |
| 🟢 P3 (Baja) | 0 | — |

---

## Hallazgos de Entorno Activos

### HAL-S7-001: Campo `file`/`signature` exige UUID de Azurite — Bloqueante en QA

- **Escenario:** CU-J02-01 (Paso 6: Submit)
- **Severidad:** 🟠 **P1 (Alta)** — Bloqueante de certificación
- **Tipo:** Hallazgo de Entorno (NO es un bug de código)
- **US afectada:** US-003
- **Pantalla:** P7 (FormDesigner) — `ZodBuilder.ts` línea 140
- **Descripción:** Los campos tipo `file` y `signature` generan validación Zod `z.string().uuid()`. El Fuzzer genera "Dummy Data" (string plano) en vez de un UUID válido para estos campos. El Submit aborta por validación fallida. Los **4 formularios** del J-02 están afectados.
- **Impacto:** Impide guardar/publicar los 4 formularios → **BLOQUEA Misiones 2-4 del J-02**
- **Workaround propuesto:** Que el Fuzzer "Autocompletar Happy" genere UUIDs aleatorios válidos, o que el tester edite manualmente el JSON del Fuzzer con un UUID como `550e8400-e29b-41d4-a716-446655440000`
- **Fecha:** 2026-05-29
- **Estado:** ✅ RESUELTO — Fix verificado en `useFormDesignerStore.ts` línea 322: "Autocompletar Happy" ahora genera UUID válido para campos file/signature Frontend Agent (Fuzzer ahora genera UUIDs válidos)

---

## Bugs Activos

### BUG-S7-008: Archivo BPMN de prueba sin coordenadas DI — Pantalla negra al importar

- **Escenario:** CU-J02-06 (Importar BPMN)
- **Severidad:** 🟠 **P1 (Alta)** — Bloqueante de CU-J02-06 a CU-J02-09
- **US afectada:** US-028, US-005
- **Pantalla:** P6 (BpmnDesigner) — `/admin/modeler/bpmn`
- **Archivo:** `docs/uat/bpmn_examples/insurance_claims_complex.bpmn` — L245-249
- **Descripción:** El archivo BPMN tiene la sección `bpmndi:BPMNDiagram` completamente vacía (0 elementos `BPMNShape`/`BPMNEdge`). El comentario L245 dice: _"BPMN DI ommited to save space for strict backend XML execution tests"_. La librería `bpmn-js` necesita coordenadas DI para renderizar, sin ellas → pantalla negra.
- **Solución propuesta:** (A) Agregar coordenadas `BPMNShape` y `BPMNEdge` para los 22+ elementos. (B) O usar `modeling.layoutProcess()` de bpmn-js post-importación para auto-generar layout.
- **Fecha:** 2026-05-30
- **Estado:** 🟠 ABIERTO — Bloqueante de Misión 2

### BUG-S7-009: Pre-Flight estancado en "Validando..." — Badge nunca completa

- **Escenario:** CU-J02-10 (Pre-Flight valida proceso)
- **Severidad:** 🟠 **P1 (Alta)** — Bloqueante de CU-J02-10 y CU-J02-11
- **US afectada:** US-005
- **Pantalla:** P6 (BpmnDesigner) — `/admin/modeler/bpmn`
- **Descripción:** Al cargar la pantalla BPMN, el badge Pre-Flight (esquina superior derecha del canvas) muestra "Validando..." de forma permanente. Nunca transiciona a ✅ Validado, ⚠️ Advertencias, ni ❌ Errores. El canvas está vacío/negro con proceso "Crédito de Consumo V1" cargado. La función `debouncedValidate` (L1471-1505) llama a `integrationStore.validateProcess({ xml })` pero el resultado nunca llega.
- **Código:** `BpmnDesigner.vue` L1471-1505 — función `debouncedValidate`
- **Fecha:** 2026-05-30
- **Estado:** 🟠 ABIERTO — Bloqueante de Misión 3

### BUG-S7-010: Botón Deploy deshabilitado por Pre-Flight PENDING

- **Escenario:** CU-J02-11 (Deploy proceso)
- **Severidad:** 🟡 **P2 (Media)** — Dependiente de BUG-S7-009
- **US afectada:** US-005
- **Pantalla:** P6 (BpmnDesigner) — `/admin/modeler/bpmn`
- **Descripción:** El botón "✓ [VALIDAR Y DESPLEGAR]" es visible (rol `ROLE_SUPER_ADMIN`) pero está deshabilitado. El código L80 valida `:disabled="isDeploying || !['VALIDATED', 'WARNING'].includes(preFlightStatus)"`. Como `preFlightStatus` está estancado en `PENDING`, el botón nunca se habilita.
- **Dependencia:** Resolución de BUG-S7-009
- **Fecha:** 2026-05-30
- **Estado:** 🟡 ABIERTO — Dependiente de BUG-S7-009

### BUG-S7-011: Endpoints Kanban API retornan 404 — Tablero vacío

- **Escenario:** CU-J02-K01 (Kanban + formulario genérico)
- **Severidad:** 🟠 **P1 (Alta)** — Bloqueante de Misión 5
- **US afectada:** US-008, US-039
- **Pantalla:** Kanban (`/kanban`) — `KanbanView.vue`
- **Descripción:** Al navegar a `/kanban`, el tablero se muestra vacío (0 columnas, 0 tarjetas). La consola DevTools muestra 13 errores 404:
  - `GET /api/v1/kanban/boards/default-board/columns` → 404
  - `GET /api/v1/kanban/boards/default-board/tasks` → 404
  - `Error fetching board AsyncError` en `kanbanStore.ts:48:45`
- **Código frontend:** `kanbanStore.ts` L49-50 hace `Promise.all` de ambos endpoints
- **Causa probable:** El controller backend no expone `/api/v1/kanban/boards/{boardId}/columns` o no existe el board `default-board` en la BD
- **Fecha:** 2026-05-30
- **Estado:** 🟠 ABIERTO — Bloqueante de Misión 5

### BUG-S7-012: SQL Array Literal Malformado en Workdesk CQRS — HTTP 500

- **Escenario:** Carga inicial del Workdesk (`/workdesk`)
- **Severidad:** 🔴 **P0 (Bloqueante)** — Misión 7A completa bloqueada
- **US afectada:** US-001
- **Pantalla:** Workdesk — muestra "ALERTA DEL SISTEMA: NIVEL 0" con código 500
- **Descripción:** Al navegar a `/workdesk`, el backend genera una consulta SQL nativa contra `ibpms_workdesk_projection` con un filtro de assignee. Java serializa la lista de assignees como `[Super_Administrador]` (corchetes cuadrados, formato `List.toString()`), pero PostgreSQL espera arrays en formato `{Super_Administrador}` (llaves). PostgreSQL rechaza con `PSQLException: malformed array literal`.
- **Error backend exacto:**
  ```
  ERROR: malformed array literal: "[Super_Administrador]"
  Detail: "[" must introduce explicitly-specified array dimensions.
  SQLState: 22P02
  ```
- **Código involucrado:**
  - `WorkdeskQueryController.getGlobalInbox()` — L99
  - `WorkdeskQueryService.getWorkdeskTasks()` — L40
  - Repositorio JPA: `findWorkdeskTasks()` — SQL nativo con `CAST((?) AS VARCHAR[])`
- **SQL problemático:** `AND (CAST((?) AS VARCHAR[]) IS NULL OR w.assignee = ANY(CAST((?) AS VARCHAR[])))`
- **Solución propuesta:** Convertir la lista Java a formato PostgreSQL array string `{val1,val2}` antes de pasarla al query nativo, o usar un approach con `@Param` y conversión explícita
- **Errores cascada frontend:** 12-21 errores en consola (500 + 401 Unauthorized en reintentos)
- **Trace ID:** `8746bec1-a5e6-4060-aafc-ee4e3265adbc`
- **Fecha:** 2026-05-30
- **Estado:** 🔴 ABIERTO — Bloqueante P0

### BUG-S7-013: DashboardBAM TypeError — getProcessHealth is not a function

- **Escenario:** Carga del widget BAM en Workdesk (solo visible para SUPER_ADMIN)
- **Severidad:** 🟡 **P2 (Media)**
- **US afectada:** US-001, US-005
- **Pantalla:** `DashboardBAM.vue` inyectado dinámicamente en Workdesk
- **Descripción:** Al cargar el Workdesk, el componente `DashboardBAM.vue` intenta llamar `designationStore.getProcessHealth()`, pero la función no existe en el store. Esto genera un `TypeError` en consola.
- **Error exacto:** `designationStore.getProcessHealth is not a function`
- **Impacto:** Widget de métricas BAM no carga, pero no bloquea la bandeja principal (si no fuera por BUG-S7-012)
- **Fecha:** 2026-05-30
- **Estado:** 🟡 ABIERTO

### BUG-S7-005: Stage Simulator Hardcodeado — No Detecta Stages Personalizados

- **Escenario:** CU-J02-03 (FORM-03 Evaluación de Daños)
- **Severidad:** 🟡 **P2 (Media)**
- **US afectada:** US-003
- **Pantalla:** P7 (FormDesigner) — `FormDesigner.vue` L143-148
- **Descripción:** El dropdown del Stage Simulator solo muestra opciones hardcodeadas: `START_EVENT`, `ANALYSIS`, `DECISION`. No detecta dinámicamente stages personalizados como `INSPECTION` y `VALUATION` configurados en los campos de FORM-03.
- **Workaround:** Verificar stages visualmente via badges en el canvas
- **Solución propuesta:** Usar `computed` que extrae stages únicos de `canvasFields`
- **Fecha:** 2026-05-29
- **Estado:** ✅ RESUELTO — Fix verificado: `v-for="stage in availableStages"` en L145

### BUG-S7-006: Data Grid (field_array) sin Drop Zone + Fuzzer sin Mock Array

- **Escenario:** CU-J02-03 (FORM-03 Evaluación de Daños)
- **Severidad:** 🟠 **P1 (Alta)** — Bloqueante de CU-J02-03
- **US afectada:** US-003, CA-41
- **Pantalla:** P7 (FormDesigner) — `FormDesigner.vue` (falta `VueDraggable` para `field_array`)
- **Descripción:** (A) El Data Grid no tiene bloque `VueDraggable` en el canvas, impidiendo arrastrar campos hijos dentro. (B) El Fuzzer `generateMockPath` no genera datos para `field_array` → error `[itemsDanados] Required` en Zod. (C) El Submit aborta por fallo de validación.
- **Solución propuesta:** (1) Agregar `VueDraggable` para `field_array` como existe para `container`. (2) Generar `[{...}]` en el Fuzzer para arrays.
- **Fecha:** 2026-05-29
- **Estado:** ✅ RESUELTO — Fix verificado: VueDraggable L226-264 + buildMock recursivo L322-334

---

## Bugs Resueltos

### BUG-S7-001: Probar [Submit] envía payload vacío — Validación Zod falla con "invalid_type: Required" en TODOS los campos

- **Escenario:** CU-J02-01 (iForm Maestro "Auditoría de Siniestro")
- **Paso:** #7 (Probar [Submit])
- **Severidad:** 🟠 **P1 (Alta)** — Bloqueante de certificación
- **US afectada:** US-003
- **CAs afectados:** CA-29 (End-to-End Validation Engine & Integration)
- **Pantalla:** P7 (FormDesigner) — `/admin/modeler/forms/designer`
- **Resultado Esperado:** Al presionar "Probar [Submit]", el sistema debería recopilar los datos del canvas y validarlos contra el esquema Zod generado. Si los datos son válidos, debería mostrar toast de éxito.
- **Resultado Real:**
  - Modal: "Execute End-to-End Validation Engine & Integration (CA-29)"
  - ❌ FALLIDO: "Integridad I/O de Camunda no superada."
  - "El sistema Zod Dinámico arrojó infracciones de validación al intentar procesar **payload vacío**"
  - 13 campos muestran: `Rule 'invalid_type': Required`
  - ⚠️ "Acción de Submit Abortada por el Front-end. El API no ha sido contactado."
- **Evidencia:** Captura de pantalla con modal de error proporcionada por tester humano
- **Impacto:** Impide guardar/publicar formularios → **BLOQUEA Misiones 2, 3, 4 del J-02**
- **Fecha:** 2026-05-27
- **Estado:** ✅ RESUELTO

---

### BUG-S7-002: Radio Button no permite editar labels de opciones (fijas como "Opción 1"/"Opción 2")

- **Escenario:** CU-J02-01
- **Paso:** #4 (Arrastrar componentes — campo `REQUIERE_PERITAJE`)
- **Severidad:** 🟡 **P2 (Media)** — Degradación UX
- **US afectada:** US-003
- **CAs afectados:** CA-11 (Paleta de Componentes Base HTML5)
- **Pantalla:** P7 (FormDesigner)
- **Resultado Esperado:** El componente Radio Button debería permitir editar los labels de cada opción (ej: "Opción 1" → "Sí", "Opción 2" → "No")
- **Resultado Real:** Las opciones se quedan fijas como "Opción 1" y "Opción 2" sin posibilidad de cambio
- **Evidencia:** Captura 3 del tester — campo `REQUIERE_PERITAJE` muestra "○ Opción 1" / "○ Opción 2"
- **Impacto:** UX degradada + posible impacto en lógica condicional que depende del valor literal
- **Fecha:** 2026-05-27
- **Estado:** ✅ RESUELTO

---

### BUG-S7-003: Nombre del formulario no se refleja como título del canvas — Muestra "Solicitud Onboarding (V1)"

- **Escenario:** CU-J02-01
- **Paso:** #3 (Asignar nombre)
- **Severidad:** 🟢 **P3 (Baja)** — Cosmético / UX
- **US afectada:** US-003
- **Pantalla:** P7 (FormDesigner)
- **Resultado Esperado:** Al asignar "Auditoría de Siniestro" como nombre, el título del formulario en el canvas debería reflejarlo
- **Resultado Real:** El canvas muestra "Solicitud Onboarding (V1)" como título principal; el nombre asignado solo aparece en "Campo Base (Semilla)"
- **Evidencia:** Capturas 2 y 4 del tester — título "Solicitud Onboarding (V1)" en vez de "Auditoría de Siniestro"
- **Impacto:** Confusión UX — no es claro cuál es el nombre oficial del formulario
- **Fecha:** 2026-05-27
- **Estado:** ✅ RESUELTO

---

## Observaciones de Entorno (No Bugs)

### OBS-S7-005: DMN Copilot SSE Endpoint No Implementado — Editor Manual Inaccesible sin IA

- **Escenario:** CU-J02-05 (Crear tabla DMN)
- **Severidad:** 🟢 **Observación de Entorno** (No es bug)
- **US afectada:** US-007
- **Pantalla:** P15 (DmnIntelligence) — `/admin/modeler/dmn`
- **Descripción:** El endpoint `/api/v1/dmn/copilot/stream` retorna 404. El `DmnGridManual` (editor manual de tabla) solo se activa cuando `dmnDraft.hasData === true`, lo cual solo ocurre tras una generación exitosa vía SSE Copilot IA. No existe botón "Crear Manual" para bypassear el flujo IA.
- **Causa:** No hay servicio LLM/IA configurado en entorno E2E de QA. El endpoint SSE no está implementado en el backend Java.
- **Impacto:** CU-J02-05 no certificable en entorno QA sin IA. La verificación del Editor DMN Manual se difiere a entorno con IA configurada.
- **Workaround:** Ninguno disponible sin acceso a Copilot IA.
- **Fecha:** 2026-05-30
- **Estado:** ⚠️ DIFERIDO — Requiere entorno con servicio IA

