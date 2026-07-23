# 🏛️ HANDOFF DE REMEDIACIÓN — US-029: Ejecución y Envío de Formulario
# BLOQUE 1: Remediación Crítica (14 GAPs prioritarios)
**Fecha:** 2026-05-02 | **Iteración:** 5 | **Arquitecto Líder:** Antigravity

---

## 🔵 AGENTE INFRA/DB

**Objetivo:** Garantizar que el esquema de base de datos soporte validación de archivos temporales, drafts con TTL, y campos condicionales.

### Tareas:

**INFRA-029-01: Tabla `ibpms_temp_documents` (CA-09, CA-13, CA-28)**
- Crear changeset Liquibase para la tabla de archivos temporales:
```sql
CREATE TABLE ibpms_temp_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    mime_detected VARCHAR(100),       -- Magic bytes MIME real
    file_size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',  -- PENDING, CONFIRMED, ORPHANED
    uploaded_at TIMESTAMP DEFAULT NOW(),
    confirmed_at TIMESTAMP,
    CONSTRAINT fk_temp_doc_task FOREIGN KEY (task_id) REFERENCES ibpms_agile_tasks(id) ON DELETE CASCADE
);
CREATE INDEX idx_temp_docs_task_user ON ibpms_temp_documents(task_id, user_id);
CREATE INDEX idx_temp_docs_status ON ibpms_temp_documents(status, uploaded_at);
```

**INFRA-029-02: Agregar columna `visible_fields` a `ibpms_form_events` (CA-34)**
- En la tabla de eventos CQRS, agregar:
```sql
ALTER TABLE ibpms_form_events ADD COLUMN visible_fields JSONB;
```
- Este campo almacenará el array `_visibleFields` para auditoría forense de campos condicionales.

**INFRA-029-03: Verificar tabla `task_drafts` existente (CA-24)**
- Confirmar que la tabla `task_drafts` o el equivalente en `ibpms_agile_tasks` (columnas `draft_payload`, `draft_payload_hash`) soporta el flujo de Merge Commit.
- Si `draft_payload` es VARCHAR, migrar a JSONB para soporte nativo de queries parciales.
- Agregar columna `draft_updated_at TIMESTAMP` para TTL de 72h si no existe.

**INFRA-029-04: Cron Job para limpieza de archivos huérfanos (CA-13)**
- Crear un scheduled changeset o documentar la necesidad de un `@Scheduled` que ejecute:
```sql
DELETE FROM ibpms_temp_documents WHERE status = 'PENDING' AND uploaded_at < NOW() - INTERVAL '24 hours';
```

**Entregable:** Reportar en `.agentic-sync/approval_request_infra_US029.md` con el listado de changesets creados y las verificaciones realizadas.

---

## 🟢 AGENTE BACKEND

**Objetivo:** Remediar 7 GAPs críticos de seguridad y validación en el flujo de completado de formularios.

### Contexto de Código Existente:
- `FormBffCoreService.java` → BFF Mega-DTO + completeTransactionalForm() con Exclusión Topológica YA implementada (L107-110 envía minifiedDto a Camunda)
- `CompletarTareaService.java` → Implicit Locking + Idempotencia funcional
- `FormCompletionService.java` → CQRS Event Sourcing + Saga Rollback + PII Encryption + Auto-Claim + Draft cleanup → este es el servicio MÁS MADURO
- `TaskDraftService.java` → Borrador con hash MD5 anti-duplicados, PERO importa directamente `AgileTaskRepositoryJpa` (violación hexagonal)
- `S3DocumentController.java` → Scaffold MOCK sin lógica real. Solo retorna UUID hardcodeado.
- `WorkboxTaskController.java` → Endpoints `/draft` y `/complete` activos, delegando a `TaskDraftService`

### Tareas:

