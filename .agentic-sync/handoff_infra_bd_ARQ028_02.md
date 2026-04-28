# Handoff Infra/BD — Rectificación de Colisión de Esquema (Schema Collision)

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica de Persistencia (Iteración 5 / sprint-6) |
| **Rama Git** | `sprint-6` |
| **Hallazgo** | Schema Collision en tabla `ibpms_audit_log` (001 vs 20) |
| **Flujo de Trabajo** | Infra/BD → QA |

---

## 2. Contexto del Incidente (Bomba de Tiempo en UAT)

Durante la auditoría del Handoff ARQ-028-02, detectamos que **Liquibase está tragándose silenciosamente la creación de columnas de auditoría de seguridad**. 

- El script `001-initial-schema.xml` crea la tabla `ibpms_audit_log` para auditorías de Formularios y Tareas de negocio (con columnas como `entity_type`, `payload_snapshot`).
- El script posterior `20-us036-rbac-schema.sql` intenta crear *la misma tabla* usando `CREATE TABLE IF NOT EXISTS ibpms_audit_log`, pero con columnas radicalmente distintas para el CISO (`user_id`, `ip_origen`, `endpoint_invocado`).
- **Resultado:** Como la tabla ya existe, Liquibase ignora el DDL en el script 20. Las columnas de seguridad jamás nacen en PostgreSQL, lo que provocará un colapso inminente en los controladores de ciberseguridad.

---

## 3. Instrucciones de Implementación

Tu objetivo es separar los dominios de auditoría renombrando la tabla del script de seguridad (RBAC).

### Tarea 1: Editar Liquibase Changeset
Modifica el archivo `backend/ibpms-core/src/main/resources/db/changelog/20-us036-rbac-schema.sql` (alrededor de la línea 70).

**Reemplaza esto:**
```sql
-- Ensure ibpms_audit_log exists for general logs as per CA-22 and CA-8 (from US-034/036)
CREATE TABLE IF NOT EXISTS ibpms_audit_log (
    ...
    CONSTRAINT pk_ibpms_audit_log PRIMARY KEY (id)
);
```

**Por esto:**
```sql
-- TABLA CORREGIDA: Segregación de dominio. Se usará ibpms_security_audit_log para separar el log del CISO del log de negocio.
CREATE TABLE IF NOT EXISTS ibpms_security_audit_log (
    id UUID NOT NULL,
    user_id VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    message_count INT,
    service_account_id UUID,
    endpoint_invocado VARCHAR(255),
    ip_origen VARCHAR(100),
    timestamp_utc TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ibpms_security_audit_log PRIMARY KEY (id)
);
```

### Tarea 2: Limpieza de Base de Datos Local
Debido a que Liquibase ya guardó el checksum anterior, debes instruir a tu contenedor de base de datos a reconstruirse, o bien ejecutar:
`mvn liquibase:clearCheckSums` o reconstruir el volumen Docker localmente para que ingiera el cambio de nombre.

---

## 4. Criterios de Aceptación y Veredicto
- [ ] El script `20-us036-rbac-schema.sql` NO DEBE contener la palabra `ibpms_audit_log`. Debe usar `ibpms_security_audit_log`.
- [ ] El levantamiento del entorno (`docker-compose up` + App Spring Boot) debe ejecutar los changelogs exitosamente sin conflictos de checksum.
- [ ] En la base de datos deben existir ambas tablas físicamente separadas.
