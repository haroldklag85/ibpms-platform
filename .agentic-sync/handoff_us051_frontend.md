# Handoff US-051: Agente Frontend (Sprint 6 — Cierre de Gaps)

**Emitido por:** Arquitecto Líder
**Fecha:** 2026-05-01
**Prioridad:** MUST (cierre obligatorio Sprint 6)
**Fuente SSOT:** `docs/requirements/epics/epic_E_seguridad_identidad_config.md` (líneas 562-695)

---

## Tareas Ordenadas por Prioridad:

### 1. CA-33 — Reconexión Agresiva SSE
**Archivo:** `stores/authStore.ts`, función `initSecurityListener()` (líneas 45-48)
**Acción:** Reemplazar el `onerror` silencioso por un retry con backoff exponencial (1s, 2s, 4s). Tras 3 fallos consecutivos, activar un `ref<boolean>` (`isSSEDisconnected`) que monte un overlay opaco en `App.vue`: *"Reconectando con el servidor de seguridad..."*

### 2. CA-34 — Soft-Refresh Quirúrgico
**Archivo:** `stores/authStore.ts`, función `initSecurityListener()` (línea 39)
**Acción:** Agregar un segundo branch en el `onmessage`:
```ts
if (event.data === '[ROLES_UPDATED]') {
    await hydrateAuth();
    const menuStore = useMenuStore();
    menuStore.purgeTopology();
    await menuStore.fetchMenuLayout();
}
```
**Nota:** Mantener el handler de `[ROLE_REVOKED]` → `logout()` como kill-switch de emergencia.

### 3. CA-35 — Persistencia Multi-Pestaña
**Archivo:** `App.vue` o `stores/authStore.ts`
**Acción:** Agregar en el `onMounted` de `App.vue`:
```ts
window.addEventListener('storage', (e) => {
    if (e.key === 'ibpms_token' && !e.newValue) {
        authStore.logout();
        router.push('/login');
    }
});
```

### 4. Ojo de Sauron — Telemetría Fail-Closed (CA-39 parcial)
**Archivo:** Crear `composables/useAuditReveal.ts`
**Acción:** Crear un composable que al hacer clic en "Mostrar 👁️" ejecute:
```ts
try {
    await apiClient.post('/api/v1/audit/events', {
        eventType: 'SECRETS_VIEWED',
        resourceId: fieldId,
        reason: 'user_requested'
    });
    // Si exitoso, revelar el dato
    isRevealed.value = true;
} catch (e) {
    // Fail-Closed: dato permanece oculto
    toast.error('No se pudo registrar la visualización. Dato protegido.');
}
```
**Nota:** El endpoint Backend `POST /api/v1/audit/events` **ya existe** en `AuditController.java`.

### 5. Portal.vue — Dashboard Bifurcado (CA-37)
**Archivo:** `views/Portal.vue`
**Acción:** Refactorizar. Actualmente es estático (tarjetas hardcodeadas). Debe:
- Consumir datos desde el backend (ProcessDefinitions) en lugar de tarjetas estáticas.
- Usar `v-if="authStore.hasAnyRole(['ROLE_SUPER_ADMIN'])"` para widgets administrativos.

### 6. Privilegios Solo Lectura (Granularidad CRUD)
**Archivos:** Vistas compartidas (Ej: `DmnIntelligence.vue`, `ConnectorBuilder.vue`)
**Acción:** Agregar `v-if="authStore.hasAnyRole(['ROLE_AI_ADMIN'])"` en los botones de acción destructiva (`[+ Nueva Regla]`, `[Eliminar]`) para ocultarlos a usuarios con acceso Read-Only.

### 7. Limpieza Técnica (Deuda UAT)
- **`authStore.ts` línea 125:** Eliminar `await new Promise(resolve => setTimeout(resolve, 800))` — es latencia artificial de demo.
- **`useSudo.ts` líneas 27-28:** Reemplazar `password.length >= 3` por validación real del response del Backend. El `.catch(() => true)` que traga errores debe eliminarse.
