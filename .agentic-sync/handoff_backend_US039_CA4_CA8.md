# Handoff Backend — US-039 CA-4 al CA-8
## Iteración 72-DEV | Rama: `sprint-3/informe_auditoriaSprint1y2`

---

## Contexto Estratégico
La US-039 define el **Formulario Genérico Base (Pantalla 7.B)**: un formulario comodín minimalista que la plataforma inyecta automáticamente en tareas operativas simples donde no se justifica un iForm Maestro completo. Es el "Plan B" del sistema de formularios.

Los CA-4 al CA-8 son **remediaciones post-auditoría** que cierran 5 GAPs de implementación detectados en `us039_functional_analysis.md`.

---

## Criterios de Aceptación Asignados

### CA-4 — Definición del Cuerpo Editable del Formulario Genérico
**Responsabilidades Backend:**
- Definir el **DTO de respuesta** del BFF `GET /api/v1/workbox/tasks/{id}/generic-form-context` que incluya:
  - `schema`: JSON Schema con exactamente 3 campos fijos:
    1. `observations` (string, min: 10, max: 2000, required: true)
    2. `attachments` (array de UUIDs, max: 5 items, required: false)
    3. `management_result` (enum string, required: true)
  - `prefillData`: variables de solo lectura del proceso (filtradas por Whitelist del CA-5)
  - `allowedResults`: array de opciones configurables por proceso (ej: `["APPROVED", "REJECTED", "PENDING_INFO", "ESCALATED"]`)
- Definir el **DTO de submit** para `POST /api/v1/workbox/tasks/{id}/generic-form-complete`:
  - `observations` (string)
  - `attachmentUuids` (array<UUID>)
  - `managementResult` (string enum)
  - `panicAction` (string enum nullable: `APPROVED | RETURNED | CANCELLED`)
  - `panicJustification` (string, min: 20, nullable — obligatorio si `panicAction != null`)
- **Validación Backend** (json-schema-validator): replicar las mismas reglas Zod del Frontend. Rechazar con HTTP 400 si el schema no se cumple.
- **Restricción arquitectónica**: el endpoint TIENE PROHIBIDO aceptar campos adicionales fuera del schema (aplicar `.strip()` o 400).

### CA-5 — Configuración de Whitelist Regex por Proceso
**Responsabilidades Backend:**
- En la tabla `process_definitions` (o metadata del deployment), persiste un campo `generic_form_whitelist` (JSONB array de strings, max 10 claves).
- El BFF `GET /generic-form-context` aplica este filtro sobre las variables de Camunda antes de inyectar `prefillData`.
- Si `generic_form_whitelist` es `null` o vacío, aplicar fallback: `["Case_ID", "Instance_Name", "Priority", "Created_At"]`.
- **Blacklist mandatoria**: SIEMPRE excluir variables con prefijo `_internal_`, `camunda_`, `zeebe_` independientemente de la whitelist.
- El endpoint `PUT /api/v1/design/processes/{processKey}/generic-form-config` permite al Arquitecto configurar la whitelist (max 10 claves).

### CA-6 — Catálogo Configurable de Roles VIP para Bloqueo Pre-Flight
**Responsabilidades Backend:**
- **Migración Liquibase**: agregar columna `is_vip_restricted BOOLEAN DEFAULT FALSE` a tabla `ibpms_roles`.
- **Seed Data**: marcar como `is_vip_restricted = true` los roles: `ALTA_DIRECCION`, `APROBADOR_FINANCIERO`, `SELLO_LEGAL`.
- **Pre-Flight Analyzer** (`/api/v1/design/processes/pre-flight`): al evaluar cada UserTask, consultar el Lane asignado → consultar `ibpms_roles` para verificar si el rol del Lane tiene `is_vip_restricted = true`. Si es así y el FormKey es `sys_generic_form` → incluir un Error ❌ Hard-Stop en el reporte.
- Exponer endpoint `GET /api/v1/admin/roles?vip_restricted=true` para que el Frontend pueda listar los roles VIP configurados.

### CA-7 — Persistencia y Auto-Guardado del Formulario Genérico
**Responsabilidades Backend:**
- Reutilizar los endpoints existentes de borradores de US-029:
  - `POST /api/v1/drafts/{taskId}` — auto-guardado (body: `{ observations?, attachmentUuids?, managementResult?, currentStep? }`)
  - `GET /api/v1/drafts/{taskId}` — recuperación
  - `DELETE /api/v1/drafts/{taskId}` — limpieza post-submit
- En la persistencia de submit exitoso, invocar `runtimeService.setVariables(executionId, variables)` con:
  - `generic_form_observations`: texto
  - `generic_form_result`: enum del resultado
  - `generic_form_attachments`: JSON array de UUIDs
- Aplicar validación de Implicit Locking: solo el `assignee` puede guardar borradores o hacer submit.

### CA-8 — Mapeo Explícito de Botones de Pánico a Eventos BPMN
**Responsabilidades Backend:**
- Dentro de `POST /generic-form-complete`, evaluar el campo `panicAction`:
  - `APPROVED`: `taskService.complete(taskId, { generic_form_result: "APPROVED", ...variables })`
  - `RETURNED`: `taskService.complete(taskId, { generic_form_result: "RETURNED", ...variables })` — El BPMN debe evaluar esta variable en un Exclusive Gateway posterior.
  - `CANCELLED`: `runtimeService.throwBpmnError(taskId, "TASK_CANCELLED_BY_OPERATOR", panicJustification)` — Si no existe Boundary Event, Camunda propaga a morgue de incidentes.
- Validar que `panicJustification.length >= 20` cuando `panicAction != null`. Rechazar con HTTP 400 si no cumple.
- Registrar en audit log: `{ action: panicAction, justification: panicJustification, userId, taskId, timestamp }`.

---

## Restricciones Arquitectónicas
1. **Arquitectura Hexagonal**: DTOs en `adapter`, lógica en `domain/service`, puertos en `domain/ports`.
2. **No tocar** variables de Camunda masivas (Ley de Exclusión Topológica US-029 CA-16).
3. **No usar** `git stash`. Solo `git commit` + `git push`.
4. Compilación Docker obligatoria antes de push.

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en tu propia rama de sprint. Queda estrictamente prohibido usar git stash.
