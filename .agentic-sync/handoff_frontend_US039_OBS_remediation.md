# Handoff Frontend — Verificación de Cierre OBS (Auditoría I-72-DEV)
## US-039 | Rama: `sprint-3/informe_auditoriaSprint1y2`

---

## Contexto (Orden del Arquitecto Líder)

La auditoría integral de la Iteración 72-DEV para US-039 (CA-4 al CA-8) fue **APROBADA CON OBSERVACIONES**. De los 3 hallazgos (OBS-1, OBS-2, OBS-3), **ninguno impacta directamente al Frontend**. Tu capa superó la auditoría sin observaciones.

Sin embargo, el Arquitecto Líder solicita una tarea de **hardening preventivo** para fortalecer la robustez del componente GenericForm.

---

## Tareas de Hardening Asignadas

### Tarea 1 — Validación Defensiva de `panicJustification` en el Store (Relación con OBS-1)

**Contexto:** OBS-1 reveló que el Backend eliminará el `@Size(min=20)` del DTO para `panicJustification`, delegando la validación condicional al Service layer. Tu Store Pinia en `genericFormStore.ts` ya envía el campo solo cuando `panicAction != null`, pero **no verifica explícitamente la longitud** antes de enviar.

**Archivo:** `frontend/src/stores/genericFormStore.ts`
**Función:** `submitForm()` (L138-167)

**Cambio solicitado:**
Agregar una guarda defensiva antes del `apiClient.post()`:

```typescript
// En submitForm(), ANTES de apiClient.post():
if (panicAction.value) {
  if (!panicJustification.value || panicJustification.value.length < 20) {
    console.error('Panic justification must be >= 20 characters')
    return false
  }
  formData.append('panicAction', panicAction.value)
  formData.append('panicJustification', panicJustification.value)
}
```

**Razonamiento:** Aunque el Modal `PanicJustificationModal.vue` ya bloquea el botón [Confirmar] con Zod, esta guarda protege contra invocaciones programáticas del store sin pasar por la UI (ej: desde tests o flujos de hot-reload mal sincronizados).

---

### Tarea 2 — Límite de 5 Archivos en `EvidenceDropzone.vue` (Alineación con CA-4 Schema)

**Contexto:** El JSON Schema del Backend define `maxItems: 5` para attachments, pero el componente `EvidenceDropzone.vue` no valida el límite de 5 archivos en el cliente. Un usuario podría arrastrar 20 archivos y el rechazo ocurriría solo cuando el Backend responda 400.

**Archivo:** `frontend/src/components/forms/generic/EvidenceDropzone.vue`
**Función:** `addFiles()` (L69-71)

**Cambio solicitado:**
```typescript
// EvidenceDropzone.vue — ANTES:
const addFiles = (newFiles: File[]) => {
  store.files = [...store.files, ...newFiles]
}

// EvidenceDropzone.vue — DESPUÉS:
const MAX_FILES = 5

const addFiles = (newFiles: File[]) => {
  const remaining = MAX_FILES - store.files.length
  if (remaining <= 0) {
    // Opcionalmente mostrar toast: "Máximo 5 archivos permitidos"
    return
  }
  const allowed = newFiles.slice(0, remaining)
  store.files = [...store.files, ...allowed]
}
```

---

## Verificación Obligatoria
1. `npm run build` sin errores.
2. Verificar que el test `GenericFormView.spec.ts` pasa sin regresión.
3. Verificar visualmente que al arrastrar 6 archivos, solo se aceptan 5.

---

## Restricciones Arquitectónicas
1. **Vue 3 Composition API** + TypeScript estricto.
2. **Pinia** para estado del formulario (no inyectar estado local en el componente).
3. **No usar** `git stash`. Solo `git commit` + `git push`.
4. **Convención de commit:** `fix(frontend): US-039 hardening panic justification guard and file limit enforcement`

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Este cambio es de **ejecución directa** — NO requiere fase PLANNING dado que son correcciones quirúrgicas con instrucciones exactas del Arquitecto.
> 2. Ejecuta los cambios, compila con `npm run build`, y haz `git commit` + `git push`.
> 3. Al finalizar, graba tu confirmación de cierre en `.agentic-sync/approval_request_frontend.md` indicando: `Hardening OBS-1 + Max Files Guard — commit: <hash>`.
> 4. Dile al Humano: *"Humano, he implementado el hardening y registrado el cierre en `.agentic-sync/approval_request_frontend.md`. Entrégale este mensaje al Arquitecto Líder."*
