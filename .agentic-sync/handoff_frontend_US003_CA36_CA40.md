# Contrato de Arquitectura Frontend (US-003 Iteración 8: CA-36 al CA-40)

**Rol:** Desarrollador Frontend Vue 3 / TypeScript (Configuración Avanzada de Inputs UI).
**Objetivo:** Desarrollar los refinamientos de usabilidad para campos paramétricos y la vista estática de auditoría visual.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
Tu responsabilidad abarca las siguientes adiciones al `FormDesigner.vue` y su renderizado:

*   **CA-36 (Máscaras de Entrada - Input Masks):** Agrega soporte en el panel de propiedades para "Máscara de Formato" sobre campos de texto/número. Inyecta visualmente el formato (ej. moneda) en Vue, pero garantizando que el `formData` serialice el dato crudo (entero/flotante).
*   **CA-37 (Visor Histórico Inmutable para Auditoría):** Crea o adapta un flag genérico (`isAuditMode` o inyectado vía `stage === 'AUDIT'`) que congele el formulario por completo: Inyecta `readonly` y `disabled` a todos los inputs, oculta los [Smart Buttons] de envío/guardado y elimina bordes interactivos.
*   **CA-38 (Restricciones de Longitud Dinámicas Zod):** Encampos de tipo `textarea` o `text`, añade inputs en el panel de propiedades para "Caracteres Mínimos" y "Máximos". Modifica el parser `generateFieldHTML` y Zod Generator para concatenar dinámicamente `.min(X)` y `.max(Y)`.
*   **CA-39 (Condicionamiento de Archivos Adjuntos):** En componentes `file` (Upload), añade propiedades "Peso Máximo (MB)" y "Tipos Permitidos (.ext)". Genera validación proactiva *antes* de enviar la petición Axios al Backend.
*   **CA-40 (Dropdown de Búsqueda Interactiva):** Remplaza o extiende la macro HTML del campo `select` convencional para usar un componente tipo Typeahead interactivo (Ej: integrando `v-select` si está en el package.json, o simulando un datalist nativo acoplado a un `input type="search"`), que permita filtar opciones largas escribiendo.

## 📐 Reglas de Desarrollo:
1. Respeta el AST actual. Toda nueva propiedad (min, max, ext) debe serializarse bien en la interfaz `FormFieldMetadataDTO` y guardarse en `canvasFields`.
2. Las validaciones de CA-39 deben interceptar el `@change` del archivo, y usar el mecanismo de Toasts/Alertas en pantalla en caso de violación de regla en lugar del envío directo.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer un `git commit` directamente. 
Codifica el AST reactivo, y luego protege el avance almacenándolo aisladamente en stash:
`git stash save "temp-frontend-US003-ca36-ca40"`

Avisa únicamente cuando el comando Stash haya concluido exitosamente.
