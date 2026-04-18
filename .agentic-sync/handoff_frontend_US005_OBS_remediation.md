# Handoff Frontend — Remediación OBS-2 y Hardening (Auditoría I-73-DEV)
## US-005 | Rama: `sprint-3/us-005-bpmn-designer`

---

## Contexto (Orden del Arquitecto Líder)

La auditoría integral de la Iteración 73-DEV para US-005 (CA-63 a CA-70) fue **APROBADA CON OBSERVACIONES**. El Frontend superó la auditoría sin observaciones directas, pero se requieren **3 tareas de alineación y hardening** para mantener la consistencia con las remediaciones Backend (OBS-1 y OBS-2).

> **Referencia de Auditoría:** `auditoria_integral_us005_iteracion73DEV.md`
> **SSOT:** `docs/requirements/v1_user_stories.md` (CA-65, CA-68, CA-69)

---

## Tarea 1 — Alineación del Request `POST /deploy` con el nuevo contrato Backend (OBS-2)

**CA afectado:** CA-65
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
**Función:** `confirmDeploy()` (buscar en `<script setup>`)

**Problema:** El Backend ampliará `POST /deploy` para aceptar `deploy_comment` y `force_deploy` como `@RequestParam` (multipart). El Frontend actualmente envía solo `file`. El response body también cambiará.

**Cambio solicitado:**

```typescript
// BpmnDesigner.vue — EN la función confirmDeploy():

// ANTES (aproximación actual):
const formData = new FormData()
formData.append('file', bpmnBlob, `${processId.value}.bpmn`)
const { data } = await api.post('/design/processes/deploy', formData, {
  headers: { 'X-Mock-Role': mockRole.value }
})

// DESPUÉS (alineado con CA-65):
const formData = new FormData()
formData.append('file', bpmnBlob, `${processId.value}.bpmn`)
formData.append('deploy_comment', deployComment.value)  // Campo del textarea ya existente
formData.append('force_deploy', String(forceDeploy.value))  // Checkbox ya existente
const { data } = await api.post('/design/processes/deploy', formData, {
  headers: { 'X-Mock-Role': mockRole.value }
})

// Tras éxito, consumir el nuevo response body:
showToast(`✅ Desplegado: v${data.version} | ID: ${data.deployment_id}`, 'success')
```

**Validación Frontend del `deployComment`:** Agregar validación al botón "Confirmar Despliegue":

```typescript
// En confirmDeploy(), agregar al inicio:
if (!deployComment.value || deployComment.value.trim().length < 10) {
  showToast('❌ La justificación del despliegue debe tener al menos 10 caracteres.', 'error')
  return
}
```

---

## Tarea 2 — Alineación del componente DataMapper con nueva estructura Entity (OBS-1)

**CA afectado:** CA-68
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
**Función:** `saveConnectorMapping()` (buscar en `<script setup>`)

**Problema:** La entidad Backend `DataMappingEntity` cambiará su estructura de campos (OBS-1). El Frontend que consuma `POST /{key}/data-mappings` debe enviar el nuevo payload.

**Cambio solicitado:**

```typescript
// ANTES (el Frontend probablemente envía):
const payload = {
  variableName: schema.name,
  variableType: schema.type,
  isRequired: true
}

// DESPUÉS (alineado con nuevo Entity):
const payload = {
  taskId: selectedElement.value.id,           // ID del nodo ServiceTask seleccionado
  connectorId: selectedConnector.value,       // ID del conector API seleccionado
  mappingJson: JSON.stringify(connectorMappings.value)  // JSON serializado del mapeo visual
}
```

Igualmente, al leer los mappings en `fetchDataMappings()`:

```typescript
// ANTES:
const { data } = await api.get(`/design/processes/${key}/data-mappings`)
// data era [{variableName, variableType, isRequired}]

// DESPUÉS:
const { data } = await api.get(`/design/processes/${key}/data-mappings`)
// data ahora es [{id, processDefinitionKey, taskId, connectorId, mappingJson, lastValidatedAt}]
// Reconstruir el mappeo visual:
if (data.length > 0) {
  const mapping = data.find((m: any) => m.taskId === selectedElement.value.id)
  if (mapping && mapping.mappingJson) {
    connectorMappings.value = JSON.parse(mapping.mappingJson)
  }
}
```

---

## Tarea 3 — Hardening del comentario de rechazo en Deploy Requests (CA-69)

**CA afectado:** CA-69
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
**Función:** `handleDeployRequest()` (buscar en `<script setup>`)

**Problema:** La SSOT (CA-69 L2566) exige mínimo 20 caracteres en el comentario de rechazo. El Frontend no valida esto antes de enviar al Backend.

**Cambio solicitado:**

```typescript
// En handleDeployRequest(requestId, isApproval):

// Al rechazar, solicitar comentario con validación:
if (!isApproval) {
  const comment = window.prompt('Comentario de rechazo (mínimo 20 caracteres):')
  if (!comment || comment.trim().length < 20) {
    showToast('❌ El comentario de rechazo debe tener al menos 20 caracteres (CA-69).', 'error')
    return
  }
  await api.post(`/design/processes/deploy-requests/${requestId}/reject`, { comment })
} else {
  const comment = window.prompt('Comentario de aprobación (opcional):') || ''
  await api.post(`/design/processes/deploy-requests/${requestId}/approve`, { comment })
}
```

> **Nota:** Idealmente en iteraciones posteriores reemplazar `window.prompt` por un Modal Vue dedicado. Para V1.0, `prompt` es aceptable para no bloquear el merge.

---

## Verificación Obligatoria
1. `npm run build` sin errores de TypeScript.
2. Verificar visualmente que al desplegar, el toast muestre la versión y deployment_id del response.
3. Verificar que al intentar rechazar una solicitud con comentario < 20 chars, se muestre error.
4. Verificar que al guardar un data mapping, el payload enviado sea `{taskId, connectorId, mappingJson}`.

---

## Restricciones Arquitectónicas
1. **Vue 3 Composition API** + TypeScript estricto.
2. **No crear componentes nuevos** — estas son correcciones in-situ en `BpmnDesigner.vue`.
3. **No usar** `git stash`. Solo `git commit` + `git push`.
4. **Convención de commit:**
   - `fix(frontend): US-005 OBS-2 align deploy request with CA-65 contract`
   - `fix(frontend): US-005 OBS-1 align DataMapper payload with new entity structure`
   - `fix(frontend): US-005 CA-69 enforce min 20 chars on rejection comment`

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. **DEPENDENCIA:** Ejecuta después de que el Backend haya confirmado sus commits de OBS-1 y OBS-2. Haz `git pull` antes de comenzar.
> 2. Ejecuta los 3 cambios y crea commits atómicos separados.
> 3. Compila con `npm run build` después de CADA commit.
> 4. Al finalizar, graba tu confirmación de cierre en `.agentic-sync/approval_request_frontend.md` indicando: `OBS-1 + OBS-2 + CA-69 Alineación — commits: <hash1>, <hash2>, <hash3>`.
> 5. Dile al Humano: *"Humano, he alineado el Frontend con las remediaciones Backend. Entrégale este mensaje al Arquitecto Líder."*
