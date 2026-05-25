# ADR-015: Gobernanza de Modelado Híbrido en PostgreSQL (JSONB, Dual-Schema y Particionamiento)

**Status:** Aceptado
**Date:** 2026-05-24
**Context:** El sistema iBPMS requiere manejar formularios dinámicos y métricas de procesos con esquemas variables en un entorno restringido a una sola Máquina Virtual (VM) de Base de Datos (PostgreSQL 15+). Se requiere formalizar las reglas para evitar anti-patrones relacionales (como EAV) y acoplamiento severo con el motor BPM (Camunda).
**Autor:** Antigravity (Lead Software Architect AI) / SRE Team

## 1. Contexto

La plataforma iBPMS (V1) está limitada por un contrato de infraestructura que restringe el clúster a un número mínimo de VMs. Todo el peso transaccional, documental y vectorial (Inteligencia Artificial vía `pgvector`) recae sobre **una única instancia principal de PostgreSQL 15+**.

El modelado tradicional en **Tercera Forma Normal (3NF)** es excelente para dominios estáticos (usuarios, catálogos), pero fracasa catastróficamente al intentar modelar formularios de negocio dinámicos ("Payloads"), lo que empuja históricamente a los equipos a usar el anti-patrón **Entity-Attribute-Value (EAV)**. Además, compartir el motor de base de datos con un framework embebido (Camunda 7) presenta un alto riesgo de acoplamiento (Vendor Lock-in) si los desarrolladores cruzan información en SQL.

Por lo tanto, se hace indispensable un documento de gobernanza estricta para el modelado.

## 2. Decisión Arquitectónica

Se decreta como obligatoria la implementación de un **Modelado Híbrido Relacional/Documental** sobre PostgreSQL, regido por las siguientes cuatro normativas inquebrantables:

### A. Uso Exclusivo de JSONB para Datos Dinámicos (Prohibición de EAV)
*   **Regla:** Toda variable o campo recolectado por un formulario dinámico del negocio **debe** almacenarse en la columna `payload` de tipo `JSONB` en la tabla `ibpms_case`.
*   **Prohibición:** Queda terminantemente prohibido crear tablas genéricas de Clave-Valor (EAV) para emular esquemas dinámicos, así como agregar columnas DDL (`ALTER TABLE`) por cada nuevo campo que un usuario necesite en la UI.
*   **Indexación:** Para proteger la memoria RAM (limitada en la única VM), **no se indexará el documento JSON completo**. Se utilizará la tabla plana `ibpms_metadata_index` y se aplicarán índices B-Tree específicos solo sobre metadatos críticos extraídos explícitamente en tiempo de guardado.

### B. Patrón "Dual-Schema" de Aislamiento Estricto (Zero-Exceptions)
*   **Regla:** La base de datos mantendrá una segregación lógica absoluta entre los esquemas del negocio (`ibpms_*`) y los del motor de procesos (`ACT_*` de Camunda).
*   **Prohibición:** Se declaran como una **Violación Severa de Arquitectura (Política de Cero Excepciones)** las consultas nativas (`@Query`) o Vistas que apliquen un `JOIN` directo a nivel SQL entre ambos mundos. La integración debe darse única y exclusivamente a través de la API Java de Camunda referenciando llaves lógicas (`process_instance_id`, `camunda_task_id`).

### C. Particionamiento para Prevención de "Ever-Growing Tables"
*   **Regla:** La tabla `ibpms_audit_log` (y cualquier futura tabla histórica de alto volumen) debe crearse nativamente usando la directiva `TABLE PARTITION BY RANGE (created_at)` de PostgreSQL (Particionamiento Mensual).
*   **Retención (Compliance):** Se establece un periodo de **retención en vivo de 5 años** legales (Cumplimiento Típico SGDEA). Los datos anteriores a este periodo serán sujetos a un proceso automático de volcado en frío a Azure Blob Storage y posterior `DROP PARTITION`.

### D. Protección de IDs y Contención
*   **Regla:** Queda prohibido el uso de `BIGINT AUTO_INCREMENT` para las entidades del Core. Todo identificador debe ser criptográfico usando `UUID v4` (`gen_random_uuid()`).
*   **Contención VM:** Dado que PostgreSQL absorberá el esfuerzo de búsquedas analíticas (CQRS), vectoriales (`pgvector`) y documentales (`JSONB`), se estandariza la necesidad de configurar alarmas SRE de *Auto-Scaling Vertical* para intervenir si el consumo de CPU supera el 80% sostenido.

## 3. Consecuencias

*   **Positivas:** 
    *   Velocidad de iteración (Time-to-Market) casi instantánea: Los formularios pueden mutar su esquema libremente en la UI sin afectar el Backend ni requerir migraciones DDL.
    *   Blindaje a futuro: Si Camunda 7 es deprecado y reemplazado por Zeebe (V2), la base de datos `ibpms_*` no requerirá ninguna migración.
*   **Negativas/Riesgos Aceptados:**
    *   Los desarrolladores enfrentarán una curva de aprendizaje al construir consultas complejas sobre `JSONB` mediante JPA/Hibernate.
    *   La base de datos única centralizará todos los fallos (Single Point of Failure), requiriendo una estrategia agresiva de Backups y *High Availability* pasiva (PostgreSQL Standby).

## 4. Revisión y Aprobación
Este documento rige el desarrollo Backend V1 y deberá ser defendido en cada Pull Request (PR).

*   **Aprobado por:** Lead Software Architect AI
*   **Aprobado por:** Tech Lead Backend
*   **Aprobado por:** SRE & DBA Team
