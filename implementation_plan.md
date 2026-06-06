# Plan de Implementación — Embudo de Validación y Simulación de Sandbox (US-005, CA-80 a CA-84)

Este plan describe el diseño e implementación para el embudo de validación en 3 niveles y la simulación interactiva de sandbox dentro de `BpmnDesigner.vue`.

## 1. Objetivos Técnicos
*   **CA-80 (Modal Glassmorphic Consolidado):** Reemplazar el botón "Probar en Sandbox" por "🧪 Validar y Simular", el cual despliega un modal glassmorphic con 3 niveles secuenciales/pestañas: Linter Local, Pre-Flight Analyzer y Sandbox Simulator.
*   **CA-81 (Ejecución Paralela y Bloqueo Selectivo):** Al abrir el modal, ejecutar paralelamente la validación del linter local y el pre-flight del backend. Si se encuentran errores fatales, bloquear el inicio de la simulación. Los warnings no deben bloquear la simulación.
*   **CA-82 (Captura de Variables e Interactividad HTTP 422):** Si el motor de simulación responde con un error HTTP 422 y tipo `MISSING_VARIABLE`, pausar la ejecución, desplegar un popup interactivo pidiendo el valor de la variable requerida, y al confirmar, reintentar la simulación inyectando el valor ingresado.
*   **CA-83 (Persistencia de Variables por Proceso):** Guardar y recuperar las variables ingresadas en `localStorage` usando la clave estructurada `ibpms_sandbox_variables_${processId}`.
*   **CA-84 (Trazado de Halos de Ejecución y Limpieza):** Marcar los nodos ejecutados en el canvas de bpmn-js usando la clase `highlight-executed` (con animaciones neon CSS). Agregar un botón "Limpiar Trayectoria" (`data-testid="btn-clear-trajectory"`) que remueva los marcadores.

## 2. Cambios en Código (`BpmnDesigner.vue`)
1.  **Variables Reactivas:**
    *   `showSandboxModal` (boolean)
    *   `sandboxStage` ('linter' | 'preflight' | 'sandbox')
    *   `preFlightErrors` (string[])
    *   `preFlightWarnings` (string[])
    *   `sandboxBlocked` (boolean)
    *   `showVariablePopup` (boolean)
    *   `missingVariableName` (string)
    *   `tempVariableValue` (string)
    *   `sandboxVariables` (Record<string, any>)
    *   `executedNodes` (string[])
    *   `isSimulating` (boolean)
    *   `simulationLogs` (string[])

2.  **Lógica y Métodos:**
    *   `openValidationAndSimulation()`: Limpia estados, carga variables guardadas, abre el modal en pestaña 'linter' y lanza `runValidationFunnel()`.
    *   `runPreFlightBackend()`: Realiza la llamada de validación del proceso al backend (`integrationStore.validateProcess`). Captura errores y warnings del response.
    *   `runValidationFunnel()`: Ejecuta concurrentemente `runClientLinter()` y `runPreFlightBackend()`. Llama a `evaluateBlockingSelectivo()`.
    *   `evaluateBlockingSelectivo()`: Determina si el linter o el preflight tienen errores fatales (bloqueando la simulación).
    *   `startSimulation()`: Ejecuta la simulación enviando el XML y las variables acumuladas. Maneja el error 422 para solicitar variables faltantes y dibuja los halos neones al finalizar con éxito.
    *   `submitVariable()`: Guarda la variable provista en `sandboxVariables`, persiste en localStorage, cierra el popup y reintenta `startSimulation()`.
    *   `saveVariablesToLocalStorage()` / `loadVariablesFromLocalStorage()`: Gestiona la persistencia en `localStorage`.
    *   `renderTrajectoryHalos()` / `clearTrajectory()`: Agrega o quita el marcador `highlight-executed` a los elementos del canvas.

3.  **UI HTML (Modales y Botones):**
    *   Actualizar el botón "Probar en Sandbox" para invocar a `openValidationAndSimulation`.
    *   Agregar el botón de "Limpiar Trayectoria" visible únicamente cuando `executedNodes.length > 0`.
    *   Agregar el modal glassmorphic de 3 niveles y el popup flotante para variables al final de la plantilla HTML.

4.  **Estilos CSS:**
    *   Añadir clases y animaciones de tipo pulso neon para `.highlight-executed`.

## 3. Pruebas y Compilación
*   Ejecutar las pruebas en WSL:
    `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend -e npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts`
*   Ejecutar el build en WSL:
    `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend -e npm run build`
