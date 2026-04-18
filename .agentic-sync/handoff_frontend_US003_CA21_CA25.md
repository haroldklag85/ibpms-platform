# Contrato de Arquitectura Frontend (US-003 Iteración 5: CA-21 al CA-25)

**Rol:** Desarrollador Frontend Vue 3 / TypeScript (Experto en Composition API y AST generation).
**Objetivo:** Extender el IDE Mónaco y el Canvas Visual para implementar Auto-Guardado, Upload Nativo y la Visibilidad Condicional (Lógica de Ramificación).

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta iteración debes consumir los endpoints reales creados concurrentemente por el Backend y alterar significativamente la macro de compilación Vue.

Debes abordar los siguientes Criterios (CA):
*   **CA-21 (Conector Multipart File Upload):** El componente `type="file"` ya existe en el Toolbox y se renderiza visualmente. Modifica el Generador HTML (AST) de `FormDesigner.vue` genérico para que en la vista en vivo emita un `@change` que dispare una subida real mediante `FormData` consumiendo `POST /api/v1/forms/upload`. ¡Cero Mocks!
*   **CA-24 (Auto-Guardado Workdesk LocalStorage/API):** Implementa un `watch` profundo sobre `canvasFields` y `formData` en el estado global visual de la plataforma. Usa un `debounce(2000ms)` para postear silenciosamente hacia `POST /api/v1/forms/draft` (o guárdalo en LocalStorage como fallback resiliente).
*   **CA-25 (Visibilidad Condicional V-If Dinámico):** El hito más difícil. Agrega al Properties Modal (Modal de ⚙️) una caja de texto llamada `Condición de Visibilidad Eval`. Si el usuario rellena esta caja (ej. `formData.country === 'COL'`), modifica la macro `generateFieldHTML()` para que inyecte un `<div v-if="formData.country === 'COL'">` recubriendo a ese campo. Modifica la interfaz `FormFieldMetadataDTO` apropiadamente.

## 📐 Reglas de Desarrollo:
1. No destruyas la directiva `:disabled` (CA-20) generada previamente ni el recursor `flatFields()`. Todo esto es incremental.
2. **CERO MOCKS PERMITIDOS:** Todo `apiClient.post` debe apuntar a las rutas estipuladas.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal. 
En cuanto tu código Vue, Stores y el IDE Zod funcionen armónicamente en caliente (HMR), empaqueta silenciosamente tu contribución ejecutando esta línea estricta en la terminal:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Al terminar el Stash, notifica tu éxito al superior.
