# Handoff Frontend - Iteración 45 (US-038: CA-6 a CA-10)

## Propósito
Otorgar soporte visual a la segregación de roles y la consolidación de tareas de los usuarios Multi-Rol, brindando advertencias sobre qué credencial se está empleando para cada trámite.

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-6 (Bypass Visual de SoD):** 
    - Si el originador de un proceso detecta una tarea subsecuente re-asignada a él por error en su Bandeja, no bloquear el renderizado visual, sino esperar a que el Backend devuelva el `HTTP 403 (Conflicto de Interés)` al intentar completarla, mapeando el Toast: *"No puede procesar una tarea en una instancia que originó"*.
* **CA-10 (Insignia de Procedencia Multi-Rol):** 
    - **Target:** `Workdesk.vue` (o la vista principal del Bandejón).
    - En la grilla o las tarjetas de tareas de Camunda, cada Item debe decodificar a qué variable/grupo candidato correspondía la asignación.
    - Se debe renderizar un Componente de Insignia (`<VChip>`, `<Badge>`) que diga visualmente el Rol Exigido (Ej: `Rol: Aprobador_Financiero`).
    - Propósito: El analista asume múltiples roles, la insignia disipa la confusión cognitiva sobre por qué le llegó un proceso de otra área.

## Tareas Vue (Prioridad 2 - Puede ejecutarse en Paralelo al Backend)
1. Inyectar el Badge (Insignia de Rol) en las columnas / cards del Workdesk.
2. Interceptar el error `HTTP 403` de SoD (Conflicto de Interés) para presentar un Alert descriptivo.
