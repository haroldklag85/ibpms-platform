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

## Bugs Activos

_(Ninguno aún)_

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
