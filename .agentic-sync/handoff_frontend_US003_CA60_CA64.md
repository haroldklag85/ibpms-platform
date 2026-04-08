# Contrato de Arquitectura Frontend (US-003 Iteración 13: CA-60 al CA-64)

**Rol:** Desarrollador Frontend Vue 3 / Vite (Tailwind UI y Device APIs).
**Objetivo:** Conectar el FormDesigner a las APIs nativas del navegador (HTML5 Hardware) e incrementar la calidad del QA Shift-Left en tiempo de escritura.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
Debes implementar las siguientes características en `FormDesigner.vue` excluyendo rigurosamente requerimientos de V2 explícitos en el roadmap:

*   **CA-60 (Arrastrar y Soltar Adjuntos):** Evoluciona el input tipo `file`. Envuelve el renderizado del input en un contendor Dropzone (Ej: `@drop.prevent="handleDrop" @dragover.prevent`). Al soltar N archivos, la lógica debe atrapar `e.dataTransfer.files` y poblar el estado en UI para simular el uploader masivo y amigable.
*   **CA-61 (Geolocalización GPS Embebida):** Añade el componente "GPS" a la toolbox. Su HTML generado en el AST es un campo de Solo Lectura y un Botón "[📌 Capturar GPS]". Al pulsar, evalúa en tiempo real `navigator.geolocation.getCurrentPosition` e inserta el String `Lat: {l}, Lng: {l}` en el `formData[key]`.
*   **CA-62 (Escáner Código QR):** Añade el componente "QR Scanner". Su HTML es un campo de Texto y un Botón "[📷 Escanear]". Para V1, mockea el WebRTC haciendo que al pulsar el botón, despierte un `prompt()` o inyecte una cadena estática/dummy de "QR-MOCK-7788" en el input (a menos que prefieras importar una librería ligera externa para WebRTC que compile limpio).
*   **CA-63 (Auto-Validación Mágica Zod para Email/URL):** Añade en la ToolBox "Tipo: Email" y "Tipo: URL". En la función generadora del SCRIPT (fase AST), el Zod inyectado debe ser obligatoriamente `z.string().email()` o `z.string().url()` según aplique, inyectando feedback Zod nativo para la validación visual de la sintaxis.
*   **CA-64 (Hints Multi-Estado):** Agrega a los inputs (si el tipo es password o si configuraste regex complejas) una serie de `<p>` dinámicos debajo del campo que validen en vivo las reglas (Ej: Tiene mayúscula ❌/✅, Larga ❌/✅) basado puramente en un computed hookeado a `formData[key]`.

## 📐 Reglas de Desarrollo:
1. Recuerda que la filosofía Gatekeeper evalúa código limpio; los eventos HTML5 como el `.geolocation` deben estar envueltos en bloques `try...catch` asegurando fallbacks amigables (Ej: Mostrar alerta "Permiso de ubicación denegado" en el DOM).
2. Para el CA-64, la validación multiestado es crucial para reducir frustración del cliente bancario/financiero. Inyéctala sabiamente en el SCRIPT del AST.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** comitear el progreso nativo a `main`. 
Cuando compruebes que el Canvas obedece las API del dispositivo (GPS), las zonas de arrastre son funcionales (Dropzone CSS), y los Zods se generan blindados, congela tu trabajo:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Escribe textualmente la comprobación de ese comando Git a este canal apenas termines de programarlo todo.
