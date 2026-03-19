# Handoff Frontend - Iteración 31 (US-028: CA-1 a CA-3)

## Propósito
Implementar el Sandbox de QA In-Browser para validar esquemas Zod en el Diseñador de Formularios (Pantalla 7), sin depender de servidor ni generar archivos locales.

## Criterios de Aceptación Cubiertos
* **CA-1: Ejecución Interna In-Browser (Zero Dead Code):** Botón `[🧪 SIMULAR CONTRATO ZOD]`. No se descargan `.spec.ts` para esta prueba viva. Panel de consola QA Split View. Valida objeto Zod en memoria reactiva.
* **CA-2: Boundary Testing Pragmático y Ciego:** Generar Payload Dummy in-memory basado en Zod. Path Feliz (Payload válido -> success: true). Path Triste (Payload vacío {} -> Error Required). Sin lógica semántica de V2.
* **CA-3: Modificación Manual del Mock Payload:** Mini-editor en la consola QA para editar JSON en caliente. Validación `.safeParse` instantánea.

## Directrices Arquitectónicas
1. **Lógica 100% Frontend:** Todo ocurre en memoria Ram del navegador.
2. **Dependencias Autorizadas:** Uso de `zod` (`safeParse`) y generadores estáticos simples para el fuzzing basados en los tipos requeridos, sin llamadas de red a LLMs.
3. **Restricción V2:** PROHIBIDO utilizar inferencia semántica o inteligencia artificial para generar el payload. Debe ser *Type-Based Fuzzing* ciego (Ej. "AAAAA" para strings, 1 para números).

## Tareas Frontend
1. Modificar interfaz del Builder de Formularios (Pantalla 7) para agregar el Split-Panel (Panel de Diseño vs Panel QA Sandbox).
2. Botón `[🧪 SIMULAR CONTRATO ZOD]` que despliega la Consola QA.
3. Implementar un generador de Mock básico recursivo que lea las llaves del Zod Schema en memoria y produzca un JSON dummy base (String, Number, Enum).
4. Implementar mini editor JSON reactivo (`<textarea>` o equivalente).
5. Evaluación en caliente: watcher sobre el texto editado que corra `schema.safeParse(jsonPadeado)` y pinte el listado de errores Zod o un bloque de éxito.

## Criterios de Aceptación NO cubiertos (Excluidos explicitamente)
* CA-4 al CA-8 se abordan en la siguiente iteración.

---
**Nota Global de Agente:** Absténgase de invocar comandos de Backend, LLMs, RAG o cualquier IA generativa para generar el JSON Dummy. Respete estrictamente el alcance V1.
