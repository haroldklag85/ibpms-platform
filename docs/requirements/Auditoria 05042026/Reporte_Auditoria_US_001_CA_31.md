# Reporte de Auditoría Estricta: US-001 (CA-31)
## Auto-Refresco Pasivo al Volver de Inactividad Prolongada

### 🗺️ Ruta Estructural Navegada (Top-Down)
1. `view_file: docs/requirements/v1_user_stories_index.md` (Para localizar la Épica de la US-001).
2. `view_file: docs/requirements/epics/epic_A_motor_core.md` (Para analizar semánticamente el CA-31).
3. `list_dir: frontend/` y `frontend/src/` (Para localizar el punto de entrada de la UI).
4. `list_dir: frontend/src/views/` (Para ubicar la pantalla contenedora).
5. `view_file: frontend/src/views/Workdesk.vue` (Para auditar el controlador del ciclo de vida y visibilidad).
6. `view_file: frontend/src/stores/timeStore.ts` (Para verificar el motor de inactividad reactivo).

### 🏷️ Archivos Etiquetados con Éxito (`@Traceability`)
*   `frontend/src/views/Workdesk.vue`: Inyectado sobre la función `onVisibilityReturn` responsable de interceptar el retorno a la pestaña activa.
*   `frontend/src/stores/timeStore.ts`: Inyectado sobre la función de exposición pública de milisegundos de inactividad `getInactivityMs`.

### 🚨 Brechas de Implementación y Deuda Técnica
La auditoría estructurada reveló las siguientes desviaciones críticas de los criterios estipulados en el **CA-31**:

1. **Violación de UI Invasiva (Loader Global):** El CA-31 requiere un refresco **silencioso en segundo plano** apoyado de un *"indicador sutil de actualización (Ej: un shimmer sobre las filas existentes)"*. La implementación actual invoca directamente `await loadData()`, lo cual muta reactivamente la bandera `store.isLoading` en Pinia, oscureciendo toda la pantalla del operario con un modal blanco superpuesto de bloqueo. Se incumple la regla del refresco no-disruptivo.
2. **Carencia de Mitigación Transaccional (Sin Try-Catch):** El CA estipula la provisión defensiva para caídas de red: *"si la petición de refresco falla (error de red), la grilla mantendrá los datos del KeepAlive con un Toast discreto"*. El código auditado delega toda la promesa de asincronía a `loadData()` sin envolverla en un bloque `try/catch` para interceptar la eventualidad de red ni disparar la notificación persistente correspondiente, lo que generaría un crash encadenado o un spinner fantasma irresoluble en caso de pérdida de conectividad durante el *visibility return*.

### ⚠️ Violaciones de Arquitectura
*   **Fuga de Responsabilidad de Datos Mudos (Silent Data Fetch):** El componente del store `WorkdeskStore` no ofrece un parámetro de invocación de lectura "en sombra" (Silent Fetch), haciendo imposible el cumplimiento UI/UX del CA sin refactorizar el Action de Pinia.
