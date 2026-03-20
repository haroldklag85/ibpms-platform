# Handoff Frontend - Iteración 42 (US-043: CA-1 a CA-6)

## Propósito
Construir la "Pantalla 19" (Matriz SLA de Negocio): Una interfaz paramétrica administrativa donde la PMO configure las reglas de tiempos corporativos, husos horarios y asuetos.

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-3 (Modal Anti-Deadlock):** 
    - Crear el formulario de "Cambiar Rango de Horas Hábiles".
    - Incluir el Toggle Switch `[Aplicar Retroactivamente a Tareas Vivas]`.
    - Al presionar Submit, interceptar la respuesta HTTP 202 (Accepted) y mostrar un Modal Informativo / Toast: *"Recálculo masivo en progreso. Los SLAs vivos se actualizarán gradualmente"*.
* **CA-5 (Grid de Feriados Fallback):** 
    - Diseñar un módulo visual tipo "Calendario Anual" o Grid (CRUD de `HolidayEntity`).
    - Permitir a la PMO agregar fechas manualmente (Ej: "1 de Mayo", "Reunión Corporativa") en las que el reloj SLA se congelará para todos los procesos u oficinas regionales.
    - El Frontend es responsable de proveer esta UI para el Fallback si las APIs automáticas gubernamentales no operan.
* *(Nota V2: Las demás validaciones arquitectónicas residen netamente en Java/Camunda).*

## Tareas Vue (Prioridad 2 - Suspendido hasta Fase 1)
1. Construir la ruta administrativa SLA Settings.
2. Renderizar los inputs horarios y el Grid Manual de Festivos.
3. Conectar a los nuevos endpoints CRUD de `BusinessHours` y `Holidays` provistos por Backend.
