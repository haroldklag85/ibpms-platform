# Handoff Frontend - Iteración 33 (US-028: CA-7 a CA-9)

## Propósito
Pulir la experiencia y rigor de evaluación del QA Sandbox Zod embebido, otorgándole visibilidad absoluta sobre las transformaciones de datos y un parseo amigable a nivel humano de los errores Zod.

## Criterios de Aceptación Cubiertos
* **CA-7 (Visibilidad Dual Absoluta):** Subdividir el contenedor de pruebas en dos sub-paneles: 1. `[📥 Payload Crudo]` (editable) 2. `[📤 Payload Parseado]` (solo lectura). El segundo debe imprimir en vivo el resultado exitoso del `zod.safeParse().data`, donde el QA confirmará si Zod mutó exitosamente el input (Ej: eliminó de los espacios, casteó fechas a ISO, coercionó números, ignoró llaves basura).
* **CA-8 (Bloqueo Sincrónico Simple):** En V1 queda PROHIBIDO aislar validaciones pesadas en Web Workers para "no bloquear el hilo". Zod usa `.safeParse()` síncrono. Bloquear el hilo de JS microsegundos es lo esperado.
* **CA-9 (Errores Human-Readable):** Al fallar `safeParse` (Path Triste `zod.error`), prohibido disparar el Array ZodIssue crudo. FrontEnd debe destripar la traza y renderizarla en listas viñetadas con formato: `❌ [llave.ruta] - Mensaje Zod.` 

## Directrices V1 
El código de ZodError parsing y el Layout Dual no requieren asistencia de backend ni peticiones generativas hacia LLMs. Operación clásica y controlada al 100% en Vue/CSS.

## Tareas Frontend
1. Re-maquetar el cuadro principal de la consola QA del Formulador y proveer un Dual-Grid para acomodar "Payload Input" vs "Parsed Data Output".
2. Atar los bloqueos `watch` directamente sin promesas ni hilos asíncronos paralelos (`async/await z.parseAsync` se omite si no asiste conectores externos locales).
3. Construir una función `formatZodIssues` ligera que ensamble los paths de error a strings viñetados amistosos y pintarlos usando `v-for`.
