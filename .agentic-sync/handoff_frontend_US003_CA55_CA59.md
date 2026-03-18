# Contrato de Arquitectura Frontend (US-003 Iteración 12: CA-55 al CA-59)

**Rol:** Desarrollador Frontend Vue 3 / TypeScript (Componentes UI y Lógicas Zod).
**Objetivo:** Desarrollar Layouts Dinámicos Flex/Grid, Candados de UI basados en fórmulas, el Motor de Solo-Lectura (Print Mode) y el Componente de Telemetría (Timer).

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
Debes implementar las siguientes características maestras sobre `FormDesigner.vue` excluyendo requerimientos de V2 explícitamente:

*   **CA-55 (Layout Multicolumna):** Modifica el componente `container`. En su panel de propiedades, permite seleccionar el "Número de Columnas" (1, 2, 3, o 4). Al renderizar el componente (en el template AST), inyéctale dinámicamente una clase de Tailwind Grid (Ej: `grid grid-cols-2 gap-4`) si el número de columnas es mayor a 1, afectando directamente la disposición de sus `children`.
*   **CA-56 (Modo Solo Lectura Plano / Print Mode):** Añade un switch/botón global en la barra superior (Toggle View-Mode). Cuando esté activo, en lugar de pintar inputs deshabilitados (con bordes), el renderizador de Vue (AST) debe pintar componentes netamente de texto Plano (P o SPAN estéticos, sin bordes inputBox) como si fuera un PDF limpio. Si el campo está vacío, pinta "---".
*   **CA-57 (Candado de Solo-Lectura Basado en Fórmulas):** De manera análoga al CA-25 (Visibilidad), añade al panel de propiedades una "Condición de Bloqueo" (`disableCondition` Ej: `formData.ROL === 'INVITADO'`). Si el evaluador devuelve TRUE en tiempo real, inyecta dinámicamente `:disabled="true"` al input correspondiente en el Canvas/AST.
*   **CA-58 (Cronómetro / Timer Component):** Agrega a la Toolbox un componente `Timer`. Éste debe incluir 3 modos en propiedades (Manual Play/Pause, Ticking en Segundo Plano, o Mock API). Almacena el resultado (Ej: 120 segundos) silenciosamente en el `formData` para que viaje a Camunda. Usa `setInterval` controlados por el ciclo de vida de Vue para evitar fugas de memoria.
*   **CA-59 (Reset Dual-Verification):** Ya existe un conato de `showResetModal` en el componente. Formalízalo asegurando que no solo borre el esquema (`canvasFields`) sino que también purgue el reactivo subyacente (`formData`), forzando una re-renderización limpia total. ZodBuilder no debe trabarse.

## 📐 Reglas de Desarrollo:
1. El AST de CA-55 es vital. Un `container` con `columns: 2` debe envolver el `<VueDraggable>` hijo bajo un padre flex o grid CSS estricto.
2. Todo AST Evaluation (CA-57 Condición Disable) debe hacerse de manera segura (No eval directos si el usuario no es Root, de preferencia usando validadores de contexto limpios o Vue computed wrappers para prevenir XSS).

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** comitear el progreso a `main`. 
Cuando compruebes que el View-Mode PDF (CA-56), las Grillas CSS y el Cronómetro en 2do Plano funcionan impecables en tu navegador, pon a salvo el trabajo mediante Stash:
`git stash save "temp-frontend-US003-ca55-ca59"`

Escribe textualmente la validación del Stash a este canal.
