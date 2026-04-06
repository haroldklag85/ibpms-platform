# Handoff Frontend — US-039 CA-4 al CA-8
## Iteración 72-DEV | Rama: `sprint-3/informe_auditoriaSprint1y2`

---

## Contexto Estratégico
La US-039 define el **Formulario Genérico Base (Pantalla 7.B)**: un formulario comodín minimalista para tareas operativas simples donde no se justifica un iForm Maestro. Este componente Vue 3 se renderiza cuando una tarea no tiene formulario personalizado asociado o es una tarea Kanban huérfana.

---

## Criterios de Aceptación Asignados

### CA-4 — Definición del Cuerpo Editable del Formulario Genérico
**Responsabilidades Frontend:**
- Crear componente `GenericFormView.vue` (Pantalla 7.B) en `frontend/src/views/` o `frontend/src/components/forms/`.
- **Estructura visual obligatoria** (de arriba a abajo):
  1. **Cuadrícula de Metadatos Solo-Lectura** (parte superior): Renderizar los campos del `prefillData` del BFF como inputs con fondo gris `#F5F5F5`, borde sólido gris, ícono de candado 🔒, cursor `not-allowed`. Tooltip al clic: "Campo de solo lectura".
  2. **Cuerpo Editable** (centro):
     - `<textarea>` "Observaciones / Notas del Operario" (obligatorio, min 10, max 2000 chars). Mostrar contador de caracteres.
     - `<Dropzone>` "Adjuntos de Evidencia" (opcional, max 5 archivos, max 10MB/archivo, tipos: PDF, JPG, PNG, DOCX, XLSX). Implementar drag-and-drop con barra de progreso, botón cancelar y chip de archivo completado.
     - `<select>` "Resultado de la Gestión" (obligatorio). Opciones dinámicas desde `allowedResults` del BFF.
  3. **Botones de Pánico** (parte inferior): ver CA-8.
- **Validación Zod local**: esquema fijo con las reglas de los 3 campos. Bloquear botón [Enviar] si el schema no valida.
- **Restricción**: PROHIBIDO agregar campos adicionales en runtime. El set es fijo.

### CA-5 — Whitelist Regex por Proceso (Consumo Frontend)
**Responsabilidades Frontend:**
- Consumir `GET /api/v1/workbox/tasks/{id}/generic-form-context`.
- Renderizar dinámicamente los campos de `prefillData` como cuadrícula de solo lectura. Los campos ya vienen filtrados por el Backend.
- **No aplicar filtrado adicional** en el Frontend — el BFF ya garantiza la Whitelist.
- Si `prefillData` viene vacío, renderizar la cuadrícula con un mensaje: "No hay metadatos disponibles para esta tarea."

### CA-6 — Roles VIP (Contexto informativo Frontend)
**Responsabilidades Frontend:**
- El Frontend **NO implementa** la lógica de bloqueo VIP directamente. Esa validación ocurre en el Pre-Flight Analyzer del Backend (Pantalla 6).
- **Acción UI**: Si la Pantalla 14 (RBAC Manager) ya está implementada, agregar una columna visual `VIP` (ícono ⭐ o badge) en la tabla de roles que indique cuáles tienen `is_vip_restricted = true`. Permitir toggle al Super Admin.
- Consumir `GET /api/v1/admin/roles?vip_restricted=true` para obtener la lista.

### CA-7 — Persistencia y Auto-Guardado
**Responsabilidades Frontend:**
- Implementar auto-guardado con **debounce de 10 segundos** de inactividad de teclado:
  - `PUT /api/v1/drafts/{taskId}` (silencioso, HTTP 204 No Content).
  - Guardar también en `LocalStorage` atado al `taskId`.
- Al reabrir la tarea, verificar si existe borrador:
  - `GET /api/v1/drafts/{taskId}` — si existe, mostrar banner: "Se detectó un borrador no enviado. ¿Desea restaurarlo? [Restaurar] [Descartar]"
  - Si el servidor falla, intentar `LocalStorage` como fallback.
- Tras submit exitoso: purgar `LocalStorage` + `DELETE /api/v1/drafts/{taskId}`.
- **Indicador de sincronización** (esquina superior):
  - "☁️ Sincronizado" (verde)
  - "💾 Solo en este navegador" (amarillo)
  - "⟳ Sincronizando..." (animación)
  - "⚠️ Sin conexión al servidor" (rojo, tras 3 fallos consecutivos)

### CA-8 — Botones de Pánico (UI y Validación)
**Responsabilidades Frontend:**
- Renderizar 3 botones en la zona inferior del formulario:
  1. **[✅ Aprobado]** (color verde): Invoca submit con `panicAction: "APPROVED"`.
  2. **[🔄 Retorno al Generador]** (color amarillo/naranja): Invoca submit con `panicAction: "RETURNED"`.
  3. **[❌ Cancelar]** (color rojo): Invoca submit con `panicAction: "CANCELLED"`.
- **Precondición compartida**: Al clicar cualquier botón de pánico, abrir un **Modal de justificación obligatoria**:
  - `<textarea>` "Justifique su decisión" (min 20 caracteres).
  - Botón [Confirmar] deshabilitado hasta que se cumplan los 20 chars.
  - Botón [Volver] para cancelar la acción.
- Tras confirmar, enviar `POST /api/v1/workbox/tasks/{id}/generic-form-complete` con el body completo (observaciones + adjuntos + resultado + panicAction + panicJustification).
- Los botones de pánico **también requieren** que el campo "Observaciones" tenga al menos 10 chars (CA-4).

---

## Arquitectura de Componentes

```
GenericFormView.vue
├── MetadataGrid.vue (Solo lectura, cuadrícula de prefillData)
├── GenericFormBody.vue
│   ├── ObservationsField.vue (textarea con contador)
│   ├── EvidenceDropzone.vue (drag-and-drop con progreso)
│   └── ManagementResultSelect.vue (select dinámico)
├── PanicButtonBar.vue (3 botones)
├── PanicJustificationModal.vue (modal de justificación)
└── DraftSyncIndicator.vue (indicador de sincronización)
```

## Endpoint Backend a Consumir
| Método | Ruta | Propósito |
|--------|------|-----------|
| `GET`  | `/api/v1/workbox/tasks/{id}/generic-form-context` | BFF: schema + prefillData + allowedResults |
| `POST` | `/api/v1/workbox/tasks/{id}/generic-form-complete` | Submit del formulario + botón de pánico |
| `PUT`  | `/api/v1/drafts/{taskId}` | Auto-guardado silencioso |
| `GET`  | `/api/v1/drafts/{taskId}` | Recuperación de borrador |
| `DELETE`| `/api/v1/drafts/{taskId}` | Limpieza post-submit |
| `GET`  | `/api/v1/admin/roles?vip_restricted=true` | Lista de roles VIP (Pantalla 14) |

---

## Restricciones Arquitectónicas
1. **Vue 3 Composition API** + TypeScript estricto.
2. **Pinia** para estado del formulario.
3. `npm run build` obligatorio sin errores antes de push.
4. **No usar** `git stash`. Solo `git commit` + `git push`.
5. Seguir las 15 reglas de Layout (`v1_master_layout_policies.md`).

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en tu propia rama de sprint. Queda estrictamente prohibido usar git stash.
