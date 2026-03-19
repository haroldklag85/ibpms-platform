# Contrato de Arquitectura Frontend (Iteración 25 | US-005: CA-46 a CA-50)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Elevar el nivel de las ServiceTasks y el Properties Panel. Implementar el mapeo visual tipado (DataMapperGrid) como medida Anti-JSON-Crudo e inyectar onboarding contextual (Tooltips reactivos).

## 📋 Contexto y Órdenes de Implementación:

### Tarea 1: Prohibición de JSON y Mapeo Visual (CA-49)
*   Cuando seleccionen un Conector API en una `ServiceTask` (Fase 24), queda **ESTRICTAMENTE PROHIBIDO** un `<textarea>` libre.
*   Inyecta en el Properties Panel un sub-componente llamado `<DataMapperGrid>`.
*   Tendrá 2 Columnas:
    *   **Izquierda (Expected Input):** Consume `GET /api/v1/integrations/connectors/{id}/schema` (Ej: `target_email (String)`).
    *   **Derecha (Process Variables):** Un Dropdown que consume `GET /api/v1/design/processes/{key}/variables`.

### Tarea 2: Coerción Inteligente (Type-Safety) (CA-50)
*   En el Dropdown de la derecha del `DataMapperGrid`, aplica heurística: si el campo Izquierdo espera un `Boolean`, deshabilita (color gris) todas las variables de la Derecha que sean `String` o `Number`. Pinta un Tooltip sobre ellas: *"Tipo Incompatible"*.

### Tarea 3: Tooltips y Detección de Sintaxis (CA-47, CA-48)
*   Inyecta un ícono `[?]` al lado de las cabeceras del Properties Panel (Ej. 'Headers', 'Listeners').
*   Al pasar el mouse, muestra un tooltip didáctico en HTML simple.
*   **Reactividad de Error (CA-48):** Si en un campo inyectan basura de sintaxis (Ej: `#{monto` incompleto), el input se pone rojo, y el Tooltip debe mutar a ROJO mostrando el error.

### Tarea 4: Warning Pre-Flight Message Event (CA-46)
*   Lee la respuesta del Pre-Flight Analyzer y asegúrate de pintar de color "Amarillo/Naranja" en el Canvas o en la Consola aquellos `MessageEvent` que el backend reporte como "Sin Conector".

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal.
Congela tu código en stash:
`git stash save "temp-frontend-US005-ca46-ca50"`

Informa textualmente la confirmación del guardado.
