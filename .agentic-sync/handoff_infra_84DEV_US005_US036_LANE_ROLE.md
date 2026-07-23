# 📦 Handoff Infra/BD — Iteración 84-DEV-LANE-ROLE
# Micro-Sprint 1: Infraestructura de Datos (Lane-Role Assignment)

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | `84-DEV-LANE-ROLE` |
| **US** | US-005 (Extensión: Lane Actor Assignment) + US-036 (Extensión: RBAC Lane Integration) |
| **CAs** | Extensión funcional aprobada por PO — No son CAs numerados existentes |
| **Rama Git** | `feature/lane-role-assignment` |
| **Agente** | Infra/BD |
| **Dependencias** | Ninguna — este handoff arranca primero |
| **SSOT** | `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_B_formularios_bpmn.md` (US-005) + `docs/requirements/epics/epic_E_seguridad_identidad_config.md` (US-036) |
| **Flujo de Trabajo** | Infra/BD → Backend → Frontend → QA (secuencial obligatorio) |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Aplicables

| ADR | Impacto |
|-----|--------|
| ADR-009 (PostgreSQL + pgvector) | Las 2 tablas nuevas DEBEN usar sintáxis PostgreSQL nativa. UUID v4 para PKs con `gen_random_uuid()`. |
| ADR-015 (PostgreSQL Modeling Governance) | Prefijo obligatorio `ibpms_*` para tablas de negocio. BIGINT AUTO_INCREMENT está PROHIBIDO. |
| ADR-001 (Hexagonal Architecture) | Las tablas son responsabilidad del Driven Adapter (infraestructura). No impactan dominio. |

### Confirmación de Stack
- **Motor BD:** PostgreSQL 15+ (Docker, puerto 5433)
- **Migraciones:** Liquibase (SQL nativo PostgreSQL)
- **Naming:** `ibpms_*` prefix, UUID PKs, `gen_random_uuid()`
- **Prohibiciones:** No H2, no MySQL, no BIGINT AUTO_INCREMENT, no SQL directo sin Liquibase

### Trazabilidad
Las 2 tablas nuevas (`ibpms_bpmn_lane` y `ibpms_lane_role_assignment`) extienden el modelo relacional existente referenciando `ibpms_bpmn_process_design(id)` y `ibpms_security_role(id)` — ambas tablas confirmadas con UUID PK en el código real. Esto cumple con ADR-015 (Dual-Schema Zero-Exceptions: solo `ibpms_*`, nunca `ACT_*`) y ADR-009 (PostgreSQL como único motor).

---

## 3. Rutas Exactas y Contexto Preexistente

### Archivos a MODIFICAR

| Archivo | Ruta | Acción |
|---------|------|--------|
| `db.changelog-master.yaml` | `backend/ibpms-core/src/main/resources/db/changelog/db.changelog-master.yaml` | Agregar `include` de la nueva migración al final |

### Archivos a CREAR

| Archivo | Ruta |
|---------|------|
| `060-lane-role-assignment-tables.sql` | `backend/ibpms-core/src/main/resources/db/changelog/changes/060-lane-role-assignment-tables.sql` |

> ⚠️ **NOTA SOBRE NUMERACIÓN:** La última migración existente es `061-bugj02006-seed-permissions.sql`. Verifica con `ls` el número más alto en `db/changelog/changes/` y usa el siguiente número secuencial. NO sobrescribas migraciones existentes.

### Contexto Preexistente (tablas referenciadas en FKs)

**`ibpms_bpmn_process_design`** (changeset `07-create-bpmn-design-tables.sql`):
- PK: `id UUID`
- Contiene: `name`, `technical_id` (UNIQUE), `status`, `xml_draft`, etc.
- Entity JPA: `BpmnProcessDesignEntity.java` con `@Table(name = "ibpms_bpmn_process_design")`

**`ibpms_security_role`** (changeset `29-consolidate-roles.sql`):
- PK: `id UUID` (con `@GeneratedValue(strategy = GenerationType.AUTO)`)
- Contiene: `name` (UNIQUE), `description`, `type`, `process_definition_id` (VARCHAR), `lane_id` (VARCHAR), `is_template`, `source`, `is_active`
- Entity JPA: `RoleEntity.java` con `@Table(name = "ibpms_security_role")`

---

## 4. Snippets Prescriptivos (DDL Exacto)

### Archivo: `060-lane-role-assignment-tables.sql` (o el número secuencial correcto)

