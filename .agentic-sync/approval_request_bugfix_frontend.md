# Solicitud de Aprobación de Plan de Implementación (BUG-US005-VERSIONS-FE)
**Fecha:** 2026-06-06  
**Rol:** [🎨 FRONTEND - VUE]  
**Sprint:** Sprint 6 — Iteración 3  

## Resumen de la Tarea: Corrección y Limpieza de Mocks en Historial de Versiones

Hemos elaborado un plan detallado en `implementation_plan.md` que aborda los criterios de aceptación y los hallazgos del diagnóstico del bug:

1. **Limpiar mocks del historial de versiones y mapear respuesta del backend:**
   - Ubicación: `BpmnDesigner.vue` (`fetchVersions`)
   - Eliminación del fallback con datos mock fijos de Carlos M. y Ana García en el bloque `catch`.
   - Inicialización correcta de `versionHistory.value = []` en caso de error o datos inválidos.
   - Mapeo de campos del backend: `versionId` a `version`, `isLatest` a `status` (traducido a `ACTIVO` o `ARCHIVADO`), y valores por defecto para `date` y `author`.

2. **Manejar lista vacía en el template HTML:**
   - Ubicación: `BpmnDesigner.vue` (template del historial de versiones)
   - Uso de `v-else-if` para la lista de versiones y `v-else` para renderizar el mensaje visual: `"No hay versiones publicadas aún."` con el atributo `data-testid="no-versions-msg"`.

3. **Agregar Test Unitario en Vitest:**
   - Ubicación: `BpmnDesigner.spec.ts`
   - Test unitario que simula una respuesta vacía del backend, invoca `fetchVersions` y comprueba que se renderiza el mensaje de lista vacía en el DOM.

4. **Trazabilidad y Leyes Globales:**
   - Aplicación estricta de `// @Traceability: US-005, CA-15, BUG-FIX: Limpiar mocks del historial de versiones y mapear respuesta del backend` en todas las modificaciones.
   - Respeto a la Ley Global 2: validación local con `npm run build` y ejecución de Vitest.
   - No se usará `git stash`.

Solicito aprobación al Lead Architect / Humano Enrutador para proceder con la fase de ejecución.
