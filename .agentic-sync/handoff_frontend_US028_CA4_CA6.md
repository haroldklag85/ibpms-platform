# Handoff Frontend - Iteración 32 (US-028: CA-4 a CA-6)

## Propósito
Robustecer el Sandbox Zod In-Browser para soportar exclusión total de llamadas de red (Zero-Network), autogeneración de DataGrids (Anidación Recursiva) y selector dinámico de fases para iForms Mutantes.

## Criterios de Aceptación Cubiertos
* **CA-4 (Aislamiento Zero-Network):** Al lanzar la simulación, el sistema bloqueará proactivamente cualquier intento de `fetch`/`axios` y no disparará ciclos de vida (ej. `onMounted` que jale Data Sources). Evalúa pura y rígidamente `zod.safeParse()`.
* **CA-5 (Fuzzing Ciego Recursivo):** El Generador Dummy debe rellenar colecciones `z.array().min(X)`. Si un Grid exige mínimo 2 filas, autogenera las 2 filas inyectando strings/números estáticos básicos sin exigir esfuerzo manual al desarrollador.
* **CA-6 (Dropdown Dinámico Etapas):** Integrar un menú `[ 🎭 Etapa a Simular 🔻 ]` en el Sandbox. Al cambiarlo, el Vue State muta reactivamente el Zod Schema mostrando (o ignorando) las validaciones condicionales y se regenera el Fuzzing Payload para esa etapa específica.

## Directrices Arquitectónicas V1
1. **Poder del Cliente:** La lógica se computa en memoria local.
2. **Restricción V2 ANTI-IA:** QUEDA TERMINANTEMENTE PROHIBIDO invocar modelos LLM para predecir las filas del DataGrid. Utilice recursividad determinista estándar (Fuzzing Tipado).

## Tareas Frontend
1. Implementar bandera reactiva en el modo Sandbox para bypassear llamadas HTTP nativas del componente si aplica.
2. Refactorizar la función `generateFuzzingPayload()` para que se llame a sí misma de forma recursiva al detectar Arrays.
3. Incrustar Dropdown de Etapas en la interfaz del Sandbox y engancharlo a la mutación dinámica del `zodSchema`.
