# Análisis Funcional Definitivo: US-030 (Instanciar y Planificar Proyecto Ágil)

## 1. Resumen del Entendimiento
La US-030 opera como la interfaz logística (Pantalla 9 y 10) donde los Scrum Masters o Agile Coaches aterrizan el molde de una Plantilla de WBS (US-006) y la materializan en un tablero operativo real para la ejecución en vivo del equipo de desarrollo. 

## 2. Objetivo Principal
Proveernos de un "Hub Ágil" donde confluyen los tickets heredados, permitiendo asignar las tarjetas del trabajo no estructurado para que los equipos las ataquen en la Pantalla 3 (Kanban). 

## 3. Alcance Funcional Definido
**Inicia:** Con la creación (Instanciación) de un proyecto marcado de tipo "Ágil".
**Termina:** Con la disponibilidad inmediata del Backlog en el tablero Kanban.

## 4. Lista de Funcionalidades Incluidas
- **Diferimiento Estratégico (CA-1):** Decide postergar la concepción purista de "Sprints" / Timeboxes para V2 del producto. En su lugar, el sistema instanciará un lienzo "Tablero Kanban Continuo" donde el Backlog entero está servido.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Huérfanos de Backlog (⚠️ CA-1):** Al prescindir explícitamente de "Sprints" e inyectar *todo* el Backlog a un "Kanban Continuo" sin Timebox, si un Scrum instanció una plantilla de 500 tareas, la vista operativa de ejecución podría colapsar visual y transaccionalmente (DOM Density) la Pantalla 3 del empleado. El "Kanban Continuo" sin mecanismo nativo de "Icebox/Backlog Parking" abrumará los motores de paginación del frontend al cargar un tablero gigantesco de golpe.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Marcos formales de Scrum (Sprints, Puntos de Historia, Burndown-Charts, Retrospectivas) aplazados a V2.

## 7. Observaciones de Alineación o Riesgos
**Riesgo Menor:** Historia extremadamente corta y declarativa pero crucial para evitar un Scope Creep masivo integrando el rito Scrum entero. Funciona como una exención directiva brillante para MVP, a expensas de requerir estricto control de paginación o filtrado "en cola".
