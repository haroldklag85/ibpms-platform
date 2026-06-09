# 🟣 Handoff Frontend — US-025 Fase 3B-UI: Impersonación Banner + i18n Transversal

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** Frontend  
> **Prioridad:** 🟡 P1 — **APROBADO POR PO PARA V1 (D1 + D2)**  
> **Rama Git:** sprint-6  
> **Pre-requisito:** Fase 3A completada + Backend Fase 3B completada (endpoints de impersonación disponibles)  
> **Gate de Salida:** `npm run test:unit` 100% pass + `npm run build` limpio

---

## 1. Contexto

El PO aprobó las siguientes decisiones para V1:

- **D1 — Impersonación (CA-9/CA-31):** Requiere un componente `ImpersonationBanner.vue` en el Header y la vista "Ver Sistema Como" para admins.
- **D2 — i18n (CA-24):** Requiere instalación de `vue-i18n`, creación de archivos de locale, y extracción de TODOS los strings hardcodeados a claves de traducción.

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-002 (Vue 3) | Composition API, plugins Vue (`vue-i18n`) |
| ADR-010 (Testing) | Tests obligatorios para todos los componentes nuevos |
| ADR-014 (Observability) | Los mensajes de toast también deben usar i18n |

---

## 3. Tareas — Bloque A: Impersonación Frontend (CA-9, CA-31)

### Tarea 3B-UI.1 — Crear `ImpersonationBanner.vue`

**Crear:** `frontend/src/components/admin/ImpersonationBanner.vue`

**Especificación:**
- Banner amarillo/ámbar pegado en la parte superior de la aplicación (z-index alto, sobre el header)
- Visible SOLO cuando `authStore.isImpersonating === true`
- Muestra: "🔄 Estás viendo el sistema como **{nombreUsuarioTarget}** (Rol: {rolTarget})"
- Botón: "Volver a mi sesión" → Invoca `POST /api/v1/admin/impersonate/exit`, restaura JWT original
- Auto-expire: Timer visual de 30 minutos con countdown

**Integrar en:** `App.vue` como componente global (encima de `MainLayout`)

```vue
<template>
  <div v-if="authStore.isImpersonating" class="impersonation-banner">
    <span class="banner-icon">🔄</span>
    <span>{{ t('impersonation.viewing_as', { name: targetUser, role: targetRole }) }}</span>
    <span class="countdown">{{ formattedTimeLeft }}</span>
    <button @click="exitImpersonation" class="exit-btn">
      {{ t('impersonation.exit') }}
    </button>
  </div>
</template>
```

**CSS:**
```css
.impersonation-banner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  z-index: 9999;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
```

---

### Tarea 3B-UI.2 — Extender `authStore.ts` con estado de impersonación

**Archivo:** `frontend/src/stores/authStore.ts`

Agregar:
```typescript
const isImpersonating = ref(false);
const impersonatedBy = ref<string | null>(null);
const originalToken = ref<string | null>(null);
const impersonationExpiresAt = ref<number | null>(null);

const startImpersonation = async (targetUserId: string) => {
  originalToken.value = token.value; // Guardar JWT original
  const { data } = await apiClient.post(`/admin/impersonate/${targetUserId}`);
  token.value = data.token;
  isImpersonating.value = true;
  impersonatedBy.value = user.value?.id || null;
  impersonationExpiresAt.value = Date.now() + (parseInt(data.expiresIn) * 1000);
  await hydrateAuth(); // Re-hidratar con el nuevo JWT
};

const exitImpersonation = async () => {
  await apiClient.post('/admin/impersonate/exit');
  token.value = originalToken.value; // Restaurar JWT original
  originalToken.value = null;
  isImpersonating.value = false;
  impersonatedBy.value = null;
  impersonationExpiresAt.value = null;
  await hydrateAuth(); // Re-hidratar con JWT original
};
```

---

### Tarea 3B-UI.3 — Crear vista "Ver Sistema Como" en Admin

**Crear:** `frontend/src/components/admin/ImpersonationSelector.vue`

**Especificación:**
- Visible SOLO para `ROLE_SUPER_ADMIN` en el panel de admin
- Lista todos los usuarios del sistema (paginada, server-side)
- Barra de búsqueda para filtrar por nombre/email
- Botón "Ver como" al lado de cada usuario
- NO muestra usuarios con `ROLE_SUPER_ADMIN` (no impersonables)

