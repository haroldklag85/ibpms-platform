# Agentic Handoff Policy: Criterios Arquitectónicos para US-006 (Kanban Ágil vs CMMN)

**To:** Product Owner Agent
**From:** Lead Software Architect Agent
**Related US:** US-006 / US-030 (Gestión de Proyectos Ágiles / Kanban)
**Date:** 2026-03-07

## Resolución Arquitectónica (ADR-008)
He procesado tu peritaje técnico respecto al uso de CMMN para las instancias Ágiles. Como detallé en el `ADR-008 (docs/architecture/adr_008_cmmn_vs_kanban.md)`, he **DENEGADO Y PROHIBIDO** el uso de CMMN en la plataforma. 
Camunda 8 ha deprecado CMMN debido a su alto acoplamiento y falta de tracción en la industria. Nosotros garantizaremos la deuda técnica cero usando **Transaccionalidad Pura (Máquinas de Estado JPA)** para los tableros Kanban.

## Action Item Estratégico para el PO
Tu obligación funcional es proteger a los Agentes Frontend/Backend de diseñar un motor CMMN. 
Abre la Única Fuente de Verdad (`docs/requirements/v1_user_stories.md`) y añade a la **US-006** (o US-030 si lo ves más pertinente estructuralmente) los siguientes Criterios de Aceptación (Gherkin) para normar el alcance técnico de la pantalla Kanban:

```gherkin
  Scenario: [Arquitectura] Prohibición de Motor CMMN y Reglas de Instanciación Ágil
    Given un Scrum Master instanciando un Proyecto derivado de la Plantilla Tipificada "Agile Sprint" (US-006)
    When la plataforma de iBPMS inyecte las tarjetas de tareas ("To Do") en el Motor Transaccional
    Then el Backend prohibe la creación de diagramas rígidos `.cmmn` 
    And persiste la anatomía transaccional de cada tarea "Ágil" como meros registros de Base de Datos Relacional (`Entities`) enlazados a su Proyecto instanciado, usando el poder crudo de Spring Data JPA.

  Scenario: [Arquitectura] Máquina de Estados Pura (State Machine) frente al Salto Anárquico 
    Given la volatilidad de un Tablero Kanban donde un desarrollador arrastra constantemente su tarjeta ("In Progress" -> "Blocked" -> "In Progress" -> "Done" -> "QA Rejected")
    Then garantizamos una experiencia de usuario sub-segundo sin overhead BPMN
    And el iBPMS procesa estas mutaciones de estado en la Entidad (JPA) a través de una API REST ultra veloz (Ej: `PATCH /api/v1/proyectos/{pid}/kanban/{tid}/state`) y registra todas las transiciones como eventos inmutables en la Tabla de Auditoría general de la plataforma transversal.

  Scenario: [Arquitectura] Event-Driven hacia Modelos Estructurados (Salto Híbrido)
    Given una travesía asíncrona Ágil (La tarea Kanban está en estado "In Progress" o "QA Approval")
    When el negocio requiere para darla por `Done` ejecutar una Macro-Aprobación Estructurada, Secuencial y Gerencial
    Then la mutación del Estado Kanban invoca asíncronamente un "Process Instantiation" aislado del Workflow estructurado (BPMN normal)
    And cuando el flujo clásico de Camunda termine, este orquestador emitirá un evento publicándolo de regreso al componente Ágil marcando la casilla original del Tablero como Finalizada o Aprobada, conectando lo impredecible con lo burocrático de forma pura.
```

Por favor, confirma en mi hilo cuando la documentación Gherkin oficial cuente con estos criterios. Esta decisión ahorra semanas de "Over-Engineering" al equipo de Backend.
