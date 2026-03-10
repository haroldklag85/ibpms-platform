# Agentic Handoff Request: Evaluación CMMN para Instancias Ágiles (US-006 / US-030)

**To:** Lead Software Architect Agent 
**From:** Product Owner (PO)
**Related US:** US-006 / US-030 (Gestión de Proyectos: Hibridación de Plantillas Ágiles)
**Date:** 2026-03-07

## Contexto Funcional
Hemos refinado la historia donde un Scrum Master instancia un Proyecto Ágil (usando una plantilla maestra tipificada de la US-006). En metodología "Tradicional" (Gantt, US-031), usamos Camunda como orquestador clásico BPMN, forzando un camino predecible (Tareas Secuenciales/Paralelas conectadas).

Sin embargo, para entornos Ágiles o iterativos, una Tarea "salta" constantemente entre estados (`ToDo`, `InProgress`, `Blocked`, `Done`) sin un diagrama de secuencias rígido. Durante el refinamiento con el Cliente, surgió la duda técnica de si, para lograr esta flexibilidad anárquica, debíamos sacar esos proyectos del motor de Camunda y hacerlos transaccionales puros en Base de Datos.

## Directriz Estratégica
El Product Owner exige tu peritaje técnico sobre el estándar **CMMN (Case Management Model and Notation)** de la OMG.

## Solicitud de Evaluación (Action Items)
1.  **Kanban Desacoplado:** Evalúa e informa formalmente si Camunda soportando CMMN es el vehículo arquitectónico correcto para gestionar las "Tareas de Backlog" (Agile) donde su ejecución no depende de un camino estructurado de BPMN. 
2.  **Transición de Estado:** Si el desarrollador completa un formulario y la tarea se marca como `DONE` (CA-12 de la US-006), ¿CMMN provee una forma endógena de capturar este *Sentry* para cerrar el Sub-Caso y alimentar la métrica general del Sprint?
3.  **Peso y Complejidad:** El equipo FullStack es nativo de la API BPMN tradicional de Camunda. Introducir CMMN implica una curva de aprendizaje sobre "Sentries" y "Plan Items". ¿La adopción de CMMN representa un *Over-Engineering* (exceso de ingeniería) para un Kanban, o es esencial para mantener toda la auditoría del iBPMS unificada en un solo stack de procesos?

Espero un **Architecture Decision Record (ADR)** con tu resolución sobre CMMN para que la Mesa de Arquitectura apruebe el camino técnico.
