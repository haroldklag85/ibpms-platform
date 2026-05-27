# Plan de Trabajo QA (Frontend) - US-005, CA-25 Zoom y Minimap

Este plan detalla las modificaciones y pruebas automatizadas a realizar en el frontend para validar las funcionalidades de Zoom, Minimap y Navegación Visual en la pantalla del Modelador BPMN (`BpmnDesigner.vue`).

## 1. Archivos a Modificar
- [BpmnDesigner.spec.ts](file:///C:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts): Archivo que contiene la suite de pruebas unitarias/componentes del BPMN Designer.

## 2. Acciones del Plan
1. **Habilitación de la Suite:**
   - Remover `.skip` de la suite principal `describe('Pantalla 6: BPMN Designer (Frontend QA)', ...)` en `BpmnDesigner.spec.ts`.
2. **Refactorización de Mocks de bpmn-js:**
   - Declarar variables de espías (`mockZoom` y `mockOpen`) a nivel superior con el prefijo `mock` para poder ser referenciadas dentro del bloque de `vi.mock('bpmn-js/lib/Modeler', ...)`.
   - Implementar `mockZoom` de manera que retorne `1.0` por defecto cuando no recibe argumentos (simulando obtener el zoom actual) y el valor pasado cuando se define.
   - Modificar la propiedad `get` del `MockModeler` para que si se solicita `'canvas'` o `'minimap'`, retorne un objeto que asocie las llamadas a `zoom` y `open` con nuestros espías.
3. **Desarrollo de las Pruebas Unitarias para CA-25:**
   - **Prueba 1 (Existencia de Controles):** Verificar la existencia de los botones con title `Zoom In`, `Zoom Out` y `Fit Viewport` y validar sus contenidos (`+`, `-`, `O`).
   - **Prueba 2 (Funcionalidad de Zoom In):** Simular el click en Zoom In y comprobar que llama a `canvas.zoom` con `1.3` (1.0 + 0.3).
   - **Prueba 3 (Funcionalidad de Zoom Out):** Simular el click en Zoom Out y comprobar que llama a `canvas.zoom` con `0.7` (1.0 - 0.3).
   - **Prueba 4 (Funcionalidad de Zoom Fit):** Simular el click en Zoom Fit y comprobar que llama a `canvas.zoom` con `'fit-viewport'`.
   - **Prueba 5 (Minimap Abierto):** Comprobar que al montar el componente, se invoca la inicialización del minimap mediante `minimap.open()`.
4. **Trazabilidad Obligatoria:**
   - Incluir la marca de trazabilidad `// @Traceability: US-005, CA-25 Zoom y Minimap` antes de las nuevas pruebas.
5. **TDD (Fase Roja y Fase Verde):**
   - Correr las pruebas inicialmente forzándolas a fallar (por ejemplo, comentando momentáneamente los botones o modificando los incrementos esperados).
   - Ejecutar la suite completa para confirmar que pasa en verde tras verificar el comportamiento del componente.

## 3. Comandos de Verificación
Para ejecutar las pruebas locales de Vitest:
`npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts` desde el subdirectorio `frontend`.
