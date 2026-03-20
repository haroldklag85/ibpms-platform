# Handoff Frontend - Iteración 40 (US-039: CA-1 a CA-5)

## Propósito
Construir el "Camaleón Operativo": Una Pantalla 7.B capaz de transmutar entre un modo ágil y un modo restrictivo BPMN, implementando borradores locales en caché y mutaciones dinámicas sobre sus esquemas de validación Zod.

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-2 (Renderizado Limpio):** Consumir el DTO purificado del Backend y pintar una grilla superior de "Solo Lectura" ultraligera coronando el formulario.
* **CA-3 (Mutación Camaleónica Dual):** 
    - **Ágil/Kanban:** Mostrar un Slider Numérico `[0-100%]` y botón `[Guardar Progreso]`.
    - **BPMN:** Ocultar el Slider. Mostrar `[Completar Tarea]` y un botón Rojo secundario `[⚠️ Escalar Incidencia / Devolver]`, forzando el lanzamiento de un error a Camunda.
* **CA-4 (Resiliencia Volátil LocalStorage):** Crear un Autosave del progreso del formulario (comentarios, drafts) en el `localStorage` del navegador, atado computacionalmente al `Task_ID`. Al completarse la tarea, purgar esta caché específica.
* **CA-5 (Minimización de Basura TRD):** 
    - Esquema Zod Base: *Opcional*.
    - Si Avance < 100% o Pulsan "Escalar": Zod muta y hace el `TextArea` Obligatorio.
    - Componente `<Dropzone>`: Si el flag externo `[Exigir Evidencia]` está activo, forzar el Dropzone a *Obligatorio* y revelar un `<select>` de "Tipo de Documento" para la Bóveda TRD.

## Tareas Vue (Prioridad 2 - Suspendido hasta Fase 1)
1. Estructurar el Formulario Genérico con Renderizado Condicional (`v-if` modo Agile vs BPMN).
2. Conectar el auto-guardado con `VueUse (useLocalStorage)`.
3. Escribir los Zod Schemas dinámicos condicionales.