**BACK-029-01: Validación JSON Schema en Backend (GAP-01, CA-02)**
- En `FormCompletionService.completeTask()` o `FormBffCoreService.completeTransactionalForm()`, ANTES de persistir el evento CQRS:
  - Recuperar el schema Zod/JSON Schema asociado al formulario desde `ibpms_form_definitions`
  - Usar la librería `org.everit.json.schema` o `com.networknt:json-schema-validator` para validar el payload contra el schema
  - Si falla, retornar HTTP 400 con el formato:
    ```json
    {"error": "ValidationFailed", "fields": [{"field": "monto_aprobado", "message": "Required"}]}
    ```
  - Agregar la dependencia Maven si no existe: `com.networknt:json-schema-validator:1.0.87`

**BACK-029-02: Exclusión Topológica en CompletarTareaService (GAP-09, CA-16)**
- `CompletarTareaService.completar()` L44 actualmente hace `taskService.complete(taskId, variables)` pasando TODO el payload
- Refactorizar para que solo envíe variables de gateway (las que necesitan los XOR/OR Gateways del BPMN)
- Crear un método `extractGatewayVariables(Map<String, Object> fullPayload)` que filtre solo las keys declaradas como variables de decisión
- El payload completo ya se persiste en el Event Sourcing de FormCompletionService (que sí lo hace bien)
- NOTA: `FormBffCoreService.completeTransactionalForm()` L107-110 YA implementa esto correctamente con `minifiedDto`. El problema es `CompletarTareaService` que es la ruta alternativa.

**BACK-029-03: Endpoint Upload-First Real (GAP-04, CA-09)**
- Refactorizar `S3DocumentController.uploadTempDocument()` para que:
  - Acepte `@RequestParam("file") MultipartFile file` 
  - Valide tamaño (max 25MB — CA-28): `if (file.getSize() > 25 * 1024 * 1024) throw 400`
  - Valide extensión contra whitelist: `.pdf, .jpg, .jpeg, .png, .gif, .docx, .xlsx, .pptx, .txt, .csv` (CA-28)
  - Almacene en disco local (`/tmp/ibpms-uploads/{taskId}/{uuid}`) en V1 (sin S3 real)
  - Registre en tabla `ibpms_temp_documents` con user_id, task_id, original_filename, content_type, file_size
  - Retorne `{ "temp_id": "uuid-generado", "filename": "contrato.pdf", "size": 8200000 }`
- Agregar `@RequestHeader("X-Task-Id") String taskId` para vincular archivo a tarea

**BACK-029-04: Validación MIME Magic Bytes (GAP-18, CA-28)**
- En el endpoint de upload, DESPUÉS de recibir el archivo:
  - Usar Apache Tika (`org.apache.tika:tika-core:2.9.1`) para detectar el MIME real del archivo por sus magic bytes
  - Si el MIME detectado no coincide con la extensión declarada (ej: .pdf con MIME `application/x-msdownload`), retornar HTTP 415:
    ```json
    {"error": "UnsupportedMediaType", "message": "El tipo real del archivo no coincide con la extensión declarada."}
    ```

**BACK-029-05: Defensa Anti-IDOR en Archivos (GAP-06, CA-13)**
- En `FormCompletionService.completeTask()`, cuando el payload referencia UUIDs de archivos:
  - Para cada UUID, verificar en `ibpms_temp_documents` que:
    1. `user_id` coincida con el usuario autenticado
    2. `task_id` coincida con la tarea que se está completando
    3. `status = 'PENDING'` (no fue ya confirmado en otra tarea)
  - Si cualquier UUID falla la validación, abortar con HTTP 403: `"Archivo no autorizado para esta tarea"`
  - Al confirmar, cambiar `status = 'CONFIRMED'` y setear `confirmed_at`

**BACK-029-06: Validación de Campos Condicionales (GAP-24, CA-34)**
- En el validador JSON Schema del BACK-029-01:
  - Aceptar un campo especial `_visibleFields: string[]` en el payload
  - Recalcular las condiciones de visibilidad de forma independiente usando los valores del payload
  - Si `_visibleFields` omite un campo que según las reglas SÍ debería ser visible y obligatorio, retornar HTTP 400
  - Almacenar `_visibleFields` en el evento CQRS para trazabilidad forense

