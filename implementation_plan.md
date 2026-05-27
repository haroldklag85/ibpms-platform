# Plan de Trabajo QA - US-005, CA-29 Copiar y Pegar Fragmentos entre Procesos (TDD Fase Roja)

Este plan detalla los pasos para modificar el archivo de pruebas unitarias `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts` agregando las pruebas requeridas para verificar el comportamiento de copiado y pegado de fragmentos de procesos en el Modeler utilizando `localStorage` en fase roja de TDD.

## 1. Archivos a Modificar
- **Pruebas unitarias:** `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts`

## 2. Modificaciones Propuestas

### M1: Actualizar el Mock de `bpmn-js/lib/Modeler`
- Declarar un objeto mock global `mockClipboard` al inicio del archivo (con prefijo `mock` para evitar errores de hoisting de Vitest):
  ```typescript
  const mockClipboard = {
      get: vi.fn(),
      set: vi.fn(),
      clear: vi.fn(),
      isEmpty: vi.fn()
  };
  ```
- Modificar el método `get` del `MockModeler` mockeado para que cuando se solicite el servicio `'clipboard'`, retorne `mockClipboard`:
  ```typescript
  if (name === 'clipboard') {
      return mockClipboard;
  }
  ```

### M2: Reiniciar el Mock de Clipboard en `beforeEach`
- En el bloque `beforeEach`, reasignar o limpiar las funciones mock de `mockClipboard` para asegurar aislamiento entre pruebas:
  ```typescript
  mockClipboard.get = vi.fn();
  mockClipboard.set = vi.fn();
  mockClipboard.clear = vi.fn();
  mockClipboard.isEmpty = vi.fn();
  ```

### M3: Agregar Pruebas Unitarias para CA-29
- Crear una nueva suite `describe('Pruebas para CA-29 (Copiar y Pegar Fragmentos entre Procesos)')`.
- Agregar la marca de trazabilidad requerida:
  `// @Traceability: US-005, CA-29 Copiar y Pegar Fragmentos entre Procesos`
- **Caso de Prueba 1:** "Debe guardar los elementos en localStorage al copiar (Ctrl+C / clipboard.set)".
  - Montar el componente `BpmnDesigner` y esperar Promesas.
  - Obtener el modeler desde `(window as any).__modelerInstance`.
  - Invocar `clipboard.set` con un árbol de elementos de prueba.
  - Verificar que el árbol se serialice y almacene en `localStorage` bajo `bpmn_shared_clipboard`.
- **Caso de Prueba 2:** "Debe recuperar los elementos desde localStorage al pegar (Ctrl+V / clipboard.get)".
  - Montar el componente y esperar Promesas.
  - Obtener el modeler desde `(window as any).__modelerInstance`.
  - Almacenar un árbol serializado en `localStorage` bajo `bpmn_shared_clipboard`.
  - Invocar `clipboard.get` y verificar que retorne correctamente el objeto deserializado.

## 3. Verificación de Fase Roja (TDD Red Phase)
- Ejecutar la suite de pruebas unitarias (`npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts` en `frontend/`).
- Dado que la lógica del portapapeles aún no está implementada en `BpmnDesigner.vue`, ambas pruebas deben FALLAR inicialmente de manera limpia.

## 4. Fase de Consolidación (Post-Aprobación del Arquitecto)
- Confirmar que las pruebas compilan y fallan (Fase Roja).
- Realizar commit de los cambios con un mensaje descriptivo y hacer push a la rama `sprint-6`.
