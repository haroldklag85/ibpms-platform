# 🟠 Handoff Infra/BD — US-025 Fase 1B: Liquibase Menu Topology

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** Infra/BD  
> **Prioridad:** 🟡 P1  
> **Rama Git:** sprint-6  
> **Pre-requisito:** Fase 0 completada  
> **Ejecución:** En PARALELO con Fase 1A (Frontend) + coordinado con Backend Fase 1B  
> **Gate de Salida:** Liquibase `update` exitoso + seed data verificable con query SQL

---

## 1. Contexto

El `MenuLayoutUseCase.java` tiene todos los menús y su mapeo de roles hardcodeados en código Java (101 líneas de if/else). Esto viola ADR-001 (Hexagonal) y ADR-009 (PostgreSQL como único store). Se requiere crear la tabla `ibpms_menu_topology` para persistir esta configuración de forma data-driven.

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-009 (PostgreSQL + Liquibase) | Toda persistencia DEBE usar changesets Liquibase. Prohibido DDL manual |
| ADR-001 (Hexagonal) | La resolución menú↔rol se desacopla del código al trasladarse a PostgreSQL |

---

## 3. Tareas

### Tarea 1B.1 — Changeset Liquibase `ibpms_menu_topology`

**Crear:** `backend/ibpms-core/src/main/resources/db/changelog/changes/XX-us025-menu-topology.sql`

**DDL de la tabla:**

```sql
--liquibase formatted sql
--changeset architect:us025-menu-topology-create

CREATE TABLE ibpms_menu_topology (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label           VARCHAR(100) NOT NULL,
    icon            VARCHAR(50)  NOT NULL,
    path            VARCHAR(200),
    parent_id       UUID REFERENCES ibpms_menu_topology(id) ON DELETE CASCADE,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    required_roles  JSONB,  -- NULL = acceso universal, ["ROLE_SUPER_ADMIN"] = solo admin
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_menu_topology_parent ON ibpms_menu_topology(parent_id);
CREATE INDEX idx_menu_topology_roles ON ibpms_menu_topology USING GIN (required_roles);

COMMENT ON TABLE ibpms_menu_topology IS 'US-025 CA-6: Topología dinámica del menú lateral del App Shell. Resolución menú↔rol data-driven.';
COMMENT ON COLUMN ibpms_menu_topology.required_roles IS 'JSONB array de roles requeridos. NULL = acceso universal. Ejemplo: ["ROLE_SUPER_ADMIN", "ROLE_CISO"]';
COMMENT ON COLUMN ibpms_menu_topology.parent_id IS 'FK auto-referencial para jerarquía de carpetas de menú';
```

### Tarea 1B.1b — Seed Data (Migrar datos hardcodeados)

Crear changeset de seed data que reproduzca **exactamente** los menús que hoy están hardcodeados en `MenuLayoutUseCase.java`:

```sql
--changeset architect:us025-menu-topology-seed

-- RAMA 0: Home (Acceso Universal)
INSERT INTO ibpms_menu_topology (label, icon, path, sort_order, required_roles)
VALUES ('Inicio', 'mdi-home', '/home', 0, NULL);

-- RAMA 1: Mi Workdesk (Acceso Universal)
INSERT INTO ibpms_menu_topology (label, icon, path, sort_order, required_roles)
VALUES ('Mi Workdesk', 'mdi-desktop-mac', '/workdesk', 10, NULL);

-- RAMA 2: Aprobaciones Pendientes
INSERT INTO ibpms_menu_topology (label, icon, path, sort_order, required_roles)
VALUES ('Aprobaciones Pendientes', 'mdi-check-decagram', '/approvals', 20, '["ROLE_APROBADOR_FINANCIERO", "ROLE_ALTA_DIRECCION"]');

-- RAMA 3: Administración y Gobernanza (Carpeta)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES ('a0000000-0000-0000-0000-000000000001', 'Administración y Gobernanza', 'mdi-cog-box', NULL, 30, NULL);

-- Hijos de Administración y Gobernanza
INSERT INTO ibpms_menu_topology (label, icon, path, parent_id, sort_order, required_roles)
VALUES 
  ('Generador de Entidades MDE', 'mdi-database-plus', '/config/mde', 'a0000000-0000-0000-0000-000000000001', 31, '["ROLE_SUPER_ADMIN"]'),
  ('Centro de IA (MLOps)', 'mdi-brain', '/config/ai-center', 'a0000000-0000-0000-0000-000000000001', 32, '["ROLE_SUPER_ADMIN"]'),
  ('Gestor de Festivos', 'mdi-calendar-alert', '/config/holidays', 'a0000000-0000-0000-0000-000000000001', 33, '["ROLE_SUPER_ADMIN"]'),
  ('Tablero de Anomalías de Seguridad', 'mdi-shield-alert', '/security/anomalies', 'a0000000-0000-0000-0000-000000000001', 34, '["ROLE_CISO", "ROLE_SUPER_ADMIN"]'),
  ('Matriz Transaccional SoD', 'mdi-file-tree', '/security/sod-matrix', 'a0000000-0000-0000-0000-000000000001', 35, '["ROLE_CISO", "ROLE_SUPER_ADMIN"]');

-- RAMA: Service Delivery (Carpeta)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES ('a0000000-0000-0000-0000-000000000002', 'Service Delivery', 'mdi-account-group', NULL, 40, '["ROLE_SUPER_ADMIN", "Global Admin"]');

INSERT INTO ibpms_menu_topology (label, icon, path, parent_id, sort_order, required_roles)
VALUES 
  ('Triaje Intake', 'mdi-filter', '/intake-triage', 'a0000000-0000-0000-0000-000000000002', 41, NULL),
  ('Intake Manual', 'mdi-text-box-plus', '/admin/intake', 'a0000000-0000-0000-0000-000000000002', 42, NULL),
  ('Customer 360', 'mdi-account-details', '/admin/customer360', 'a0000000-0000-0000-0000-000000000002', 43, NULL);

-- RAMA: Project Builder (Carpeta)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES ('a0000000-0000-0000-0000-000000000003', 'Project Builder', 'mdi-rocket', NULL, 50, '["ROLE_SUPER_ADMIN", "Global Admin"]');

INSERT INTO ibpms_menu_topology (label, icon, path, parent_id, sort_order, required_roles)
VALUES 
  ('Project Builder', 'mdi-hammer-wrench', '/admin/project-builder', 'a0000000-0000-0000-0000-000000000003', 51, NULL),
  ('Gestor de Proyectos', 'mdi-view-dashboard-variant', '/admin/projects/manager', 'a0000000-0000-0000-0000-000000000003', 52, NULL),
  ('Agile Hub', 'mdi-chart-timeline-variant', '/admin/projects/agile-hub', 'a0000000-0000-0000-0000-000000000003', 53, NULL);

-- RAMA: Analytics & BAM (Carpeta)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES ('a0000000-0000-0000-0000-000000000004', 'Analytics & BAM', 'mdi-chart-bar', NULL, 60, '["ROLE_SUPER_ADMIN", "Global Admin"]');

INSERT INTO ibpms_menu_topology (label, icon, path, parent_id, sort_order, required_roles)
VALUES 
  ('Dashboard BAM', 'mdi-monitor-dashboard', '/admin/analytics/bam', 'a0000000-0000-0000-0000-000000000004', 61, NULL);

-- RAMA: Integration Hub (Carpeta)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES ('a0000000-0000-0000-0000-000000000005', 'Integration Hub', 'mdi-api', NULL, 70, '["ROLE_SUPER_ADMIN", "Global Admin"]');

INSERT INTO ibpms_menu_topology (label, icon, path, parent_id, sort_order, required_roles)
VALUES 
  ('Catálogo de Conectores', 'mdi-book-open-page-variant', '/admin/integration/catalog', 'a0000000-0000-0000-0000-000000000005', 71, NULL),
  ('Connector Builder', 'mdi-puzzle-edit', '/admin/integration/builder', 'a0000000-0000-0000-0000-000000000005', 72, NULL),
  ('Visual Mapper', 'mdi-sitemap', '/admin/integration/mapper', 'a0000000-0000-0000-0000-000000000005', 73, NULL),
  ('DLQ Dashboard', 'mdi-alert-octagon', '/admin/integration/dlq', 'a0000000-0000-0000-0000-000000000005', 74, NULL);

-- RAMA: SGDEA (Carpeta)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES ('a0000000-0000-0000-0000-000000000006', 'SGDEA', 'mdi-folder-lock', NULL, 80, '["ROLE_SUPER_ADMIN", "Global Admin"]');

INSERT INTO ibpms_menu_topology (label, icon, path, parent_id, sort_order, required_roles)
VALUES 
  ('Bóveda Documental', 'mdi-safe', '/sgdea/vault', 'a0000000-0000-0000-0000-000000000006', 81, NULL);

-- RAMA: Gobernanza (Carpeta)
INSERT INTO ibpms_menu_topology (id, label, icon, path, sort_order, required_roles)
VALUES ('a0000000-0000-0000-0000-000000000007', 'Gobernanza', 'mdi-gavel', NULL, 90, '["ROLE_SUPER_ADMIN", "Global Admin"]');

INSERT INTO ibpms_menu_topology (label, icon, path, parent_id, sort_order, required_roles)
VALUES 
  ('Gobernanza de Identidades', 'mdi-card-account-details', '/admin/security/identity', 'a0000000-0000-0000-0000-000000000007', 91, NULL),
  ('PMO / SLA Management', 'mdi-timer-settings', '/admin/pmo/settings', 'a0000000-0000-0000-0000-000000000007', 92, NULL);
```

---

## 4. Registro del Changeset

**Agregar al archivo master de changelog:** `db/changelog/db.changelog-master.yaml` (o equivalente):

```yaml
- include:
    file: changes/XX-us025-menu-topology.sql
```

**Convención de numeración:** Consultar el último changeset existente y asignar el siguiente número secuencial.

---

## 5. Criterios de Aceptación del Gate

- [ ] `liquibase update` ejecuta sin errores contra PostgreSQL local
- [ ] `SELECT COUNT(*) FROM ibpms_menu_topology` retorna ≥ 25 registros (menús seed)
- [ ] `SELECT * FROM ibpms_menu_topology WHERE required_roles @> '["ROLE_SUPER_ADMIN"]'` retorna módulos admin
- [ ] `SELECT * FROM ibpms_menu_topology WHERE required_roles IS NULL` retorna Home + Workdesk
- [ ] El índice GIN sobre `required_roles` existe: `\di ibpms_menu_topology` confirma
- [ ] Jerarquía padre-hijo funcional: `SELECT * FROM ibpms_menu_topology WHERE parent_id IS NOT NULL` retorna hijos correctos

## 6. Exclusiones

- NO crear tabla `ibpms_impersonation_audit_log` (CA-9/CA-31 diferidos a V2)
- NO crear tabla `ibpms_user_preferences` (diferida a V2)
- NO modificar tablas existentes de roles/permisos — solo crear `ibpms_menu_topology`

## 7. Archivos Impactados

| Archivo | Acción |
|---------|--------|
| `db/changelog/changes/XX-us025-menu-topology.sql` | Crear (DDL + Seed) |
| `db/changelog/db.changelog-master.yaml` | Modificar (agregar include) |
