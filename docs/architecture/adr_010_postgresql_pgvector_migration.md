# ADR 010: Migración de Motor de Base de Datos Base (MySQL a PostgreSQL)

## Estado
**Aceptado**

## Contexto
El **Sprint 10** exige la implementación de un Motor Cognitivo (RAG - Retrieval-Augmented Generation) para el análisis de Docketing Legal y un "Learning Loop". Para que la Inteligencia Artificial pueda buscar similitudes semánticas entre miles de expedientes, requiere una base de datos capaz de almacenar y consultar **Vectores de Incrustación (Embeddings)** generados por los LLMs.

En el diseño original, la plataforma estaba anclada a **MySQL 8**. Sin embargo, MySQL no posee soporte nativo o extensiones de grado de producción maduras para la búsqueda de vectores (Vector Search / HNSW). 
Bajo el contrato restrictivo de V1 (Máximo 3 VMs), no podemos aprovisionar una nueva máquina dedicada a una base de datos vectorial especializada como Milvus, Pinecone o Weaviate.

## Decisión
Se ha decidido **migrar el motor de base de datos central de todo el iBPMS de MySQL 8 a PostgreSQL 15+**.
Se utilizará específicamente una imagen que incluya la extensión de código abierto **`pgvector`**.

## Justificación
1. **Soporte Nativo de IA:** `pgvector` permite almacenar los *embeddings* (vectores de 768 o 1536 dimensiones) en una columna estándar y crear índices de similitud (HNSW o IVFFlat) que responden en milisegundos.
2. **Cumplimiento IaaS V1:** Al tener la tabla relacional de `Expedientes` y sus vectores semánticos en el mismo motor de base de datos, mantenemos nuestra huella de arquitectura en exactamente 1 VM paralela de Base de Datos. No rompemos el contrato.
3. **Consistencia Transaccional:** Al realizar búsquedas de IA, podemos cruzar inmediatamente (JOIN) con roles y permisos (RBAC). Si usamos una base externa, tendríamos que sincronizar los datos constantemente.
4. **Camunda Support:** Camunda 7 tiene soporte Oficial (Tier 1) pleno para PostgreSQL, por lo que el motor de procesos operará con total normalidad.

## Consecuencias y Plan de Refactorización
Esta es una decisión con bajo costo de refactorización si se toma ahora (antes de salir a producción):

1.  **Infraestructura (`docker-compose.yml`):** Reemplazar la imagen `mysql:8.0` por `pgvector/pgvector:16` (o similar).
2.  **Backend (`pom.xml`):** Eliminar la dependencia `mysql-connector-j` e inyectar `postgresql`.
3.  **Configuración (`application.yml`):** Cambiar el dialecto de JPA a `PostgreSQLDialect` y la cadena JDBC.
4.  **Liquibase/Scripts:** Traducir cualquier script manual de inicialización o migraciones específicas de MySQL a la sintaxis estándar de Postgres.

## Conclusión
Adoptar PostgreSQL estabiliza el proyecto y nos dota de un motor relacional de grado empresarial que, simultáneamente, actúa como una Base de Datos Vectorial de altísima potencia, habilitando el Sprint 10 sin costos de infraestructura adicionales.
