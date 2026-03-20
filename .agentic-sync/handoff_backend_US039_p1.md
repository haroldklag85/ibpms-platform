# Handoff Backend - Iteración 40 (US-039: CA-1 a CA-5)

## Propósito
Desarrollar la capa BFF (Backend for Frontend) que sirva como escudo "Anti-Basura" para la Pantalla 7.B (Formulario Genérico), filtrando el ruido del motor BPMN y protegiendo las aprobaciones VIP forzando el uso de formularios maestros.

## Criterios de Aceptación Cubiertos (Backend)
* **CA-1 (Restricción VIP Pre-Flight):** En el Endpoint de instanciación/renderizado de tareas (`BpmTaskService`), inyectar un validador *Pre-Flight*. Si la tarea solicita el formulario genérico (`sys_generic_form`) pero el Rol Asignado pertenece a un grupo protegido ("Alta Dirección", "Aprobador Financiero", etc.), el Backend devolverá HTTP 403 o HTTP 400 exigiendo el mapeo de un Formulario Pro-Code formal.
* **CA-2 (Prevención de Context Bleeding / BFF):** Al enviar el DTO `prefillData` al Frontend para pintar la Pantalla 7.B, el Backend aplicará un DTO de limpieza estricto o un `Whitelist`. Excluirá docenas de variables transaccionales internas de Camunda, retornando SOLO metadatos de negocio vitales (Ej: `Case_ID`, `Client_Name`, `Priority`, `SLA`).

*(Nota: CA-3, CA-4, y CA-5 son de responsabilidad neta del Frontend).*

## Tareas Java (Prioridad 1 - Tarea Bloqueante)
1. Modificar el Servicio de Tareas para implementar el Validador `Pre-Flight` VIP.
2. Construir/Ajustar el DTO de Respuesta de variables de proceso aplicando el Whitelist estricto (BFF Filter).
