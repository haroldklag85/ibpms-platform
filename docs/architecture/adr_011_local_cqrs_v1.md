# ADR-011: Estrategia de Local CQRS para V1 (Tasklists y Búsquedas)

**Status:** Aceptado
**Date:** 2026-04-08
**Context:** Necesidad de soportar bandejas unificadas responsivas sin acoplar la lectura con los modelos pesados de escritura del motor (V1 Táctica) respetando las restricciones de infraestructura (Single PostgreSQL VM).
**Autor:** Antigravity (Lead Software Architect AI)

## 1. Contexto Comercial y Técnico
En un iBPMS, el motor transaccional sufre si 10,000 agentes de operaciones oprimen F5 (Refrescar) para obtener datos estructurados en su bandeja al mismo tiempo que el motor de workflow intenta hacer un commit ACID para avanzar una tarea (Command).

La Arquitectura Cloud-Native V2 promete delegar las lecturas a un "Read Model" de ElasticSearch (Distributed CQRS). No obstante, para la **V1**, levantar clústeres separados de ElasticSearch o bases especializadas de lectura vulnera la restricción operativa de mantener un único servidor central de Base de Datos PostgreSQL 15+.

¿Cómo entregamos lecturas hiper-rápidas para los Workdesks (Filtros, Facetas, Bandejas) sin colapsar el modelo de dominio y manteniendo Spring Modulith intacto para la etapa 1?

## 2. Decisión Arquitectónica: Local CQRS
Se aprueba formalmente el uso del patrón **Local CQRS** implementado exclusivamente a nivel de capa de servicio y consultas de base de datos dentro del límite de un mismo contexto modular (No hay sincronización de red externa asíncrona).

### Directrices de Implementación V1
1.  **Separación Lógica, no Física:** 
    *   **Escritura (Commands):** Mutarán las tablas relacionales transaccionales (`ibpms_case`, `ibpms_task`). Serán manejadas por `Spring Data JPA` u orientadas al dominio usando Entidades ricas y bloqueos pesados transaccionales (ACID).
    *   **Lectura (Queries):** No usarán JPA puro si esto requiere cargar gráficos de objetos pesados mediante JOINs perezosos (N+1). El "Read Engine" utilizará **Proyecciones (DTOs)**, consultas nativas (`JdbcTemplate` / MyBatis / `Spring Data Projections`) enfocadas en retornar objetos planos inmediatamente listos para JSONización, o **Vistas Materializadas (Materialized Views)** en PostgreSQL que se refresquen on-demand.
2.  **Cero Mapeo Bidireccional:** Un modelo retornado desde la ruta API de Queries (`WorkdeskSummaryDto`) **jamás** debe usarse como objeto de entrada para un comando (`UpdateTaskCommand`).
3.  **Prohibición de ElasticSearch Temporal:** Ningún microservicio V1 debe tener dependencia de ElasticSearch o sistemas pub/sub para intentar construir una base de lectura espejo. El Single Source of Truth para Querys y Commands será PostgreSQL.

## 3. Consecuencias
*   **Positivas:** 
    *   La infraestructura sigue siendo austera y económica (Una VM).
    *   No hay inconsistencia eventual de red severa transaccionalmente; las lecturas son inmediatas post-escritura en la misma BD.
    *   Se evita código obeso e ingeniería excesiva en la fase de tracción temprana.
*   **Negativas/Riesgos:** 
    *   Picos sostenidos de lectura y escritura competirán por pool de conexiones, I/O en SSD y poder computacional lógico sobre la única instancia de PostgreSQL. La mitigación debe ser apoyada mediante indexación B-Tree rigurosa y escalamiento vertical temporal si la carga aumenta bruscamente antes de la V2. 
    *   Se asume deuda técnica intencional y calculada en cuanto al refactoring del módulo de lectura para cuando se aborde la V2 hacia ElasticSearch.
