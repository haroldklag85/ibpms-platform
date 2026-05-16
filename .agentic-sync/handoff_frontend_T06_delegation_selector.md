# 🧠→🎨 Handoff: Arquitecto Líder → Frontend
# T-06: Implementar Selector Múltiple de Delegantes — Eliminar Hardcode (CA-04 / US-001)

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [🎨 FRONTEND - VUE]
**Fecha:** 2026-05-11T22:07:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🟡 MEDIA
**Dependencia:** T-04/T-05 Backend (debe existir endpoint `/api/v1/admin/users/{id}/delegate` operativo)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Tu skill de build audit
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Estándares de código limpio
cat .agents/skills/clean_code_standards/SKILL.md

# 4. Zero-Mock enforcement
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 5. ADR-006 (Vue3 Low-Code Engine / Dumb Components)
cat docs/architecture/adr_006_vue3_lowcode_engine.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE incluir comentarios `// @Traceability: US-001, CA-04` en cada función y componente. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

### Hallazgo Crítico: Campo Fantasma `delegatedAssistants`

En `Workdesk.vue`, línea 54, se consume un campo que **NO EXISTE** en ningún store:

```vue
<option v-for="asst in (authStore as any).delegatedAssistants || []" 
        :key="asst.id" :value="asst.id">
```

**Evidencia forense:**
- `grep "delegatedAssistants" frontend/src/stores/` → **0 resultados**
- El cast `(authStore as any)` confirma que es un bypass de TypeScript — **violación de Zero-Trust UI**.
- El dropdown de delegantes siempre renderiza vacío (`[]` fallback), dejando la funcionalidad de delegación completamente inoperativa.

### Componentes ya existentes (reutilizar)

| Componente | Ruta | Estado |
|------------|------|:------:|
| `RbacDelegationLog.vue` | `views/admin/RbacManager/RbacDelegationLog.vue` | ✅ Tiene lógica de POST `/delegate` |
| `IdentityGovernance.vue` | `views/admin/Security/IdentityGovernance.vue` | ✅ Tiene tab "Delegaciones" (línea 642) |
| `Workdesk.vue` | `views/Workdesk.vue` | ⚠️ Campo fantasma — DEBE corregirse |

### API Backend disponible

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/admin/users/{id}/delegations` | Lista de delegaciones activas del usuario |
| POST | `/api/v1/admin/users/{id}/delegate` | Crear nueva delegación |
| GET | `/api/v1/workdesk/delegation-context` | Obtener contexto de delegación activa (ya usado en Workdesk) |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Crear o extender el store con `delegatedAssistants`

En el store de autenticación (`authStore` o crear `delegationStore.ts` si aplica patrón Dumb Component ADR-006):

```typescript
// @Traceability: US-001, CA-04 — Selector múltiple de delegantes
// Reemplaza el campo fantasma que usaba (authStore as any).delegatedAssistants

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '@/services/apiClient'

// Definir la interfaz para los delegantes
interface DelegateAssistant {
  id: string
  displayName: string
  email?: string
}

// Acción para cargar delegantes desde el backend real
async function fetchDelegatedAssistants(userId: string): Promise<DelegateAssistant[]> {
  const response = await apiClient.get(`/admin/users/${userId}/delegations`)
  return response.data
}
```

### Paso 2: Reemplazar el campo fantasma en `Workdesk.vue`

**Archivo:** `frontend/src/views/Workdesk.vue`

1. **Eliminar** el cast `(authStore as any).delegatedAssistants || []` (línea 54).
2. **Reemplazar** con una referencia reactiva alimentada por el store/composable real.
3. **Agregar** un `onMounted()` que invoque `fetchDelegatedAssistants()` al cargar la vista.
4. **Implementar** manejo de error Fail-Fast: si el endpoint falla, mostrar Toast de error (NO `alert()`).

### Paso 3: Implementar selector múltiple (si CA-04 lo exige)

- El `<select>` actual (línea 51-56) es un dropdown simple single-select.
- Si CA-04 requiere selección múltiple de delegantes, migrar a un componente `<MultiSelect>` de PrimeVue o construir uno nativo con checkboxes.
- Cada selección debe invocar el endpoint POST `/delegate` para activar la delegación.

### Paso 4: Validar con build

```bash
cd frontend
npm run build
```

Esperar: `Build successful` sin warnings de TypeScript sobre `delegatedAssistants`.

### Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Cero casts `as any` relacionados con delegación | `grep "(authStore as any)" Workdesk.vue` → 0 resultados |
| 2 | Dropdown alimentado por endpoint real | Network tab: GET `/delegations` con respuesta real |
| 3 | Error manejado con Toast/Modal (no `alert()`) | Inspección visual |
| 4 | `// @Traceability: US-001, CA-04` en cada función | Inspección de código |
| 5 | `npm run build` → Build successful | Log de terminal |
| 6 | Commit en rama de sprint | `git log -1` con mensaje descriptivo |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Leer los 5 archivos de governance listados arriba.
2. Verificar que el backend tiene el endpoint `/delegations` disponible (si no, reportar bloqueante).
3. Crear/extender el store con la propiedad real.
4. Refactorizar `Workdesk.vue` para consumir el store.
5. Build: `npm run build`.
6. Commit: `git add . && git commit -m "feat(US-001/CA-04): real delegation selector — purge phantom field" && git push`

---

**RECUERDA:** Si el endpoint backend NO está disponible, DETENTE y reporta al Arquitecto Líder. NO simules la respuesta con mocks (LEY GLOBAL 2, §7 Zero-Mock).
