# Contrato de Arquitectura Frontend (US-003 Iteración 6: CA-26 al CA-30)

**Rol:** Desarrollador Frontend Vue 3 / TypeScript (Manejo Avanzado de AST y UI reactivo).
**Objetivo:** Integrar features avanzados de Micro-ERP como la recolección Typeahead API o importaciones CSV in-memory.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta iteración debes habilitar la creación rápida de catálogos y versionar el Canvas.

Debes abordar los siguientes Criterios (CA):
*   **CA-27 (Historial UI):** Agregar un botón o panel lateral secundario "Historial de Versiones" que consuma `GET /api/v1/forms/{id}/versions` (recién creado por Backend).
*   **CA-28 (Auditoría Forense Config):** Incluye una bandera `enableAuditLog: boolean` en la metadata de todos los campos en el properties modal. Al habilitarse, el AST generará HTML asumiendo la inyección de la huella del usuario `{{ currentUser.name }}` en un texto chico de auditoría.
*   **CA-29 (Importación Dinámica Dropdown CSV):** En la configuración del componente tipo `select`, añade un lector in-memory (File Reader HTML5). El diseñador puede adjuntar un archivo .csv ("valor,etiqueta") y tú transformarás el array crudo en `element.options`.
*   **CA-30 (Axios Async Typeahead API):** Agrega un nuevo componente `async_select` o modifica `select`. El usuario proveerá una `urlBase`. Tu compilador AST debe generar un input que, en `@input`, haga `await fetch(urlBase + '?q=' + query)` y rellene el Typeahead.

## 📐 Reglas de Desarrollo:
1. Sigue operando en `FormDesigner.vue`. Las modificaciones a `generateFieldHTML()` y Zod son acumulativas; cuida las expansiones hechas en Fases previas (v-if, :disabled, etc).
2. **Cero implementaciones triviales:** Tu código AST Vue generado debe correr como un script válido `<script setup>`.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** ejecutar un `git commit` permanentemente.
Sigue el procedimiento de Zero-Trust Git guardando tus archivos modificados temporalmente en el caché para inspección superior:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Apenas termines y valides gráficamente, repórtate a la jefatura.
