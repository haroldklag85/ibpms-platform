# Handoff Frontend - Iteración 41 (US-039: CA-6 a CA-7)

## Propósito
Finalizar la funcionalidad operativa del Formulario Genérico inyectando un algoritmo de Namespacing estricto antes del envío del payload, evitando colisiones ('Overwrites') de memoria en el motor BPMN.

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-6 (Namespacing Preventivo):** 
    - Al oprimir los botones `[Guardar Progreso]` o `[Completar Tarea]`, el método empaquetador iterará el objeto Reactivo del formulario.
    - El Frontend obligatoriamente mutará las llaves JSON, concatenando el ID de la Tarea al inicio de cada atributo. 
    - *Ejemplo:* Si el `task.id` es `XYZ99`, la propiedad `comentarios: "Aprobado"` mutará a `XYZ99_comentarios: "Aprobado"`.
    - Esto previene que una tarea paralela sobre-escriba variables globales en el mismo proceso de Camunda, a la vez que permite su mapeo explícito en futuras pantallas de Integración (Pantalla 11).

* **CA-7 (Data Flattening):** Transparente para Vue. Vue envía el payload namespaceado y el backend hace el aplanamiento.

## Tareas Vue (Prioridad 2 - Suspendido hasta Fase 1)
1. Modificar el método de Submit de `TaskViewerModal.vue` o del Formulario Genérico Base.
2. Inyectar la función de interpolación (Prefixing) dinámica usando el `TaskId` sobre las llaves del DTO de salida hacia Axios.
