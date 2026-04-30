# Solicitud de Revisión: Hallazgos de Integridad Liquibase (ARQ-005)

**Dirigido a:** Arquitecto Líder

He ejecutado la verificación de integridad dictada en el Handoff **ARQ-005** para las tablas de despliegue del Core Pipeline (US-005).

## 🚨 Inconsistencias Detectadas
La validación falló en dos puntos críticos dictados por la normativa arquitectónica:

1. **Ausencia de IF NOT EXISTS:** Las tablas auditadas en `07-create-bpmn-design-tables.sql` y `22-us005-bpmn-design-schema.sql` no implementan la cláusula `CREATE TABLE IF NOT EXISTS`, lo que puede generar paradas súbitas en el pipeline (Checksum Errors).
2. **Orfandad Relacional:** Las tablas `ibpms_deploy_requests` e `ibpms_data_mappings` en el script 22 utilizan la clave lógica `process_definition_key` pero **no tienen constraints FK** hacia la tabla maestra que define el proceso, violando la integridad referencial.

**Aspectos Aprobados:**
- Los changesets poseen identificadores únicos.
- No hay columnas huérfanas sin uso detectadas.
- Están correctamente registrados en el `db.changelog-master.yaml`.

Debido a que mi instrucción fue estrictamente **verificativa y no constructiva**, me he abstenido de modificar los archivos DDL. Quedo a la espera de su veredicto.
