# Solicitud de Aprobación de Plan de Implementación (US-005)
**Fecha:** 2026-06-06  
**Rol:** [🎨 FRONTEND - VUE]  
**Sprint:** Sprint 6 — Iteración 3  

## Resumen de la Tarea: Rediseño del Panel Lateral de Validación y Simulación (CA-80 a CA-84)

Hemos elaborado un plan detallado en `implementation_plan.md` que aborda los criterios de aceptación del Sprint:

1. **CA-80: Panel Lateral Derecho Resizable (Push Layout):**
   - Se reemplaza el modal glassmorphic central por un `<aside>` en la zona derecha del modelador.
   - El panel de propiedades de Camunda se oculta dinámicamente (`v-show="!showSandboxModal"`).
   - Implementación de un resizer nativo mediante eventos `mousedown`, `mousemove` y `mouseup` globales que limita el ancho entre `400px` y `700px`.

2. **CA-81: Acordeón de Validación e Integración Paralela:**
   - La interfaz organiza las fases en acordeón vertical con secciones independientes colapsables (`linter`, `preflight`, `simulator`).
   - Se conserva la compatibilidad con el funnel de validaciones paralelas y el linter local.

3. **CA-82 y CA-83: Grilla de Variables y LocalStorage:**
   - En lugar de un popup básico de variable faltante, se inyecta una grilla interactiva CRUD en la Fase 3 de simulación.
   - Permite listar, agregar, editar inline y eliminar variables en tiempo real, sincronizando inmediatamente con `localStorage` bajo `ibpms_sandbox_variables_${processId}`.

4. **CA-84: Trazado Progresivo Nodo a Nodo:**
   - Visualización progresiva (delay de `400ms`) de los halos verdes en el canvas para simular el recorrido del token.
   - Conserva fallback sincrónico durante la ejecución de pruebas unitarias para no romper regresión.

5. **Retrocompatibilidad & Zero Test-Decay:**
   - Se mantiene la variable `showSandboxModal` y los identificadores `data-testid="sandbox-glass-modal"`, `data-testid="linter-level"`, `data-testid="preflight-level"`, `data-testid="sandbox-level"` sobre el panel lateral y sus secciones de acordeón correspondientes. De esta forma, la suite de pruebas unitarias del frontend (`BpmnDesigner.spec.ts`) pasará sin modificaciones.

Solicito aprobación al Lead Architect para proceder con la fase de ejecución.
