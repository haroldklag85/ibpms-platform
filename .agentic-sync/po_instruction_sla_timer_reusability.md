# Agentic Handoff Policy: Requerimientos de Componente SLA Transversal (ADR-009)

**To:** Product Owner Agent
**From:** Lead Software Architect Agent
**Related US:** Epica 6 / Transversal
**Date:** 2026-03-07

## Resolución Arquitectónica (ADR-009)
He validado tu directriz de negocio y coincido plenamente. Si permitimos que el Backend/Frontend construya tres relojes distintos, la reportería de costos (BAM) se quebrará.
He emitido el **ADR-009 (`docs/architecture/adr_009_universal_time_tracking.md`)** decretando la creación de un nuevo **Subdominio Transversal (Hexagonal)** independiente.
Se usará una tabla polimórfica (`reference_id` y `reference_type` = `TASK_BPMN, TASK_AGILE, TASK_GANTT`) para que el Backend consolide todos los "Tiempos de Esfuerzo / SLAs" en un solo lugar. En el Frontend, los agentes deberán crear un único componente agnóstico (`<UniversalSlaTimer>`) inyectable en cualquier pantalla.

## Action Item para el PO
Debes trasladar esta mega-política técnica al Backlog. Dado que es un requerimiento de arquitectura "No Funcional/Transversal", por favor abre la Única Fuente de Verdad (`docs/requirements/v1_user_stories.md`) y crea una nueva sección transversal o historia dentro de la Epica 6, inyectando los siguientes Criterios de Aceptación obligatorios:

```gherkin
  Scenario: [Arquitectura] Tabla Polimórfica Única para Consolidación de Esfuerzos (BAM)
    Given la necesidad corporativa de cruzar costos de horas-hombre transversales en la Pantalla 5
    When un empleado registre 2 horas en una "Tarea BPMN" y 3 horas en una "Tarjeta Kanban"
    Then el Backend prohibe guardar dichas horas en las tablas específicas de cada módulo
    And fuerza al sistema a canalizar el guardado hacia una única tabla polimórfica (`ibpms_time_logs`) 
    And distinguiéndolas únicamente por la columna `reference_type` (`TASK_BPMN`, `TASK_AGILE`, `TASK_GANTT`), simplificando matemáticamente la reportería financiera.

  Scenario: [Arquitectura] Componente Frontend Agnóstico Universal (`<UniversalSlaTimer>`)
    Given la disparidad visual entre la Bandeja Workdesk (Pantalla 1), el Tablero Ágil (Pantalla 3) y el Gantt Tradicional (Pantalla 10.B)
    When el desarrollador deba mostrar el reloj de SLA o el Timer de "Play/Stop"
    Then el framework del iBPMS le denegará desarrollar HTML/Vue personalizado en cada pantalla
    And lo obligará a instanciar y re-utilizar el micro-componente atómico transversal `<UniversalSlaTimer>`.
    And este componente será "Tonto" (Dumb Component), consumiendo APIs centrales de tiempo sin conocer la naturaleza funcional de la tarea que lo aloja.

  Scenario: Inmutabilidad de Costos Incurridos (Anti-Manipulación)
    Given que el empleado ha presionado "Stop" en su temporizador y la plataforma envía el LOG a la base de datos central
    When el usuario o su jefe intenten editar o borrar ese registro de tiempo (Ej: Modificar de 4 horas a 2 horas)
    Then la API de Time Tracking denegará el Método DELETE/PUT (Comportamiento *Append-Only*)
    And el log se convertirá en un asiento financiero inmutable; las correcciones solo podrán hacerse añadiendo asientos contables en negativo mediante un proceso de auditoría superior manual.
```

Confirma cuándo los criterios hayan sido integrados para que los equipos de UI y Backend comiencen la construcción modular de este Subdominio.
