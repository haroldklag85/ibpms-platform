# 🧠 Handoff Document - Sprint 10: Motor Cognitivo Docketing (RAG V1)

**Atención: Equipo de Agentes de Desarrollo Backend (Data & AI Squad)**
Este documento contiene las especificaciones arquitectónicas (Contrato) para la ejecución final de la Fase 6 (Sprint 10). Su misión es habilitar el "Cerebro" de la plataforma: un motor de Búsqueda Semántica basado en Generación Aumentada por Recuperación (RAG).

---

## 🎯 Objetivo del Sprint
Dotar al Backend del iBPMS de la capacidad para almacenar "Embeddings" (Vectores Matemáticos) de los expedientes jurídicos y permitir búsquedas semánticas (distancia del coseno) en lenguaje natural para asistir a los usuarios a través del Chat IA del Frontend.

## 🏛️ Restricción Arquitectónica (CRÍTICA)
De acuerdo a las decisiones de Dirección de Arquitectura registradas en el ADR (PostgreSQL vs Qdrant), la Fase V1 debe ajustarse al presupuesto actual de **3 Máquinas Virtuales (IaaS)**.

*   **PROHIBIDO:** Instalar o consumir conectores hacia bases de datos vectoriales puras en la nube como Pinecone, Milvus o Qdrant.
*   **OBLIGATORIO:** Toda la persistencia vectorial debe realizarse interactuando la misma base de datos operativa actual (**PostgreSQL**) haciendo uso de la extensión `pgvector`.

## 🛡️ Capas Anti-Corrupción (Reglas de Oro)

Para no quedar amarrados a PostgreSQL ni a un proveedor de IA externo cuando la arquitectura evolucione a V2, se han creado dos **Puertos Hexagonales**. Toda integración debe pasar estrictamente por estas interfaces:

1. **Abstracción de Base de Datos Vectorial:**
   `com.ibpms.poc.application.port.out.ai.VectorDatabasePort` (Debe ser implementada por `PgVectorAdapter`).
2. **Abstracción de Proveedor LLM (Agnóstico a la IA):**
   `com.ibpms.poc.application.port.out.ai.LlmProviderPort` (Para el Sprint 10, debe ser implementada por `ApimLlmAdapter` para consumir Vertex/OpenAI vía APIM. Esto permitirá en el futuro construir un `LocalLlmAdapter` si requerimos instalar un LLM Open Source en la misma red sin alterar la lógica de negocio).

## 🛠 Pasos de Acción Inmediata para los Agentes:

1.  **Migración de Base de Datos (Liquibase / JPA):**
    *   Habilita la extensión en la base de datos ejecutando: `CREATE EXTENSION IF NOT EXISTS vector;`
    *   Crea una tabla relacional `document_embeddings` (o similar) que contenga la columna de tipo `vector(768)`.
    *   No mezcles los vectores masivos en la tabla principal de `expedientes`. Únelos mediante un ID foráneo.
2.  **Adaptador Postgres (PgVectorAdapter) y Adaptador IA (ApimLlmAdapter):**
    *   Implementa `PgVectorAdapter` usando consultas nativas matemática `<->` de pgvector.
    *   Implementa `ApimLlmAdapter` para que haga peticiones HTTP al Azure APIM conectando con el LLM autorizado actual.
3.  **Caso de Uso RAG:**
    *   Programa la lógica conversacional. Cuando el frontend envíe "Busca tutelas por despido", el endpoint inyecta los Puertos (Interfaces). Llama a `LlmProviderPort.generateEmbedding()`, pasa ese vector a `VectorDatabasePort.searchSimilar()`, recaba los textos en una misma cadena de String, y se lo envía ciego de vuelta a `LlmProviderPort.generateRagResponse()`. Nunca interactúes directamente con bibliotecas de OpenAI o GCP dentro del Caso de Uso.

¡Inicien la codificación del Sprint 10 y hagan los reportes de commit!
