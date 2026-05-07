# 🏛️ HANDOFF DE REMEDIACIÓN — US-029: Ejecución y Envío de Formulario
# BLOQUE 2: Refinamiento UX + Seguridad Complementaria (10 GAPs)
**Fecha:** 2026-05-03 | **Iteración:** 5 | **Arquitecto Líder:** Antigravity

---

## 📊 Mapa de GAPs: Bloque 1 (Cerrado) vs Bloque 2 (Este Handoff) vs V2 (Diferido)

| GAP | CA | Descripción | Bloque |
|-----|-----|-------------|--------|
| ~~GAP-01~~ | CA-02 | ~~JSON Schema Backend~~ | ✅ B1 CERRADO |
| GAP-02 | CA-06 | Micro-Tokens JWT asíncronos | 🔵 V2 DIFERIDO |
| ~~GAP-03~~ | CA-08 | ~~Lazy Patching~~ | ✅ B1 CERRADO |
| ~~GAP-04~~ | CA-09 | ~~Upload-First~~ | ✅ B1 CERRADO |
| **GAP-05** | CA-11 | Cifrado AES PII LocalStorage | 🟠 **B2** |
| ~~GAP-06~~ | CA-13 | ~~Anti-IDOR~~ | ✅ B1 CERRADO |
| GAP-07 | CA-14 | Anti-Replay Redis | 🔵 V2 DIFERIDO |
| ~~GAP-08~~ | CA-15 | ~~Zod Isomórfico Backend~~ | ✅ B1 CERRADO |
| ~~GAP-09~~ | CA-16 | ~~Exclusión Topológica~~ | ✅ B1 CERRADO |
| ~~GAP-10~~ | CA-17 | ~~RYOW~~ | ✅ B1 CERRADO |
| GAP-11 | CA-19 | Reconciliación US-029/US-017 | 🔵 V2 DOC |
| ~~GAP-12~~ | CA-20 | ~~Overlay Submit~~ | ✅ B1 CERRADO |
| ~~GAP-13~~ | CA-21 | ~~Confirmación Post-Submit~~ | ✅ B1 CERRADO |
| **GAP-14** | CA-22 | Wizard multi-step | 🟠 **B2** |
| **GAP-15** | CA-25 | Scroll auto al primer error | 🟡 **B2** |
| **GAP-16** | CA-26 | Pre-aviso caducidad borrador | 🟡 **B2** |
| ~~GAP-17~~ | CA-27 | ~~Schema Version Conflict~~ | ✅ B1 CERRADO |
| ~~GAP-18~~ | CA-28 | ~~Aduana archivos MIME~~ | ✅ B1 CERRADO |
| ~~GAP-19~~ | CA-29 | ~~Feedback Upload barra~~ | ✅ B1 CERRADO |
| **GAP-20** | CA-30 | Sesión duplicada BroadcastChannel | 🟡 **B2** |
| **GAP-21** | CA-31 | Indicador sync mejorado | 🟡 **B2** |
| **GAP-22** | CA-32 | Anti-envío accidental | 🟡 **B2** |
| **GAP-23** | CA-33 | Read-only visual | 🟡 **B2** |
| ~~GAP-24~~ | CA-34 | ~~Campos condicionales~~ | ✅ B1 CERRADO |

**Resumen:** 14 cerrados (B1) + 10 este Bloque + 3 diferidos V2 = 27/27 cubiertos (excluyendo los 7 CAs ya con cobertura previa detectados en la auditoría).

---

## 🔵 AGENTE INFRA/DB

**Objetivo:** Soporte para TTL de borradores y limpieza programada.

### INFRA-029-05: Columna `draft_expires_at` en `ibpms_agile_tasks` (CA-26)
- Verificar si existe columna `draft_updated_at` en la tabla `ibpms_agile_tasks`.
- Si no existe, crear changeset Liquibase:
```sql
-- Changeset: XX-us029-draft-expiration.sql
ALTER TABLE ibpms_agile_tasks ADD COLUMN IF NOT EXISTS draft_expires_at TIMESTAMP;
COMMENT ON COLUMN ibpms_agile_tasks.draft_expires_at IS 'Fecha de expiración del borrador (72h desde última edición). Usado para warning a 48h.';

-- Índice para el cron de limpieza
CREATE INDEX IF NOT EXISTS idx_agile_tasks_draft_expires ON ibpms_agile_tasks(draft_expires_at) WHERE draft_expires_at IS NOT NULL;
```
- Registrar en `db.changelog-master.yaml`.

**Entregable:** `.agentic-sync/approval_request_infra_US029_b2.md`