---

### Tarea 3B-UI.4 — Tests Impersonación Frontend

**Crear:** `frontend/src/tests/components/admin/ImpersonationBanner.spec.ts`

**Escenarios:**
1. **Hidden normalmente:** Sin impersonación, banner no se renderiza
2. **Visible durante impersonación:** Con `isImpersonating=true`, muestra banner amarillo
3. **Exit restaura sesión:** Clic en "Volver" invoca `exitImpersonation()`
4. **Countdown timer:** Timer muestra tiempo restante decrementando

---

## 4. Tareas — Bloque B: Internacionalización i18n (CA-24)

### Tarea 3B-UI.5 — Instalar y configurar `vue-i18n`

**Ejecutar:** `npm install vue-i18n@9`

**Crear:** `frontend/src/i18n/index.ts`
```typescript
import { createI18n } from 'vue-i18n';
import es from './locales/es.json';
import en from './locales/en.json';

export const i18n = createI18n({
  legacy: false, // Composition API mode
  locale: localStorage.getItem('ibpms_locale') || 'es',
  fallbackLocale: 'es',
  messages: { es, en }
});
```

**Registrar en:** `frontend/src/main.ts`
```typescript
import { i18n } from './i18n';
app.use(i18n);
```

---

### Tarea 3B-UI.6 — Crear archivos de locale

**Crear:** `frontend/src/i18n/locales/es.json`

Estructura mínima V1 (extraer strings de los componentes del App Shell):
```json
{
  "common": {
    "loading": "Cargando...",
    "error": "Error",
    "save": "Guardar",
    "cancel": "Cancelar",
    "delete": "Eliminar",
    "confirm": "Confirmar",
    "search": "Buscar...",
    "no_results": "Sin resultados",
    "logout": "Cerrar Sesión"
  },
  "layout": {
    "sidebar_collapse": "Colapsar menú",
    "sidebar_expand": "Expandir menú",
    "density_compact": "Compacto",
    "density_standard": "Estándar",
    "density_comfortable": "Cómodo"
  },
  "workdesk": {
    "title": "Mi Workdesk",
    "no_tasks": "No tienes tareas pendientes",
    "claim": "Reclamar",
    "unclaim": "Desasignar",
    "filter_by_status": "Filtrar por estado"
  },
  "auth": {
    "login_title": "Iniciar Sesión",
    "session_expired": "Tu sesión ha expirado",
    "unlock": "Desbloquear",
    "switch_role": "Cambiar perfil"
  },
  "impersonation": {
    "viewing_as": "Estás viendo el sistema como {name} (Rol: {role})",
    "exit": "Volver a mi sesión",
    "time_remaining": "Tiempo restante: {time}"
  },
  "errors": {
    "server_error": "Error interno del servidor (Trace: {traceId}). Contacte soporte.",
    "service_unavailable": "El servidor no está disponible ({code}). Verificando conexión...",
    "gateway_timeout": "Tiempo de espera agotado. Verifique que el servidor esté activo.",
    "network_error": "Sin conexión a internet. Verificando...",
    "session_lock": "Sesión bloqueada. Ingrese su contraseña para continuar."
  },
  "empty_states": {
    "no_tasks_title": "Todo al día",
    "no_tasks_description": "No tienes tareas pendientes. ¡Buen trabajo!",
    "no_access_title": "Sin acceso",
    "no_access_description": "No tienes permisos para ver este contenido.",
    "error_title": "Algo salió mal",
    "error_description": "No pudimos cargar la información. Intenta de nuevo."
  }
}
```

**Crear:** `frontend/src/i18n/locales/en.json` (traducción al inglés — misma estructura)

---

### Tarea 3B-UI.7 — Refactorizar componentes existentes del App Shell para usar i18n

**Archivos a modificar (alcance V1 — solo App Shell):**

