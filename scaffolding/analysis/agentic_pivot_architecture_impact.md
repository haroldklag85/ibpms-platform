# Evaluación Arquitectónica: Impacto del Pivote "Agentic Workflows" (V1 & V2)

**Fecha:** 2026-02-23
**Objetivo:** Analizar el impacto de la nueva estrategia "AI-Centric" (PRD actualizado) sobre los diagramas C4 y el Plan de Implementación actuales.

## 1. Análisis de Impacto en la Arquitectura Táctica V1 (MVP)

De acuerdo con la matriz MoSCoW (`v1_moscow_scope_validation.md`), el pivote hacia un modelo "AI-Centric" (Agentic, Zero-UI, ML Consultant) ha sido **explícitamente mitigado** para la versión 1, relegando esas capacidades a la V2. 

**Impacto en C4 V1 (`c4-model.md`): NINGUNO (Contenido).**
- La arquitectura V1 (Camunda 7 embebido, SPA Vue 3, Traductor NLP a DMN) se mantiene perfecta e inalterada.
- El diseño Hexagonal propuesto sigue siendo el escudo vital (Must Have) que nos permitirá desechar el motor Camunda en V2 sin afectar las entidades (`Expediente.java`).

## 2. Análisis de Impacto Masivo en la Arquitectura Estratégica V2 (SaaS)

El PRD introduce disrupciones arquitectónicas severas para la V2 que actualmente **NO están modeladas** en el `c4-model-v2.md`. 

### A. Reemplazo del Motor de Orquestación (Core Engine)
*   **PRD V2:** "AI-Centric Engine (Multi-Agent System): Reemplazo absoluto del BPMN rígido. Sistema multi-agente que enruta dinámicamente basado en 'Intento'".
*   **Estado Actual C4-V2:** Tenemos modelado un contenedor `Zeebe / C8` (Motor CQRS de Casos basado en BPMN/CMMN).
*   **GAP Arquitectónico:** Zeebe debe ser reemplazado o complementado por un **Multi-Agent Orchestrator Framework** (Ej. LangChain, Semantic Kernel, u otro motor basado en RAG y LLMs). El paradigma cambia de "Ejecución de Grafos Deterministas" a "Resolución de Intents".

### B. Revolución en Experiencia de Usuario (Zero-UI)
*   **PRD V2:** "Zero-UI (Headless) & Conversational: Ejecución directa vía chatbots, Slack/Teams y correos interactivos".
*   **Estado Actual C4-V2:** Tenemos el `Web Add-in O365` y los `Micro-Frontends`. No hay orquestación para clientes de chat.
*   **GAP Arquitectónico:** Debemos incluir en el Nivel 1 (Contexto) y Nivel 2 (Contenedores) los nuevos actores (Ms Teams/Slack) y un nuevo **Conversational Gateway/Adapter** en el Backend que reciba los "Intents" de chat y los traduzca a comandos para el sistema multi-agente.

### C. Módulos Satelitales AI (Consultor y Auditor)
*   **PRD V2:** "Consultor Digital AI (ML)" y "Auditor AI (Plug-in Compliance)".
*   **Estado Actual C4-V2:** No existen contenedores ML que ingieran datos históricos de ElasticSearch.
*   **GAP Arquitectónico:** 
    1. Se requiere un **ML Pipeline / Data Ingestion Pod** que lea los Audit Logs/ElasticSearch y alimente el entrenamiento continuo.
    2. Se requieren microservicios satelitales: `AI Auditor Service` y `AI Process Consultant Service`.
    3. Se menciona una **Graph DB** en el PRD ("Agentic RAG y Graph DB"). Actualmente solo tenemos PostgreSQL y ElasticSearch en V2. Debemos añadir una BD de Grafos (Ej. Neo4j o CosmosDB Gremlin) para el RAG agentico.

## 3. Conclusión y Siguientes Pasos (Actions Required)

El diseño de la PoC y la **V1 está blindada y lista para continuar a nivel de código** sin modificaciones arquitectónicas, gracias al uso de la Arquitectura Hexagonal.

Sin embargo, el documento modelo **`c4-model-v2.md` se ha quedado obsoleto** frente a esta nueva e innovadora visión Agentic. 

**Plan de Acción Propuesto:**
1. Crear una rama de actualización sobre el `c4-model-v2.md` para eliminar la dependencia rígida de Zeebe/C8 e introducir el ecosistema Multi-Agente, la Graph DB y los Bots Conversacionales.
2. Actualizar la sección V2 del `implementation_plan.md` para reflejar el futuro Headless y Agentic.

