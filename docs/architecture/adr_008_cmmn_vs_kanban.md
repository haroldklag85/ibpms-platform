# ADR-008: Gestión de Casos CMMN vs Transaccionalidad Pura (Kanban Ágil)

**Status:** Aprobado
**Date:** 2026-03-07
**Context:** US-006 / US-030 (Gestión de Proyectos Ágiles / Kanban)
**Autor:** Lead Software Architect

## 1. Contexto y Problema
El Product Owner ha escalado una duda arquitectónica sobre los Proyectos "Ágiles" (Kanban). A diferencia de los proyectos tradicionales (Gantt - US-031) que siguen un diagrama BPMN secuencial rígido, las tarjetas en un Kanban (ToDo -> InProgress -> Blocked -> Done) no tienen un orden determinista. 
Se consulta si la plataforma debe utilizar el motor **CMMN (Case Management Model and Notation)** nativo de Camunda 7 para orquestar este caos, o si representa un exceso de ingeniería (*Over-Engineering*).

## 2. Decisión Arquitectónica: RECHAZO ROTUNDO DE CMMN
Se dicta la prohibición técnica de utilizar hojas `.cmmn` e invocar el motor CMMN de Camunda para la gestión de tableros Kanban o Sprints Ágiles.

Los tableros ágiles se construirán mediante **Máquinas de Estado Relacionales Puras (JPA / Spring State Machine)** o, a lo sumo, consumiendo la API de `UserTask` de Camunda de forma huérfana (independiente de un proceso).

## 3. Argumentos Fundacionales del Análisis (Trade-Offs)

1.  **Deuda Técnica y Lock-In (Obsolescencia):** Aunque Camunda 7 soporta CMMN 1.1, la industria y la propia empresa creadora han aceptado que CMMN fue un fracaso comercial generalizado debido a su complejidad. **Camunda 8 ha deprecado y eliminado por completo el motor CMMN**, abogando por el uso de BPMN Event-Driven. Acoplar nuestro iBPMS a CMMN en V1 nos estancaría, violando el paso hacia V2.
2.  **Curva de Aprendizaje y Over-Engineering:** Obligar al equipo FullStack a aprender conceptos ultra-específicos como *Sentries, Plan Items, Discretionary Tasks* solo para arrastrar una tarjeta Kanban es el equivalente a matar una mosca con una bomba nuclear. Minimiza la mantenibilidad y eleva la fricción de onboarding de nuevos desarrolladores.
3.  **Transacciones Ágiles (El "Salto" Anárquico):** Un tablero Kanban requiere mutaciones atómicas sub-segundo (Ej: mover de *Blocked* a *InProgress* 5 veces al día). Las Base de Datos Relacionales (PostgreSQL) manejadas por el backend Java (Hexagonal) están literalmente creadas para administrar estados transaccionales ACID de forma ultra-rápida. Insertar el overhead de ejecución de reglas CMMN degradaría el UX del cliente en Pantalla de Kanban.

## 4. Solución Tecnológica Mandataria para Proyectos Ágiles
El Sprint Backlog y el Kanban operarán así:
1.  **Pantalla 8 (Template Builder):** Crea esqueletos y *Milestones* guardados puramente en tablas relacionales.
2.  **Pantalla Kanban:** Las Tareas Ágiles son entidades JPA que transicionan sus columnas de estado. El Backend expondrá una API REST plana en el Application Service (`patchTaskStatus(taskId, newState)`).
3.  **Integración Híbrida (Opción BPMN):** Si de una tarjeta Ágil se requiere disparar un flujo gigantesco estructurado (Ej. Aprobación de Presupuesto), la transición de JPA disparará un sub-proceso BPMN síncrono. Cuando el BPMN acabe, avisará a la entidad para saltar a *Done* de vuelta en el Kanban (Arquitectura de Eventos).
4.  La Auditoría transversal para métricas sigue viva porque el módulo transaccional escribirá en el mismo *Audit Ledger Inmutable* de todo el sistema.
