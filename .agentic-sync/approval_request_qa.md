# Solicitud de Aprobación Técnica - Pruebas Unitarias CA-29 (US-005)

**Para:** Arquitecto Líder
**De:** Ingeniero de Automatización QA
**Fecha:** 2026-05-26
**Historia de Usuario:** US-005 (Desplegar y Versionar un Modelo de Proceso (BPMN))
**Criterio de Aceptación:** CA-29 (Copiar y Pegar Fragmentos entre Procesos)
**Fase:** TDD - Fase Roja (Red Phase)

## Resumen del Plan de Pruebas Propuesto

Se solicita la aprobación para modificar `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts` bajo la siguiente estrategia:

1. **Modificación del Mock de Modeler:**
   Añadir el mock de `clipboard` que simula la interfaz del portapapeles nativo de `diagram-js`:
   ```typescript
   const mockClipboard = {
       get: vi.fn(),
       set: vi.fn(),
       clear: vi.fn(),
       isEmpty: vi.fn()
   };
   ```
   En el método `get` del modeler simulado, se retornará `mockClipboard` cuando el servicio solicitado sea `'clipboard'`.

2. **Reinicio de Mocks:**
   En `beforeEach`, se reinstanciarán los mocks de `mockClipboard` (`mockClipboard.set = vi.fn()`, etc.) para asegurar el aislamiento y evitar efectos secundarios entre pruebas.

3. **Pruebas Unitarias en Fase Roja (CA-29):**
   Añadir la sección `describe('Pruebas para CA-29 (Copiar y Pegar Fragmentos entre Procesos)')` con la marca de trazabilidad `// @Traceability: US-005, CA-29 Copiar y Pegar Fragmentos entre Procesos` y dos casos de prueba:
   - **Caso 1:** Guardado en `localStorage` al copiar (`clipboard.set`).
   - **Caso 2:** Recuperación desde `localStorage` al pegar (`clipboard.get`).

Estas pruebas fallarán inicialmente (Fase Roja) ya que el componente `BpmnDesigner.vue` no cuenta aún con la lógica del portapapeles ni la sincronización con `localStorage`.

Por favor, proceda a la revisión y aprobación técnica del plan para iniciar la fase de ejecución.