---

## 🟢 AGENTE BACKEND

**Objetivo:** 2 tareas de soporte para TTL de borradores y cifrado.

### BACK-029-08: Draft TTL Auto-Calculation (GAP-16, CA-26)
- En `TaskDraftService.saveDraft()`, al guardar un borrador:
  - Calcular `draft_expires_at = ZonedDateTime.now().plusHours(72)`
  - Persistir en la columna `draft_expires_at` de la tarea
- En `WorkboxTaskController` o `FormBffController`, al devolver el contexto del formulario:
  - Incluir `draftExpiresAt` en la respuesta JSON para que el Frontend muestre el warning

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/TaskDraftService.java`

### BACK-029-09: Endpoint de Verificación de Sesión Activa (GAP-20, CA-30)
- Crear un endpoint ligero (opcional, puede ser solo frontend con BroadcastChannel):
```java
// En WorkboxTaskController o FormBffController
@GetMapping("/tasks/{taskId}/active-session")
public ResponseEntity<Map<String, Object>> checkActiveSession(@PathVariable String taskId) {
    // Retorna si hay un draft activo reciente (< 30s) de otro usuario
    return ResponseEntity.ok(Map.of(
        "hasActiveSession", taskDraftService.hasRecentDraft(taskId, 30),
        "lastEditBy", taskDraftService.getLastEditor(taskId)
    ));
}
```
- **NOTA:** Este endpoint es OPCIONAL. El Frontend puede resolver la detección de duplicados puramente con `BroadcastChannel` del lado del cliente (mismo usuario, pestañas distintas). Solo es necesario si se quiere detectar colisiones entre usuarios diferentes.

**Gate de Validación:** `mvn compile` → exit code 0.
**Entregable:** `.agentic-sync/approval_request_backend_US029_b2.md`

---

## 🟡 AGENTE FRONTEND

**Objetivo:** 7 tareas de refinamiento UX para completar la experiencia de ejecución de formulario.

### Contexto:
- `genericFormStore.ts` ya tiene: autoSave con debounce 10s, syncState reactivo, draft recovery banner
- `GenericFormBody.vue` ya tiene: overlay submit, confirmación post-submit, RYOW, lazy patching, modal 409
- `EvidenceDropzone.vue` ya tiene: Upload-First con barra progreso, AbortController, retry

### FRONT-029-08: Cifrado AES PII en LocalStorage (GAP-05, CA-11)
**Archivo:** `frontend/src/stores/genericFormStore.ts`

- Crear un módulo `frontend/src/utils/draftCrypto.ts`:
```typescript
// Usa Web Crypto API (nativo del navegador, sin dependencias)
const ALGO = 'AES-GCM'

export async function encryptDraft(data: string, sessionKey: string): Promise<string> {
  const key = await deriveKey(sessionKey)
  const iv = crypto.getRandomValues(new Uint8Array(12))
  const encoded = new TextEncoder().encode(data)
  const encrypted = await crypto.subtle.encrypt({ name: ALGO, iv }, key, encoded)
  // Concatenar IV + ciphertext y convertir a base64
  const combined = new Uint8Array(iv.length + new Uint8Array(encrypted).length)
  combined.set(iv)
  combined.set(new Uint8Array(encrypted), iv.length)
  return btoa(String.fromCharCode(...combined))
}

export async function decryptDraft(base64: string, sessionKey: string): Promise<string> {
  const key = await deriveKey(sessionKey)
  const combined = Uint8Array.from(atob(base64), c => c.charCodeAt(0))
  const iv = combined.slice(0, 12)
  const data = combined.slice(12)
  const decrypted = await crypto.subtle.decrypt({ name: ALGO, iv }, key, data)
  return new TextDecoder().decode(decrypted)
}

