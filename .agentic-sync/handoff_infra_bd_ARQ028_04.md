# Handoff Infra/BD — ARQ-028-04 | Segregación de Cohesión Mixta

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica y Refactorización (Iteración 5 / sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Cerrar** | **ARQ-028-04:** Entidad con cohesión mixta (`ibpms_form_definitions`) |
| **Flujo de Trabajo** | Infra/BD → Backend → QA |

---

## 2. Contexto Arquitectónico
La tabla `ibpms_form_definitions` viola el principio de Alta Cohesión al almacenar tanto el "Diseño y Versionado del Formulario" como su "Estado de Certificación de QA". La arquitectura requiere separar la certificación en su propia tabla (`ibpms_form_certifications`) con una relación 1:1 o 1:N hacia la definición.

---

## 3. Instrucciones de Implementación

### Tarea 1: Crear Changeset de Liquibase
Crea un nuevo archivo SQL en `backend/ibpms-core/src/main/resources/db/changelog/` (ej. `26-us028-arq02804-split-certification.sql`) e incluye lo siguiente:

1. **Creación de la nueva tabla:**
   ```sql
   CREATE TABLE ibpms_form_certifications (
       id UUID NOT NULL,
       form_definition_id UUID NOT NULL,
       is_qa_certified BOOLEAN NOT NULL DEFAULT FALSE,
       certified_schema_hash VARCHAR(64),
       certified_by VARCHAR(100),
       certified_at TIMESTAMP,
       CONSTRAINT pk_ibpms_form_certifications PRIMARY KEY (id),
       CONSTRAINT fk_fc_form_definition FOREIGN KEY (form_definition_id) REFERENCES ibpms_form_definitions(id) ON DELETE CASCADE
   );
   ```

2. **Migración de Datos (Data Migration):**
   Mueve los datos existentes de certificaciones a la nueva tabla.
   ```sql
   INSERT INTO ibpms_form_certifications (id, form_definition_id, is_qa_certified, certified_schema_hash, certified_by, certified_at)
   SELECT gen_random_uuid(), id, is_qa_certified, certified_schema_hash, certified_by, certified_at
   FROM ibpms_form_definitions;
   ```

3. **Drop de Columnas Antiguas:**
   Elimina las columnas de certificación de la tabla original.
   ```sql
   ALTER TABLE ibpms_form_definitions
   DROP COLUMN is_qa_certified,
   DROP COLUMN certified_schema_hash,
   DROP COLUMN certified_by,
   DROP COLUMN certified_at;
   ```

### Tarea 2: Actualizar el changelog master
Asegúrate de registrar este nuevo script en tu `db.changelog-master.yaml` o el archivo de control maestro si aplica.

---

## 4. Criterios de Aceptación
- [ ] La tabla `ibpms_form_certifications` debe existir.
- [ ] Las columnas de certificación NO deben existir en `ibpms_form_definitions`.
- [ ] El despliegue de Liquibase (`docker-compose up` o `mvn spring-boot:run`) debe ejecutarse exitosamente.

> ⚠️ Notifica tu finalización para que el Agente Backend pueda proceder con su código.
