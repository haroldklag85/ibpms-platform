# Solicitud de Aprobación de Plan de Trabajo (QA Automation)

**Para:** Arquitecto Líder
**De:** Ingeniero de Automatización QA (Frontend)
**Fecha:** 2026-05-26
**Asunto:** Suite de Pruebas Unitarias para US-005, CA-25 (Zoom, Minimap y Navegación Visual)

## Resumen del Plan
Se propone la siguiente estrategia para la habilitación de las pruebas unitarias y de componentes en `BpmnDesigner.spec.ts`:

1. **Habilitación de la Suite:**
   - Remover `.skip` de `describe('Pantalla 6: BPMN Designer (Frontend QA)', ...)` en `BpmnDesigner.spec.ts` (si existiera).

2. **Refactorización de Mocks de bpmn-js (Vitest):**
   - Introducir espías (`mockZoom` y `mockOpen`) a nivel global en la cabecera de la especificación para interceptar y validar las llamadas de zoom y minimap.
   - Configurar `mockZoom` para que retorne `1.0` por defecto cuando se consulte el nivel de zoom actual, y retorne el valor ingresado cuando se asigne un nuevo nivel.

3. **Estructura de Casos de Prueba (CA-25):**
   - **Prueba 1 (Existencia de Controles):** Validar la existencia física y etiquetas/títulos de los botones de Zoom In (`+`), Zoom Out (`-`) y Zoom Fit (`O`).
   - **Prueba 2 (Funcionalidad de Zoom In):** Asegurar que al hacer click se llame a `canvas.zoom` con el valor incrementado a `1.3` (+0.3).
   - **Prueba 3 (Funcionalidad de Zoom Out):** Asegurar que al hacer click se llame a `canvas.zoom` con el valor decrementado a `0.7` (-0.3).
   - **Prueba 4 (Funcionalidad de Zoom Fit):** Asegurar que al hacer click se llame a `canvas.zoom` con el parámetro `'fit-viewport'`.
   - **Prueba 5 (Minimap Abierto):** Comprobar que al montar el componente se ejecute `minimap.open()`.

4. **Trazabilidad Obligatoria:**
   - Adición del tag: `// @Traceability: US-005, CA-25 Zoom y Minimap` en la declaración de las pruebas.

5. **Estrategia TDD:**
   - **Fase Roja:** Probar con aserciones erróneas o mock inactivo para verificar que fallan como se espera.
   - **Fase Verde:** Corregir y ejecutar la suite para confirmar el 100% de éxito.

Por favor, valide y apruebe esta propuesta para proceder con la ejecución técnica.
