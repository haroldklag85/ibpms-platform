# Contrato de Arquitectura Frontend (US-003 Iteración 7: CA-31 al CA-35)

**Rol:** Desarrollador Frontend Vue 3 / TypeScript (Experto AST).
**Objetivo:** Desarrollar los módulos core funcionales faltantes del IDE FormBuilder (Firmas en lienzo, Exportación, Grillas repetibles, Tooltips visuales).

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
Esta es una fase intensiva de Frontend. Tu labor en `FormDesigner.vue` abarca lo siguiente:

*   **CA-31 (Firma Electrónica Manuscrita):** Añade un componente `signature` a la Paleta. En renderizado, dibuja un `<canvas>` HTML5 interactivo que, al mover el cursor y cerrarse/guardar, emita un string Base64 (`canvas.toDataURL()`) enlazado a `v-model`. Define su Zod como `z.string()`.
*   **CA-32 (Validaciones Cruzadas Múltiples):** Complejo. Extiende el Generador de Zod (en el Panel). Si se configuran reglas cruzadas (Ej: `Campo A > Campo B`), modifica tu generador de texto Zod para inyectar una cadena encadenada al final del schema global: `.superRefine((data, ctx) => { if (data.A > data.B) { ctx.addIssue(...) } })`. Evita mocks, el AST lo debe redactar.
*   **CA-33 (Exportación a PDF):** Añade un botón Global "[Exportar PDF]" en la barra superior. Si puedes, invoca `window.print()` (basado en reglas de media CSS `@media print` que oculten botones) o usa `html2pdf.js` si está disponible, permitiendo descargar el diseño lleno en PDF limpio.
*   **CA-34 (Grupos de Campos Repetibles - Data Grids):** Expande la lógica del layout `container`. Crea un tipo especial de layout `field_array` que en inyección genere un `<div v-for="(row, index) in formData.myArrayVar">` e incluya botones `[+ Agregar Fila]`. El generador de Zod deberá anidar: `mi_grilla: z.array(z.object({ ... }))`.
*   **CA-35 (Ayudantes Locales: Tooltips y Placeholders):** Asegúrate de que el modal de Propiedades Avanzadas (`editingField`) capture la cadena `tooltipText` y `placeholder`. Al compilar el Template vue, si un campo posee  `tooltipText`, al lado del nombre (Label) inyectale un ícono de info (ⓘ) y usa soporte de 'title' html5 genérico o tu componente global `AppTooltip`.

## 📐 Reglas de Desarrollo:
1. Protege obsesivamente el funcionamiento dual `generateFieldHTML()` y tu parseador in-memory AST Bidireccional. Todo debe seguir interactuando en tiempo real con Mónaco IDE.
2. Si te complicas mucho con Mónaco y `Zod.superRefine()`, haz una aproximación estable. **Lo primero es asegurar que compile.**

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer un `git commit` directamente. 
Asegúrate de que la interfaz corre, edita y la firma se puede trazar con el mouse. Luego detente, guarda todo, y cierra:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Retorna el reporte de finalización al Arquitecto Humano.
