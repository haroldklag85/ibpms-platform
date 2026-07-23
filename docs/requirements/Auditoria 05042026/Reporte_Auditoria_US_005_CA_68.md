# Reporte de Auditoría Estricta: US-005 (CA-68)
## Persistencia del Data Mapping como Extension Properties

### 🚨 Violación Crítica de Protocolo
Se detectó y reportó el uso accidental del comando `grep_search` para inicializar la búsqueda del requerimiento CA-68 en el repositorio. Según las reglas `auditoria_trazabilidad_topdown.md`, se abortó la secuencia de búsqueda no autorizada, se documentó el incidente en `task.md`, y se procedió a reiniciar la navegación desde la raíz empleando los comandos estructurales manuales (`list_dir` y `view_file`).

### 🗺️ Ruta Estructural Navegada (Top-Down)
1. `list_dir: backend/ibpms-core/src/main/java/com/ibpms/poc/`
2. `list_dir: backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web`
3. `view_file: BpmnDesignController.java` (Capa REST)
4. `view_file: DataMappingEntity.java` (Capa de Persistencia)
5. `view_file: DataMappingIntegrityTest.java` (Tests)
6. `view_file: 22-us005-bpmn-design-schema.sql` (Migración Liquibase)

### 🏷️ Archivos Etiquetados con Éxito (`@Traceability`)
*   `BpmnDesignController.java` (A nivel de métodos `getDataMappings` y `createDataMapping`)
*   `DataMappingEntity.java` (A nivel de entidad)
*   `DataMappingIntegrityTest.java` (A nivel de clase de pruebas)

### 🚨 Brechas de Implementación y Deuda Técnica
La auditoría confirmó la anomalía **"OBS-1: Entity/DDL mismatch"** reflejada en la matriz de cobertura. Existe una grave inconsistencia de contrato entre los tests y la base de datos real:
*   El script oficial de Liquibase (`22-us005-bpmn-design-schema.sql`) crea la tabla `ibpms_data_mappings` con `id UUID`, `task_id` y `connector_id`.
*   El test de integración `DataMappingIntegrityTest` ignora por completo el script de Liquibase. En su método `setUp()`, inyecta un esquema DDL espurio (`CREATE TABLE IF NOT EXISTS ibpms_data_mappings (id VARCHAR, process_key VARCHAR, form_id VARCHAR, mapping_json TEXT)`).
*   Esto provoca que el Test dé "Falso Positivo" validando columnas (`form_id`) que ya no existen en el diseño oficial de la Entidad.

### ⚠️ Violaciones de Arquitectura
El test `DataMappingIntegrityTest` viola el principio Zero-Mock y la fidelidad del entorno. Emplea un `jdbcTemplate.execute` de creación de tabla manual en lugar de montar el contenedor con **Testcontainers** y correr la migración de **Liquibase**, creando un espejismo en la cobertura QA.
