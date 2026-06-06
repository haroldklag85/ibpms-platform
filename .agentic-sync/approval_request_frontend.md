# Solicitud de Aprobación de Plan de Trabajo — Modificaciones Frontend (US-005, CA-80 a CA-84)

**Para:** [🧠 ARQUITECTO LÍDER]
**De:** [🎨 FRONTEND - VUE]
**Historia de Usuario:** US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
**Criterios de Aceptación:** CA-80, CA-81, CA-82, CA-83, CA-84 (Embudo de Validación de 3 Niveles y Simulación Interactiva)

---

## Resumen del Plan de Trabajo

### 1. Modificaciones de Código en `BpmnDesigner.vue`
*   **CA-80 (Botón y Modal Consolidado):** Cambiar el botón `@click="runSandbox"` por uno que dispare `openValidationAndSimulation()`. Añadir un modal glassmorphic de 3 niveles con navegación por pestañas ('linter', 'preflight', 'sandbox') y logs de consola.
*   **CA-81 (Validación Concurrente y Bloqueo):** Disparar en paralelo `runClientLinter()` y `runPreFlightBackend()` (usando `Promise.all`). Bloquear selectivamente la simulación si existen errores fatales en el linter local o en el pre-flight del backend. Los warnings no bloquearán la simulación.
*   **CA-82 (Captura de HTTP 422 y Re-intento):** En el catch de `startSimulation()`, si la API responde con 422 y error `MISSING_VARIABLE`, pausar la simulación, pedir el valor en un popup emergente y reintentar la llamada de simulación inyectando las variables acumuladas.
*   **CA-83 (Persistencia Local):** Persistir las variables ingresadas de forma temporal en `localStorage` usando la clave `ibpms_sandbox_variables_${processId}` para que se mantengan entre sesiones de diseño.
*   **CA-84 (Neon Halos y Limpieza):** Renderizar marcadores `highlight-executed` en los nodos ejecutados al finalizar con éxito la simulación del sandbox. Implementar un botón "Limpiar Trayectoria" (`data-testid="btn-clear-trajectory"`) para remover todos los marcadores y resetear el listado de nodos ejecutados.

### 2. Verificación de QA y SRE
1.  **Pruebas Unitarias:** Ejecutar la suite `BpmnDesigner.spec.ts` en WSL para validar que los escenarios unitarios (CA-80 a CA-84) pasen exitosamente en verde.
2.  **Compilación de Producción:** Ejecutar `npm run build` en la subcarpeta `frontend/` en WSL para verificar que la compilación sea limpia.

---

## 3. Control de Versiones
1.  Hacer commit de los cambios con convención semántica (`feat(design): implement unified 3-level validation funnel and interactive sandbox simulation (US-005)`) en la rama del sprint actual.
2.  Hacer push directo a origin sin pasar por la rama `main` (conforme a las reglas de Zero-Trust Git).

*Quedo a la espera de su aprobación formal para pasar de PLANNING a EXECUTION mode.*