**BACK-029-07: Violación Hexagonal en TaskDraftService (DEUDA TÉCNICA)**
- `TaskDraftService.java` L6 importa directamente `AgileTaskRepositoryJpa` (infraestructura)
- Refactorizar para inyectar un puerto `AgileTaskPort` en su lugar
- Este cambio es idéntico al patrón ya aplicado en `LiberarTareaService` con `ClaimAuditPort`

**Gate de Validación:** Ejecutar `mvn compile` y verificar exit code 0.
**Entregable:** Reportar en `.agentic-sync/approval_request_backend_US029.md`

---

## 🟡 AGENTE FRONTEND

**Objetivo:** Remediar 7 GAPs de UX y seguridad en la experiencia de ejecución de formularios.

### Contexto de Código Existente:
- `genericFormStore.ts` → Store funcional con autoSave debounce 10s, draft recovery, panic buttons, sync indicators
- El store usa `apiClient.post('/workbox/tasks/${taskId}/generic-form-complete', formData, {multipart})` → envía archivos como multipart (VIOLA CA-09 Upload-First)
- No existe un componente Vue dedicado para la Pantalla 2 de iForm Maestro (solo el formulario genérico)

### Tareas:

**FRONT-029-01: Overlay + Feedback Visual Durante Submit (GAP-12, CA-20)**
- Crear un composable `useSubmitFeedback.ts` en `composables/workdesk/`:
  - Estado reactivo: `phase: 'idle' | 'validating' | 'saving' | 'success' | 'error'`
  - Al hacer clic [Enviar]: 
    1. Deshabilitar botón, cambiar texto a "Enviando..." con spinner
    2. Overlay semitransparente (bg-black/30 backdrop-blur-sm) sobre todo el formulario
    3. Texto debajo del spinner: "Validando datos..." → "Guardando en el servidor..."
  - Si HTTP 400/500: retirar overlay, reactivar botón, mostrar errores en rojo
  - NUNCA pantalla blanca ni pérdida de datos

**FRONT-029-02: Confirmación Post-Submit + Redirect (GAP-13, CA-21)**
- Al recibir HTTP 200 del `/complete`:
  - Reemplazar overlay por pantalla de confirmación: checkmark verde animado ✅ + "¡Tarea completada exitosamente!" + ID de tarea
  - Mostrar botón "Ir al Workdesk" para redirección inmediata
  - Auto-redirect al Workdesk después de 3 segundos
  - DURANTE esos 3 segundos ejecutar RYOW (GAP-10) en paralelo

**FRONT-029-03: RYOW — Purga Síncrona Post-Submit (GAP-10, CA-17)**
- Al recibir HTTP 200:
  1. `localStorage.removeItem('generic_draft_' + taskId)` — purga borrador
  2. Eliminar la tarea del store de Pinia del Workdesk (`useWorkdeskStore().items.splice(...)`)
  3. Esto evita que el usuario vea su tarea "completada" flotando como fantasma
- NOTA: `genericFormStore.clearDraft()` ya limpia localStorage y servidor. Integrar con RYOW de Pinia.

**FRONT-029-04: Lazy Patching Campos Nuevos (GAP-03, CA-08)**
- En el componente que renderiza el formulario:
  - Comparar `prefillData` (datos históricos de Camunda) contra el schema Zod (campos del formulario)
  - Si existen campos obligatorios nuevos que no están en `prefillData` (valor null/undefined):
    - Pintar esos inputs con borde rojo pulsante + ícono ⚠️
    - Deshabilitar botón [Enviar] hasta que todos los campos nuevos tengan valor
  - Texto tooltip: "Este campo fue añadido en una versión reciente del formulario."

**FRONT-029-05: Migrar Submit a Upload-First (GAP-04, CA-09)**
- Refactorizar `genericFormStore.submitForm()` (L159-192):
  - ANTES del submit, subir cada archivo individualmente con `POST /api/v1/documents/upload-temp`
  - Recoger el `temp_id` de cada respuesta
  - En el POST `/complete`, enviar SOLO los UUIDs: `{ "attachments": ["uuid-1", "uuid-2"] }`
  - ELIMINAR el envío multipart/form-data al endpoint de completado
  - El endpoint `/complete` solo debe recibir JSON plano

