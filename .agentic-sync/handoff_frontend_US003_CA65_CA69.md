# Contrato de Arquitectura Frontend (Iteración 14: Shift-Left QA Auto-Generador)

**Rol:** Desarrollador Frontend Vue 3 / TypeScript.
**Objetivo:** Implementar la **US-028**, anidada al final del FormDesigner, para generar y descargar dinámicamente archivos `.spec.ts` (Vitest) basados en el AST.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
Al analizar los requerimientos, los CA-65, CA-66 y CA-67 originales están totalmente diferidos a V2 (Modo Oscuro, Monedas, WYSIWYG). Por tanto, esta iteración orquesta el componente colindante vital que finaliza temporalmente el alcance IDE:

*   **Generador BDD Automático:** Añade un botón en la cabecera (Header) de `FormDesigner.vue` denominado `[⚡ GENERADOR DE TESTS]`.
*   **Lógica de Cobertura Zod:** Al hacer clic, debes recorrer el arreglo actual `canvasFields.value` y concatenar un String GIGANTE que represente un archivo TypeScript válido (`.spec.ts`).
*   **Aislamiento de APIs (Mocks Boilerplate):** Si en el AST hay campos con llamadas asíncronas (`isAsync: true` o `asyncUrl`), inyecta en el String resultante bloques de código Vitest con `vi.mock('axios')` o fetch mocks pre-escritos.
*   **Paths de Testing (Happy/Sad):** El String resultante debe contener mínimo dos bloques `it(...)`: Uno pasando un objeto llenado con datos falsos básicos (100% de éxito en Zod) y otro con un payload vacío `{}` esperando un `ZodError` por campos obligatorios.
*   **Descarga Silenciosa (Blob):** Finalmente, instancia un `new Blob([testFileString], { type: 'text/typescript' })`, crea un `URL.createObjectURL` e inyecta un tag `<a>` temporal en el DOM para forzar la descarga del archivo físico a la máquina del Arquitecto.

## 📐 Reglas de Desarrollo:
1. Asegúrate de que el código generado importe adecuadamente `Zod` y exponga la estructura clara de una Suite de Pruebas.
2. El Blob debe nombrarse dinámicamente (Ej. `formulario_autogen.spec.ts`).

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal. 
Implementa estas macros avanzadas del AST Generador Zod y, tras verificar que la descarga arroje un archivo de prueba funcional, guarda todo:
`git stash save "temp-frontend-US003-ca65-ca69-QA"`

Escribe textualmente la comprobación del comando en pantalla cuando finalices.