async function deriveKey(password: string): Promise<CryptoKey> {
  const keyMaterial = await crypto.subtle.importKey(
    'raw', new TextEncoder().encode(password), 'PBKDF2', false, ['deriveKey']
  )
  return crypto.subtle.deriveKey(
    { name: 'PBKDF2', salt: new TextEncoder().encode('ibpms-draft-salt'), iterations: 100000, hash: 'SHA-256' },
    keyMaterial, { name: ALGO, length: 256 }, false, ['encrypt', 'decrypt']
  )
}
```
- En `genericFormStore.ts`, modificar `autoSaveDraft()`:
  - Antes de `localStorage.setItem()`: cifrar con `encryptDraft(json, sessionId)`
  - En `checkForDraft()`: descifrar con `decryptDraft(stored, sessionId)`
  - La `sessionKey` se obtiene del JWT `sub` claim (ya disponible en `useAuthStore().userId`)

### FRONT-029-09: Scroll Automático al Primer Error (GAP-15, CA-25)
**Archivo:** `frontend/src/components/forms/generic/GenericFormBody.vue`

- En `onConfirmClick()`, si la validación falla (`!isValid`):
```typescript
const onConfirmClick = async () => {
  if (!isValid.value || missingRequiredFields.value.length > 0) {
    showInlineError.value = true
    // Scroll al primer campo con error
    nextTick(() => {
      const firstError = document.querySelector('.border-red-500, .text-red-600, [data-error="true"]')
      if (firstError) {
        firstError.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    })
    return
  }
  // ... rest of submit logic
}
```

### FRONT-029-10: Pre-Aviso Caducidad Borrador 48h (GAP-16, CA-26)
**Archivo:** `frontend/src/components/forms/generic/GenericFormBody.vue`

- Leer `draftExpiresAt` del contexto BFF (`store.prefillData.draftExpiresAt`)
- Calcular horas restantes. Si < 24h, mostrar banner amarillo:
```html
<div v-if="draftHoursRemaining !== null && draftHoursRemaining < 24"
     class="mb-4 p-3 bg-yellow-50 border border-yellow-300 rounded text-yellow-800 text-sm flex items-center gap-2">
  <span class="text-xl">⏰</span>
  <div>
    <strong>Borrador próximo a expirar.</strong>
    <p>Quedan {{ Math.ceil(draftHoursRemaining) }} horas antes de que el borrador se elimine automáticamente.</p>
  </div>
</div>
```

### FRONT-029-11: Detección Sesión Duplicada — BroadcastChannel (GAP-20, CA-30)
**Archivo:** Crear `frontend/src/composables/workdesk/useSessionLock.ts`

```typescript
import { ref, onMounted, onUnmounted } from 'vue'

export function useSessionLock(taskId: string) {
  const isLocked = ref(false)
  const lockMessage = ref('')
  let channel: BroadcastChannel | null = null

  onMounted(() => {
    channel = new BroadcastChannel(`ibpms-form-${taskId}`)

    // Announce this tab
    channel.postMessage({ type: 'CLAIM', tabId: crypto.randomUUID(), timestamp: Date.now() })

    channel.onmessage = (event) => {
      if (event.data.type === 'CLAIM') {
        // Another tab opened the same form
        isLocked.value = true
        lockMessage.value = 'Este formulario está siendo editado en otra pestaña. Modo solo lectura activado.'
      }
    }
  })

  onUnmounted(() => {
    channel?.postMessage({ type: 'RELEASE' })
    channel?.close()
  })

  return { isLocked, lockMessage }
}
```
- En `GenericFormBody.vue`, importar e integrar:
  - Si `isLocked === true`: deshabilitar TODOS los inputs, botones, y dropzone. Mostrar banner azul informativo.

### FRONT-029-12: Anti-Envío Accidental (GAP-22, CA-32)
**Archivo:** `frontend/src/components/forms/generic/GenericFormBody.vue`

- ANTES de ejecutar `onConfirmClick()`, si hay campos obligatorios sin llenar (`!isValid`):
  - Mostrar un `window.confirm()` nativo o un modal: "¿Está seguro que desea enviar? Hay campos sin completar."
  - Solo para el caso donde se intente enviar ANTES de completar todo
- NOTA: El botón ya se deshabilita con `:disabled="!isValid"`, así que este GAP aplica solo si se REMUEVE la validación de disabled para formularios parciales. Implementar como diálogo `beforeunload`:
```typescript
// Proteger contra cierre accidental de pestaña
onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
})
onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (store.syncState !== 'SYNCED' && store.observations.length > 0) {
    e.preventDefault()
    e.returnValue = 'Tiene cambios sin guardar. ¿Desea salir?'
  }
}
```

### FRONT-029-13: Distinción Visual Read-Only (GAP-23, CA-33)
**Archivo:** `frontend/src/components/forms/generic/MetadataGrid.vue`

- Los campos de `prefillData` que vienen del contexto BFF (no editables por el usuario) deben:
  - Background `bg-gray-100` (gris sutil)
  - Ícono 🔒 al lado del label
  - `cursor: not-allowed` y `pointer-events: none` en el input
  - Atributo `readonly` o `disabled` en el HTML
```html
<input
  :value="value"
  readonly
  class="bg-gray-100 text-gray-600 cursor-not-allowed border-gray-200"
