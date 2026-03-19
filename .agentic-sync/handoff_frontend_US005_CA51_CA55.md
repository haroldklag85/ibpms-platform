# Contrato de Arquitectura Frontend (Iteración 26 | US-005: CA-51, CA-53, CA-55)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Evolucionar el `<DataMapperGrid>` dotándolo de capacidades avanzadas (Mapeo Estático y Headers) y blindando lógicas Swagger (OneOf).

## 📋 Contexto y Órdenes de Implementación:

### Tarea 1: Valores Constantes en Mapeo (CA-51)
*   En la columna derecha del `<DataMapperGrid>` (Payload Input), añade un Toggle/Switch interactivo: `[Variable Zod] <-> [Valor Estático]`.
*   Si eligen "Variable", muestra el Dropdown normal. Si eligen "Estático", muta a un `<input type="text">` permitiendo al Arquitecto digitar datos quemados (Ej. `Country_Code = "COL"`).

### Tarea 2: Cabeceras / Headers Dinámicos Restringidos (CA-55)
*   Crea una pestaña (Tab) visual adyacente al Body Mapping, llamada `[ 🔑 HEADERS DINÁMICOS ]`.
*   Aplica la misma matriz tipo DataMapper pero **prohíbe el texto libre crudo** para el valor (para evitar Header Injection). El destino debe mapearse exclusivamente desde el catálogo de Variables Zod o Macros estáticas del Sistema (`sys.token`, `sys.user_id`).

### Tarea 3: Visualización de Cláusulas Lógicas (CA-53)
*   Si el endpoint de Swagger dicta que ciertos campos son `oneOf` o `anyOf` (o al menos uno obligatorio), envuelve estas filas del DataMapper en un borde semántico unificado que diga: `[ 🔀 Requiere mapear al menos UNO ]`.

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal.
Congela tu código en stash:
`git stash save "temp-frontend-US005-ca51-ca55"`

Informa textualmente la confirmación del guardado.
