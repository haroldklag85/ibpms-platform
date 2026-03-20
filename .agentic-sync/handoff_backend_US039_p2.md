# Handoff Backend - Iteración 41 (US-039: CA-6 a CA-7)

## Propósito
Culminar el motor del Formulario Genérico inyectando un Event Listener Asíncrono para Aplanamiento de Datos (Data Flattening), indispensable para proveer telemetría hiper-rápida a los Dashboards BAM (Grafana / V2) sin sacrificar el motor transaccional.

## Criterios de Aceptación Cubiertos (Backend)
* **CA-7 (Aplanamiento de Logs Genéricos - Data Flattening):** 
    - Crear una tabla SQL plana (Entity `GenericTaskLogEntity`) con columnas rígidas: `taskId`, `processInstanceId`, `userId`, `comments` (Text), `hasEvidence` (Boolean), `createdAt`.
    - Implementar un Event Listener en Spring Boot (`@EventListener` u homólogo de Camunda *TaskListener* en evento `complete`).
    - Al completar una Tarea Asignada al `sys_generic_form`, el Backend interceptará las variables entrantes, extraerá la observación y la inyectará en esta tabla plana, delegando la carga analítica a este repositorio y protegiendo el historial JSON crudo de Camunda.
* **CA-6 (Namespacing Preventivo - Apoyo al Front):** El Backend recibirá las variables del UI con prefijos (Ej: `TK105_comentarios`). El Backend debe acepatar estas llaves dinámicas y asegurarse de persistirlas correctamente en Camunda sin chocar con variables globales.

## Tareas Java (Prioridad 1 - Tarea Bloqueante)
1. Construir la Entidad y Repositorio de analítica plana `GenericTaskLogEntity`.
2. Implementar el interceptor/listener Asíncrono sobre el completado de tareas genéricas para poblar dicha tabla.
