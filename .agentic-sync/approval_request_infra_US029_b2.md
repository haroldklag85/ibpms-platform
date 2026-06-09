# 🏛️ Approval Request: Infraestructura/DB US-029 (Bloque 2)

## 📌 Resumen de Cambios

Se ha completado la validación requerida para el Bloque 2 (INFRA-029-05).

1. **Verificación Estructural:** Se inspeccionó la tabla `ibpms_agile_tasks`. Se confirmó que **no** contaba con una columna `draft_updated_at` ni `draft_expires_at`.
2. **Creación del Changeset:** Se procedió a crear un nuevo script de Liquibase (`40-us029-draft-expiration.sql`) para agregar la columna faltante e inyectar un índice parcial que optimice futuras limpiezas o consultas de vigencia:
   - Añadida columna `draft_expires_at TIMESTAMP`.
   - Añadido comentario explicativo (TTL de 72h / Warning 48h).
   - Añadido `CREATE INDEX idx_agile_tasks_draft_expires ON ibpms_agile_tasks(draft_expires_at) WHERE draft_expires_at IS NOT NULL`.
3. **Registro en Master Changelog:** Se registró el nuevo changeset exitosamente en `db.changelog-master.yaml`.

## 🛠️ Archivos Modificados / Creados
- `backend/ibpms-core/src/main/resources/db/changelog/40-us029-draft-expiration.sql` (CREADO)
- `backend/ibpms-core/src/main/resources/db/changelog/db.changelog-master.yaml` (ACTUALIZADO)

---
**Status:** `READY_FOR_BACKEND`
