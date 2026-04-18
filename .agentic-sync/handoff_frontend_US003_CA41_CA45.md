# Contrato de Arquitectura Frontend (US-003 Iteración 9: CA-41 al CA-45)

**Rol:** Desarrollador Frontend Vue 3 / TypeScript (Componentes y Formularios Zod).
**Objetivo:** Refinar las iteraciones de arreglos en Zod, sincronizar el formulario con variables de Camunda de etapas anteriores, e incluir el selector avanzado y selector de fecha.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
Debes implementar las siguientes especificaciones sobre `FormDesigner.vue` excluyendo explícitamente cualquier cosa postpuesta para la V2:

*   **CA-41 (Restricciones en Grillas Repetibles - Min/Max Rows):** Expande el componente `field_array` (añadido en la Iteración 7). En las "Propiedades", añade "Mínimo Filas" y "Máximo Filas". En el Zod Generator, mapealo lógicamente: `z.array(z.object({...})).min(X).max(Y)`.
*   **CA-42 (Soporte Multi-Idioma):** **EXCLUIDO POR COMPLETO (Diferido a V2).** No escribir código sobre esto.
*   **CA-43 (Data Binding - Precarga):** Modifica el renderizado (Modo Ejecución). Cuando el Backend responda al inyectar el layout (BFF pattern), extraerás el objeto `prefillData` y harás un mapeo (`onMounted` o inicialización del form) donde rellenarás en `formData` los valores que coincidan con los `names/ids` de los inputs. Esto pre-puebla los campos que ya se llenaron en otras etapas.
*   **CA-44 (Datepicker Estándar Simple):** **Se excluye rangos de fecha (A V2).** Implementa o asegura que la paleta incluya un `<input type="date">` sencillo o su equivalente de TailWind para captura única de fecha (`z.date()` o `z.string()`).
*   **CA-45 (Multi-Select Visual Pastillas/Chips):** Modifica el componente Dropdown (`select`). Si el diseñador marca la propiedad "Múltiple", utilza un mecanismo renderizador en Vue que genere "Pastillas/Chips" para las selecciones acumuladas, permitiendo que el usuario borre individualmente (la 'X' de la pastilla).

## 📐 Reglas de Desarrollo:
1. Recuerda mantener un "Cero-Trust" sobre la integración. Si `prefillData` por el backend viene nulo, el form se inicializa vacío sin lanzar Excepciones front.
2. Con CA-45, si estás usando `<select multiple>`, envuélvelo en UI custom, ya que el multiselect nativo no renderiza pastillas de forma linda.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** comitear el avance a la rama principal (main).
Cuando logres un visual funcional para Pastillas, Fechas y DataBinding, almacénalo usando:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Informa a tu oficial superior apenas el rescate en stash finalice.
