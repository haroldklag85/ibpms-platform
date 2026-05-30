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

### BUG-S7-005: Stage Simulator Hardcodeado — No Detecta Stages Personalizados

- **Escenario:** CU-J02-03 (FORM-03 Evaluación de Daños)
- **Severidad:** 🟡 **P2 (Media)**
- **US afectada:** US-003
- **Pantalla:** P7 (FormDesigner) — `FormDesigner.vue` L143-148
- **Descripción:** El dropdown del Stage Simulator solo muestra opciones hardcodeadas: `START_EVENT`, `ANALYSIS`, `DECISION`. No detecta dinámicamente stages personalizados como `INSPECTION` y `VALUATION` configurados en los campos de FORM-03.
- **Workaround:** Verificar stages visualmente via badges en el canvas
- **Solución propuesta:** Usar `computed` que extrae stages únicos de `canvasFields`
- **Fecha:** 2026-05-29
- **Estado:** 🟡 ABIERTO

### BUG-S7-006: Data Grid (field_array) sin Drop Zone + Fuzzer sin Mock Array

- **Escenario:** CU-J02-03 (FORM-03 Evaluación de Daños)
- **Severidad:** 🟠 **P1 (Alta)** — Bloqueante de CU-J02-03
- **US afectada:** US-003, CA-41
- **Pantalla:** P7 (FormDesigner) — `FormDesigner.vue` (falta `VueDraggable` para `field_array`)
- **Descripción:** (A) El Data Grid no tiene bloque `VueDraggable` en el canvas, impidiendo arrastrar campos hijos dentro. (B) El Fuzzer `generateMockPath` no genera datos para `field_array` → error `[itemsDanados] Required` en Zod. (C) El Submit aborta por fallo de validación.
- **Solución propuesta:** (1) Agregar `VueDraggable` para `field_array` como existe para `container`. (2) Generar `[{...}]` en el Fuzzer para arrays.
- **Fecha:** 2026-05-29
- **Estado:** 🟠 ABIERTO — Bloqueante

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