```sql
-- Changeset: 060-lane-role-assignment-tables
-- Author: ibpms-architect
-- US-005/US-036 Extension: Lane Actor Assignment + RBAC Lane Integration
-- @Traceability: US-005/US-036 - ADR-009, ADR-015

-- ============================================================
-- Tabla 1: Lanes BPMN como entidad de primer nivel
-- Permite registrar lanes del XML BPMN como filas consultables
-- ============================================================
CREATE TABLE IF NOT EXISTS ibpms_bpmn_lane (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_design_id UUID NOT NULL,
    lane_xml_id       VARCHAR(150) NOT NULL,
    lane_name         VARCHAR(255) NOT NULL,
    actor_description VARCHAR(500),
    linked_role_id    UUID,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lane_process FOREIGN KEY (process_design_id)
        REFERENCES ibpms_bpmn_process_design(id) ON DELETE CASCADE,
    CONSTRAINT fk_lane_linked_role FOREIGN KEY (linked_role_id)
        REFERENCES ibpms_security_role(id) ON DELETE SET NULL,
    CONSTRAINT uq_lane_per_process UNIQUE (process_design_id, lane_xml_id)
);

CREATE INDEX idx_bpmn_lane_process ON ibpms_bpmn_lane(process_design_id);

-- ============================================================
-- Tabla 2: Asignación Lane↔Rol (Many-to-Many con granularidad I/E)
-- Un rol puede tener permisos Initiate y/o Execute por lane
-- ============================================================
CREATE TABLE IF NOT EXISTS ibpms_lane_role_assignment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lane_id         UUID NOT NULL,
    role_id         UUID NOT NULL,
    can_initiate    BOOLEAN NOT NULL DEFAULT false,
    can_execute     BOOLEAN NOT NULL DEFAULT true,
    assigned_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    assigned_by     VARCHAR(255),
    CONSTRAINT fk_lra_lane FOREIGN KEY (lane_id)
        REFERENCES ibpms_bpmn_lane(id) ON DELETE CASCADE,
    CONSTRAINT fk_lra_role FOREIGN KEY (role_id)
        REFERENCES ibpms_security_role(id) ON DELETE CASCADE,
    CONSTRAINT uq_lane_role UNIQUE (lane_id, role_id)
);

CREATE INDEX idx_lra_lane ON ibpms_lane_role_assignment(lane_id);
CREATE INDEX idx_lra_role ON ibpms_lane_role_assignment(role_id);
```

### Registro en `db.changelog-master.yaml`

Agregar al final del archivo (después del último include existente):

```yaml
  - include:
      file: changes/060-lane-role-assignment-tables.sql
      relativeToChangelogFile: true
```

> ⚠️ **IMPORTANTE:** Ajustar el nombre del archivo si el número secuencial es diferente.

---

## 5. Matriz de QA y Testing Atómico

| Test ID | Validación | Criterio PASS |
|---------|-----------|---------------|
| INF-01 | `mvn clean compile` exitoso | Build sin errores de Liquibase |
| INF-02 | Spring Boot arranca sin errores | `Tomcat started on port 8080` en logs |
| INF-03 | Tabla `ibpms_bpmn_lane` existe | `SELECT * FROM ibpms_bpmn_lane LIMIT 1;` no da error |
| INF-04 | Tabla `ibpms_lane_role_assignment` existe | `SELECT * FROM ibpms_lane_role_assignment LIMIT 1;` no da error |
| INF-05 | FK a `ibpms_bpmn_process_design` funciona | INSERT con `process_design_id` inválido falla con FK violation |
| INF-06 | FK a `ibpms_security_role` funciona | INSERT con `role_id` inválido falla con FK violation |
| INF-07 | UNIQUE constraint funciona | INSERT duplicado `(process_design_id, lane_xml_id)` falla |

Validación de esquema obligatoria: Ejecuta verificaciones de sintáxis Liquibase o configuraciones docker-compose antes de hacer push.

---

## 6. Mensaje de Despacho

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> 📝 **POLÍTICA ANTIAMNESIA:** Antes de iniciar, lee `docs/architecture/arquitecturar.md` para re-entrenar tu contexto sobre la arquitectura del proyecto. NO asumas cómo funciona el sistema — léelo.

> 📋 **DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_INFRA.md`.
> 4. Al grabar el archivo, deténte y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_INFRA.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
> 6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
> 7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `feature/lane-role-assignment`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

### Archivos INTOCABLES (Blast Radius = 0)
- `Workdesk.vue`, `Login.vue`, `FormDesigner.vue`, `FormRenderer`
- `router/index.ts`, `docker-compose.yml`
- Todas las migraciones Liquibase existentes (001-059+)
- Tests E2E existentes (J-02, J-04)