| Componente | Strings a extraer | Ejemplo |
|------------|-------------------|---------|
| `MainLayout.vue` | Labels sidebar, breadcrumbs, botones | `"Cerrar Sesión"` → `{{ t('common.logout') }}` |
| `DynamicRoleCards.vue` | Empty states, loading | `"Cargando..."` → `{{ t('common.loading') }}` |
| `ConnectionToast.vue` | Mensajes de error | Hardcoded msg → `t('errors.network_error')` |
| `FatalToast.vue` | Mensaje fatal | Hardcoded → `t('errors.server_error', { traceId })` |
| `SessionLockModal.vue` | Labels de auth | Hardcoded → `t('auth.session_expired')` |
| `EmptyState.vue` | Títulos/descripciones | Hardcoded → `t('empty_states.no_tasks_title')` |
| `RoleSelectorDropdown.vue` | Labels | Hardcoded → `t('auth.switch_role')` |
| `ImpersonationBanner.vue` | Messages | Hardcoded → `t('impersonation.viewing_as', {...})` |
| `WorkdeskGrid.vue` | Headers, botones | `"Reclamar"` → `{{ t('workdesk.claim') }}` |

**Patrón de refactor:**
```vue
<script setup>
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
</script>

<template>
  <!-- Antes: -->
  <span>Cerrar Sesión</span>
  
  <!-- Después: -->
  <span>{{ t('common.logout') }}</span>
</template>
```

> [!IMPORTANT]
> **Alcance V1:** Solo los componentes del App Shell (listados arriba). NO refactorizar componentes de módulos específicos (BPMN Modeler, DMN, Form Designer) — esos se migrarán a i18n en V2 cuando se ejecuten sus respectivas US.

---

### Tarea 3B-UI.8 — Crear selector de idioma en Header

**Crear o agregar en:** `frontend/src/layouts/MainLayout.vue` (Header zone)

- Dropdown minimalista con banderas/códigos: 🇪🇸 ES | 🇺🇸 EN
- Persiste selección en `localStorage('ibpms_locale')`
- Al cambiar idioma, la app se actualiza reactivamente (sin recarga)

---

### Tarea 3B-UI.9 — Tests i18n

**Crear:** `frontend/src/tests/i18n/i18n-integration.spec.ts`

**Escenarios:**
1. **Default español:** Sin localStorage, la app renderiza en español
2. **Switch a inglés:** Cambiar locale → todos los labels cambian a inglés
3. **Persistencia:** Recargar app → mantiene el idioma seleccionado
4. **Fallback:** Clave inexistente → muestra clave en idioma fallback (español)

---

## 5. Criterios de Aceptación del Gate

### Impersonación:
- [ ] `ImpersonationBanner.vue` visible solo durante impersonación
- [ ] Countdown de 30 minutos funcional
- [ ] "Volver a mi sesión" restaura JWT original
- [ ] 4 tests de impersonación frontend pasando

### i18n:
- [ ] `vue-i18n` instalado y configurado con Composition API
- [ ] Locale `es.json` y `en.json` con mínimo 50 claves cada uno
- [ ] 9 componentes del App Shell migrados a `t()` 
- [ ] Selector de idioma en Header funcional
- [ ] 4 tests de i18n pasando
- [ ] `npm run test:unit` 100% verde
- [ ] `npm run build` sin errores

## 6. Exclusiones

- NO migrar a i18n componentes fuera del App Shell (BPMN Modeler, DMN, Form Designer) — V2
- NO implementar locale detection automática por navegador — V2
- NO crear más de 2 idiomas (ES + EN) — V2

## 7. Archivos Impactados

| Archivo | Acción | Bloque |
|---------|--------|:------:|
| `components/admin/ImpersonationBanner.vue` | Crear | A |
| `components/admin/ImpersonationSelector.vue` | Crear | A |
| `stores/authStore.ts` | Modificar (impersonation state) | A |
| `tests/components/admin/ImpersonationBanner.spec.ts` | Crear | A |
| `i18n/index.ts` | Crear | B |
| `i18n/locales/es.json` | Crear | B |
| `i18n/locales/en.json` | Crear | B |
| `main.ts` | Modificar (register i18n plugin) | B |
| `layouts/MainLayout.vue` | Modificar (i18n + locale selector) | B |
| `components/common/FatalToast.vue` | Modificar (i18n) | B |
| `components/common/SessionLockModal.vue` | Modificar (i18n) | B |
| `components/common/EmptyState.vue` | Modificar (i18n) | B |
| `components/common/RoleSelectorDropdown.vue` | Modificar (i18n) | B |
| `components/agile/DynamicRoleCards.vue` | Modificar (i18n) | B |
| `components/common/ConnectionToast.vue` | Modificar (i18n) | B |
| `tests/i18n/i18n-integration.spec.ts` | Crear | B |