/>
<span class="text-gray-400 text-xs ml-1">🔒</span>
```

### FRONT-029-14: Indicador Sync Mejorado (GAP-21, CA-31)
**Archivo:** `frontend/src/components/forms/generic/DraftSyncIndicator.vue`

- Verificar que el componente actual refleje los 4 estados del `syncState`:
  - `SYNCED` → ☁️ "Guardado" (verde)
  - `SAVING` → ⟳ "Guardando..." (azul, con animación spin)
  - `LOCAL_ONLY` → 💾 "Solo local" (amarillo)
  - `ERROR` → ⚠️ "Error de sincronización" (rojo)
- Si el componente ya existe y cubre estos estados, marcar como PASS sin cambios.

**Gate de Validación:** `npm run build` → exit code 0.
**Entregable:** `.agentic-sync/approval_request_frontend_US029_b2.md`

---

## 🔴 AGENTE QA

**Objetivo:** Certificar los 10 GAPs del Bloque 2 mediante revisión de código y trazabilidad arquitectónica.

### Matriz de Escenarios a Validar:

| ID | Escenario | CA/GAP | Validación |
|----|-----------|--------|------------|
| QA-029-15 | `draftCrypto.ts` existe con funciones `encryptDraft`/`decryptDraft` usando Web Crypto API | CA-11/GAP-05 | Verificar que `localStorage.setItem` en `autoSaveDraft()` usa `encryptDraft()` y NO guarda JSON plano |
| QA-029-16 | `onConfirmClick()` hace `scrollIntoView()` al primer campo con error | CA-25/GAP-15 | Verificar `nextTick` + `querySelector('.border-red-500')` + `scrollIntoView({behavior: 'smooth'})` |
| QA-029-17 | Banner amarillo de caducidad visible cuando borrador < 24h de expirar | CA-26/GAP-16 | Verificar cálculo `draftHoursRemaining` y condicional `v-if` |
| QA-029-18 | `useSessionLock.ts` usa `BroadcastChannel` para detectar pestaña duplicada | CA-30/GAP-20 | Verificar que `isLocked === true` deshabilita inputs/botones en `GenericFormBody.vue` |
| QA-029-19 | `DraftSyncIndicator.vue` muestra 4 estados: ☁️ SYNCED, ⟳ SAVING, 💾 LOCAL_ONLY, ⚠️ ERROR | CA-31/GAP-21 | Verificar cobertura de los 4 iconos/colores |
| QA-029-20 | `beforeunload` previene cierre accidental si hay cambios sin guardar | CA-32/GAP-22 | Verificar `window.addEventListener('beforeunload')` con `e.preventDefault()` |
| QA-029-21 | Campos read-only tienen `bg-gray-100`, `cursor-not-allowed`, y 🔒 | CA-33/GAP-23 | Verificar `MetadataGrid.vue` con atributo `readonly` y clases CSS |
| QA-029-22 | `TaskDraftService.saveDraft()` calcula y persiste `draft_expires_at` | CA-26/GAP-16 | Backend: verificar que `task.setDraftExpiresAt(now + 72h)` existe |
| QA-029-23 | Backend `/active-session` endpoint retorna `hasActiveSession` (OPCIONAL) | CA-30/GAP-20 | Si existe: verificar respuesta. Si no existe: PASS (frontend resuelve con BroadcastChannel) |
| QA-029-24 | Wizard multi-step en formulario genérico (GAP-14, CA-22) | CA-22/GAP-14 | Verificar si existe componente stepper/wizard. Si no: registrar como deuda V2 diferida (formulario genérico no requiere wizard) |

### Criterio de aceptación QA:
- **PASS mínimo:** 8/10 (QA-029-23 y QA-029-24 son opcionales/diferibles)
- **Compilación:** Frontend `npm run build` exit 0 + Backend `mvn compile` exit 0

**Entregable:** `.agentic-sync/approval_request_qa_US029_b2.md`

---

## 📌 GAPs Diferidos a V2 (NO incluir en este Bloque)

| GAP | CA | Razón de Diferimiento |
|-----|-----|----------------------|
| GAP-02 | CA-06 | Micro-Tokens JWT requieren infraestructura Redis no disponible en V1 |
| GAP-07 | CA-14 | Anti-Replay con jti + Redis — misma dependencia de infraestructura |
| GAP-11 | CA-19 | Reconciliación formal US-029/US-017 — solo documentación, sin impacto código |
| GAP-14 | CA-22 | Wizard multi-step — el formulario genérico NO requiere wizard (es single-step). Aplica solo para iForms Maestro en V2 |
