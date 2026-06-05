# Solicitud de Aprobación Arquitectónica — BACKEND (OBS-1 / US-005 CA-68)

## 1. Discrepancias Detectadas
Tras realizar la auditoría de esquema cruzado entre las Entidades de Negocio (BPMN) y los Changelogs de Liquibase (siguiendo directrices Zero-Trust SRE), he detectado las siguientes brechas que comprometen la compilación y validación de Hibernate:

1. **Desalineación de Llave Primaria (Partitioning)**: El script Liquibase `37-us005-audit-log-partitioning-fix.sql` particionó la tabla `ibpms_bpmn_design_audit_log` y cambió su llave primaria a una compuesta `(id, timestamp)` por requerimientos de Postgres. Sin embargo, la entidad Java `BpmnDesignAuditLogEntity` sigue teniendo un `@Id` simple en `id`.
2. **Tipado JSONB Subóptimo**: Las columnas `details` (en `BpmnDesignAuditLogEntity`) y `generic_form_whitelist` (en `BpmnProcessDesignEntity`) son declaradas como `JSONB` en Postgres, pero actualmente están mapeadas como `String` en Java. El handoff arquitectónico exige estrictamente mapearlos a `Map<String, Object>` usando la librería especializada `io.hypersistence.utils`.
3. **Ausencia de Dependencia**: El proyecto actualmente no declara la dependencia de `hypersistence-utils` en el `pom.xml`, lo cual bloqueará la implementación del punto anterior.
4. Las entidades `ProcessLockEntity`, `ExternalTaskTopicEntity`, `DeployRequestEntity` y `DataMappingEntity` se validaron satisfactoriamente y están al día con sus esquemas DDL correspondientes (incluyendo adiciones posteriores como `058-create-deploy-requests.sql`).

## 2. Plan de Acción (Fixes a Implementar)

**Paso 1: Resolución de Dependencias**
- Inyectar la dependencia `hypersistence-utils-hibernate-63` (o la acorde a la versión actual de Hibernate) en `backend/ibpms-core/pom.xml`.

**Paso 2: Corrección de PK Compuesta (Entity Alignment)**
- Crear la clase `BpmnDesignAuditLogId.java` que implemente `Serializable` (conteniendo `id` y `timestamp`).
- Añadir la anotación `@IdClass(BpmnDesignAuditLogId.class)` a `BpmnDesignAuditLogEntity`.
- Añadir la anotación `@Id` al campo `timestamp` de dicha entidad.

**Paso 3: Mapeo Fuerte de JSONB**
- Refactorizar `BpmnDesignAuditLogEntity.details` para ser de tipo `Map<String, Object>` anotado con `@Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)`.
- Refactorizar `BpmnProcessDesignEntity.genericFormWhitelist` de manera idéntica.
- Asegurar que los getters, setters y constructores respeten este nuevo tipado dinámico.

**Paso 4: Gatekeeper SRE (Zero-Trust)**
- Ejecutar el script `start-dev.bat` / compilar con Maven y garantizar la inicialización impecable de Spring Boot sobre Tomcat puerto 8080 (cero conflictos de `validate` en JPA/Hibernate).

**Paso 5: Gobernanza Documental**
- Actualizar `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` detallando la acción y valor de negocio.

Quedo a la espera de autorización para iniciar la iteración de mutación física (EXECUTION).
