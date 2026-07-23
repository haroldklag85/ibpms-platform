# 🏛️ Approval Request: Infraestructura/DB US-029 (Bloque 1)

## 📌 Resumen de Cambios

Se han completado y verificado exitosamente las 4 tareas asignadas al Agente Infra/DB para habilitar la ejecución robusta de formularios.

1. **INFRA-029-01 (Upload-First)**: Se creó la tabla `ibpms_temp_documents` con las restricciones y tipos adecuados (`id UUID`, `task_id VARCHAR(255)`, y soporte para `mime_detected` y tamaño de archivo), junto con los índices en `(task_id, user_id)` y `(status, uploaded_at)`.
2. **INFRA-029-02 (Campos Condicionales)**: Se agregó la columna `visible_fields JSONB` a la tabla `form_event_store` (bóveda CQRS) para soportar la trazabilidad forense.
3. **INFRA-029-03 (Borradores)**: Se verificó la tabla `task_drafts`. La tabla actual ya cuenta con un campo `partial_data JSONB` y la columna `updated_at TIMESTAMPTZ`, por lo cual soporta nativamente el flujo requerido sin requerir migraciones estructurales.
4. **INFRA-029-04 (Cron Job Cleanup)**: Se documentó como un comentario en SQL la necesidad de implementar el proceso `@Scheduled` para la limpieza de documentos efímeros que expiren luego de 24 horas.

## 🛠️ Archivos Modificados / Creados
- `backend/ibpms-core/src/main/resources/db/changelog/39-us029-form-execution-schema.sql` (CREADO)
- `backend/ibpms-core/src/main/resources/db/changelog/db.changelog-master.yaml` (ACTUALIZADO)

## ✅ Validación de Gate
- **Comando:** `mvn compile` (ejecutado dentro del contenedor Docker `ibpms-core-dev`).
- **Resultado:** `BUILD SUCCESS` (Exit code: 0).

---
**Status:** `READY_FOR_BACKEND`
