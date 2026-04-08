# 🏛️ Auditoría Forense de Arquitectura - iBPMS Platform
**Fecha:** 2026-04-08
**Autor:** Agente Arquitecto Líder

Se ha realizado una revisión integral y cruzada del repositorio documental en `docs/architecture/`, evaluando los C4 Models (V1/V2), Diccionarios de Datos (ERD) y los Registros de Decisiones Arquitectónicas (ADRs). Se han detectado **4 contradicciones y GAPs críticos** que rompen los principios sistémicos si son implementados ciegamente por los agentes.

---

## 🚨 GAP 1: Remanente Heredado de MySQL en PostgreSQL (Definición Rota)
**Documentos Involucrados:** `data_architecture_erd.md` vs `adr_010_postgresql_pgvector_migration.md`

*   **El Problema:** El ADR 010 declara triunfalmente la migración de todo el motor relacional hacia PostgreSQL 15+ para apalancar `pgvector` en la misma infraestructura. Sin embargo, el Diccionario de Datos Físico (`data_architecture_erd.md`) sigue prescribiendo el diseño de claves primarias como `char(36) id PK "UUID"`. 
*   **Impacto Arquitectónico:** Obligar a PostgreSQL a almacenar UUIDs usando `VARCHAR/CHAR(36)` castiga el performance masivamente (ocupando más de 36 bytes por registro en índices B-Tree). PostgreSQL tiene un tipo de dato `uuid` nativo a 16-bytes.
*   **Acción Requerida:** Todo script DDL y los diccionarios deben actualizarse para proscribir `CHAR(36)` y usar explícitamente `UUID`.

## 🚨 GAP 2: Riesgo Severo de "Dual-Write" y Consistencia Transaccional (Contradicción)
**Documentos Involucrados:** `adr-003-camunda7-embedded.md` vs `rabbitmq_topology.md`

*   **El Problema:** El ADR 003 argumenta usar Camunda Empotrado para aprovechar el bloque `@Transactional` de Spring Boot y asegurar que ninguna fila de Base de Datos se comprometa si el flujo BPMN falla simultáneamente (Consistencia ACID simplificada). Pero el documento Topológico postula a RabbitMQ. Al disparar mensajes asíncronos (A RabbitMQ: `ibpms.bpmn.events`) en medio de un contexto JPA y que posteriormente este sufra un rollback, **el mensaje en RabbitMQ se habrá emitido irrevocablemente**, generando casos fantasmas.
*   **Impacto Arquitectónico:** Si un flujo ordena emitir un evento tras guardar un Expediente y hay un error de constraint, RabbitMQ mandará un correo a un cliente basándose en un flujo que en la DB jamás existió.
*   **Acción Axiomática a Definir:** Falta la mención estricta a la implementación del **Patrón Transactional Outbox** acoplado a la infraestructura transaccional de Spring.

## 🚨 GAP 3: Fuga del Patrón Hexagonal en los Módulos Ágiles (Ambigüedad)
**Documentos Involucrados:** `adr-001-hexagonal-architecture.md` vs `adr_008_cmmn_vs_kanban.md`

*   **El Problema:** El ADR 001 declara solemnemente que ninguna etiqueta técnica `@Entity` (JPA) debe filtrarse a las lógicas del negocio (Core Puro). Mientas tanto, el ADR 008, al justificar las transiciones del motor Kanban, dicta pragmáticamente: *"Las Tareas Ágiles son entidades JPA que transicionan sus columnas de estado"*.
*   **Impacto Arquitectónico:** Los desarrolladores interpretarán esto como una licencia para pasar *Entities* directamente desde los repositorios JPA hasta los *Application Services* sin utilizar mappers ni POJOs de dominio limpios, corrompiendo la Arquitectura Hexagonal y acoplando reglas a JPA.
*   **Acción Requerida:** Se debe modificar el ADR-008 dejando claro que el estado transitado será un Objeto Pojo y el Driven Adapter JPA será el único encargado de hidratar la respuesta de "Status".

## 🚨 GAP 4: Sobresaturación de Componentes vs Limitaciones IaaS Tácticas (Diseño Roto C4)
**Documentos Involucrados:** `c4-model.md` (V1 Táctica)

*   **El Problema:** El Diagrama C4 de la Fase 1 establece como mandato un entorno estrecho en la Nube: una infraestructura base para soportar la operación (PostgreSQL, APIM, VM). Pero internamente proyecta dentro del mismo contenedor lógico: Motor Camunda, RabbitMQ, RAG Engine Python y un motor local **Llama 3**. 
*   **Impacto Arquitectónico:** Llama 3 local exige consumos de RAM/VRAM intolerables para convivir el mismo hardware donde corre Spring Boot. Al momento del GC (Garbage Collection) de Java, combinado con búsquedas vectoriales, el clúster se congelará transaccionalmente OOM (Out Of Memory).
*   **Acción Requerida:** Sinceramiento operacional. Si se usa Azure, el modelo de IA inferencial V1 debería apuntar externamente al SaaS de Azure OpenAI vía API (como en V2), u oficializar que Llama 3 operará en una subred separada que rompe el "monolito clásico 1 VM".

---

### Conclusión y Veredicto
El marco conceptual teórico general es increíblemente avanzado y futurista, sin embargo, debe pasar por una etapa urgente de "Deflación de Deuda Técnica". Recomiendo enfáticamente que me autorices a parchar los archivos `data_architecture_erd.md`, `c4-model.md` y `adr_008_cmmn_vs_kanban.md` para cerrar estos GAPs.