**FRONT-029-06: Schema Version Conflict Modal (GAP-17, CA-27)**
- En el catch del submit, si el error es HTTP 409 con `error: "SchemaVersionConflict"`:
  - Mostrar modal informativo (NO destruir datos): "El formulario fue actualizado con nuevos campos obligatorios. Tus datos están seguros."
  - Al aceptar: recargar Mega-DTO BFF con la nueva versión, reinyectar datos del operario como prefillData
  - Los datos se preservan del LocalStorage/Draft

**FRONT-029-07: Feedback Upload con Barra Progreso (GAP-19, CA-29)**
- En el componente de carga de archivos:
  - Barra de progreso horizontal con porcentaje (usando `onUploadProgress` de Axios)
  - Nombre y tamaño del archivo visible durante la carga
  - Botón [✕ Cancelar] que aborta con `AbortController`
  - Al completar: chip verde con checkmark ✅ y botón [🗑️ Eliminar]
  - Si falla: mensaje rojo "No se pudo subir el archivo. ¿Reintentar?" con botón [Reintentar]

**Gate de Validación:** Ejecutar `npm run build` y verificar exit code 0.
**Entregable:** Reportar en `.agentic-sync/approval_request_frontend_US029.md`

---

## 🔴 AGENTE QA

**Objetivo:** Certificar los 14 GAPs remediados del Bloque 1 mediante revisión de código y trazabilidad arquitectónica.

### Matriz de Escenarios a Validar:

| ID | Escenario | CA | Validación Esperada |
|----|-----------|-----|---------------------|
| QA-029-01 | POST `/complete` con payload vacío → HTTP 400 con fields[] | CA-02 | Verificar que `FormCompletionService` o `FormBffCoreService` invoca JSON Schema validator antes de CQRS persist |
| QA-029-02 | POST `/complete` con campo numérico como string → HTTP 400 | CA-02 | Error específico de tipo de dato en respuesta |
| QA-029-03 | `CompletarTareaService.completar()` NO pasa ALL variables a Camunda | CA-16 | Verificar que solo envía `gatewayVariables` (max 5 keys) y NO el payload masivo |
| QA-029-04 | POST `/documents/upload-temp` con archivo > 25MB → HTTP 400 | CA-28 | Validación ANTES de almacenar |
| QA-029-05 | POST `/documents/upload-temp` con .exe renombrado a .pdf → HTTP 415 | CA-28 | Apache Tika detecta magic bytes |
| QA-029-06 | POST `/documents/upload-temp` con extensión no permitida → HTTP 400 | CA-28 | Whitelist: pdf,jpg,png,docx,xlsx,pptx,txt,csv |
| QA-029-07 | POST `/complete` con UUID de archivo de OTRO usuario → HTTP 403 | CA-13 | Anti-IDOR: verificar user_id + task_id en ibpms_temp_documents |
| QA-029-08 | POST `/complete` con `_visibleFields` adulterado → HTTP 400 | CA-34 | Backend recalcula condiciones y detecta inconsistencia |
| QA-029-09 | `TaskDraftService` NO importa `AgileTaskRepositoryJpa` | HEXA | Verificar imports: solo puertos de dominio |
| QA-029-10 | `FormBffCoreService.completeTransactionalForm()` envía minifiedDto a Camunda | CA-16 | L107-110: solo `formApproved` + `form_storage_id` |
| QA-029-11 | Frontend: botón [Enviar] deshabilitado durante submit + overlay visible | CA-20 | Verificar que existe componente `useSubmitFeedback` |
| QA-029-12 | Frontend: confirmación ✅ + redirect 3s + purga localStorage | CA-17/21 | Verificar RYOW en composable |
| QA-029-13 | Frontend: submit envía JSON plano (NO multipart) al `/complete` | CA-09 | Archivos se suben por separado con Upload-First |
| QA-029-14 | Frontend: HTTP 409 → modal de conflicto (NO destruye datos) | CA-27 | Modal informativo + recarga BFF |

**Entregable:** Reportar en `.agentic-sync/approval_request_qa_US029.md`
