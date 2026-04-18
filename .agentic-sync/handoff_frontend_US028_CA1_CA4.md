# Contrato de Arquitectura Frontend (Iteración 14 - US-028: Auto-Generador QA Vitest)

**Rol:** Desarrollador Frontend Vue 3 / TypeScript.
**Objetivo:** Implementar la **US-028** (Criterios de Aceptación 1 al 4) para generar y descargar dinámicamente archivos `.spec.ts` (Vitest BDD) basados en el AST.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
A continuación, los 4 escenarios de cumplimiento obligatorio sobre `FormDesigner.vue`:

*   **CA-1 (Cobertura Lógica Pura):** Añade un botón prominente en la cabecera (Header de herramientas) denominado `[⚡ GENERADOR DE TESTS]`. Al pulsarlo, no debes tocar el DOM interactivo ni dependencias de Vue. El script generado debe focalizarse EXCLUSIVAMENTE en aislar el modelo `zodSchema` y testear los payloads contra el método `parse()`. 
*   **CA-2 (Aislamiento de APIs - Mocks Boilerplate):** Si durante la iteración del `canvasFields.value` detectas campos complejos o asíncronos (`asyncUrl` o similares), debes inyectar en el `.spec.ts` las líneas de Mock globales (Ej: `vi.stubGlobal('fetch', ...)` o fragmentos de MSW genéricos) devolviendo un `{ status: 200, data: [] }` para que el QA los rellene manualmente. La red real debe estar bloqueada por el autogenerado.
*   **CA-3 (Boundary Testing: Happy vs Sad Extremo):** El String resultante debe emitir una estructura de bloque `describe('Formulario Mapeado')` conteniendo exactamente DOS aserciones: 
    * 1. Path Feliz 100%: Declarando un objeto JS ficticio con llaves/valores dummy que simule una captura perfecta.
    * 2. Path Triste: Declarando una aserción `expect(() => schema.parse({})).toThrow(ZodError)` enviando un objeto nulo para verificar que se rompen las dependencias obligatorias y saltan las validaciones 400.
*   **CA-4 (Descarga Pasiva Blob):** Con el String gigante del script creado en su totalidad, forjar en memoria un `new Blob([testFileString], { type: 'text/typescript' })` y emular vía hipervínculo oculto la descarga nativa hacia el disco del operario, nombrando el archivo algo como `form_payload.spec.ts`.

## 📐 Reglas de Desarrollo:
1. El script autogenerado es una larga concatenación de strings o literales de plantilla. Modela el `.spec.ts` resultante con alta legibilidad estética para el desarrollador humano (saltos de línea y tabulaciones correctas).

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal. 
Implementa la lógica del Blob Auto-Generador. Una vez compruebes que al hacer clic se descarga tu primer script `.spec.ts` funcional, congela todo en un stash:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Escribe textualmente el comando de éxito cuando finalices.
