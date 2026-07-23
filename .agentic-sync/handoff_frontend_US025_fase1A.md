# 🟡 Handoff Frontend — US-025 Fase 1A: Fundaciones (Tests + ADR-014)

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** Frontend  
> **Prioridad:** 🟡 P1  
> **Rama Git:** sprint-6  
> **Pre-requisito:** Fase 0 completada  
> **Gate de Salida:** `npm run test:unit` 100% pass + `npm run build` limpio

---

## 1. Contexto

La auditoría US-025 reveló que los componentes más críticos del App Shell (MainLayout.vue 380 líneas, useMenuStore.ts, usePreferencesStore.ts) tienen **CERO tests unitarios**, violando ADR-010 (Testing Pyramid). Además, el interceptor 5xx de `apiClient.ts` agrupa todos los errores bajo un solo mensaje, violando ADR-014.

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-010 (Testing Pyramid) | Cobertura obligatoria para componentes visuales complejos |
| ADR-014 (Error Observability) | Diferenciación semántica de errores 5xx con traceId |

---

## 3. Tareas

### Tarea 1.1 — Tests unitarios MainLayout.vue

**Crear:** `frontend/src/tests/layouts/MainLayout.spec.ts`

**Escenarios mínimos obligatorios:**
1. **Sidebar RBAC:** Con roles `['ROLE_SUPER_ADMIN']` el sidebar muestra todos los grupos. Con roles `['ROLE_OPERADOR']` muestra solo Workdesk
2. **Colapso/Expansión:** Al invocar `toggleSidebar()`, la clase CSS muta de `w-64` a `w-16`
3. **Breadcrumbs:** La ruta `/admin/modeler/bpmn` genera 3 breadcrumbs con labels legibles del `routeNameMap`
4. **Density Toggle:** Al hacer clic en botón COMPACT, `preferencesStore.uiDensity` muta a `'COMPACT'`

**Mocks requeridos:**
- `useMenuStore` — mockear `fetchMenuLayout()` y `layout` con data de prueba
- `useAuthStore` — mockear `roles`, `user`, `hasAnyRole()`
- `usePreferencesStore` — mockear `uiDensity`
- `vue-router` — mockear `useRoute()` y `useRouter()`

---

### Tarea 1.2 — Tests unitarios useMenuStore.ts

**Crear:** `frontend/src/tests/stores/useMenuStore.spec.ts`

**Escenarios mínimos:**
1. **fetch exitoso:** `fetchMenuLayout()` llama `GET /users/me/menu-layout` y popula `layout`
2. **fetch fallido:** Si el endpoint falla, `layout` queda como `[]` (Zero-Trust)
3. **cache hit:** Si `layout.length > 0`, no hace segunda llamada HTTP
4. **purgeTopology:** Resetea `layout` a `[]`

**Mocks:** `apiClient.get` vía `vi.mock('@/services/apiClient')`

---

### Tarea 1.3 — Tests unitarios usePreferencesStore.ts

**Crear:** `frontend/src/tests/stores/usePreferencesStore.spec.ts`

**Escenarios mínimos:**
1. **Default:** Sin localStorage, `uiDensity` = `'STANDARD'`
2. **Mutación persiste:** Cambiar `uiDensity` a `'COMPACT'` → `localStorage.getItem('ibpms_density')` === `'COMPACT'`
3. **Body attribute:** Cambiar densidad → `document.body.getAttribute('data-density')` === nuevo valor

---

### Tarea 1.4 — Refactorizar interceptor 5xx (ADR-014)

**Archivo:** `frontend/src/services/apiClient.ts` — Líneas 50-74

**Código actual (DEFECTUOSO):**
```typescript
if (error.response && [500, 502, 503, 504].includes(error.response.status)) {
    const event = new CustomEvent('global-error-dispatch', { detail: { 
        code: error.response.status,
        message: `Colapso del Servidor / Integración Cíclica`
    }});
    window.dispatchEvent(event);
}
```

**Código objetivo (ADR-014 compliant):**
```typescript
if (error.response) {
    const status = error.response.status;
    const traceId = error.response.headers?.['x-correlation-id'] || 'N/A';

    if (status === 500) {
        window.dispatchEvent(new CustomEvent('global-error-dispatch', { detail: {
            code: 500,
            type: 'SERVER_ERROR',
            message: `Error interno del servidor (Trace: ${traceId}). Contacte soporte.`,
            dismissible: false
        }}));
    } else if (status === 502 || status === 503) {
        window.dispatchEvent(new CustomEvent('global-error-dispatch', { detail: {
            code: status,
            type: 'SERVICE_UNAVAILABLE',
            message: `El servidor no está disponible (${status}). Verificando conexión...`,
            dismissible: true,
            autoRetry: true
        }}));
    } else if (status === 504) {
        window.dispatchEvent(new CustomEvent('global-error-dispatch', { detail: {
            code: 504,
            type: 'GATEWAY_TIMEOUT',
            message: 'Tiempo de espera agotado. Verifique que el servidor esté activo.',
            dismissible: true
        }}));
    }
}
```

**Reglas:**
- Error 500 → `dismissible: false` (el usuario DEBE ver el traceId)
- Error 502/503 → `autoRetry: true` con backoff exponencial (3 intentos máximo)
- Error 504 → `dismissible: true` sin retry
- Nunca agrupar códigos con semántica distinta bajo un mismo mensaje

---

### Tarea 1.5 — Tests del interceptor refactorizado

**Crear o ampliar:** `frontend/src/tests/services/apiClient.spec.ts`

**Escenarios mínimos:**
1. **Error 500:** Dispatcha evento con `type: 'SERVER_ERROR'` y `dismissible: false`, incluye traceId
2. **Error 502:** Dispatcha evento con `type: 'SERVICE_UNAVAILABLE'` y `autoRetry: true`
3. **Error 504:** Dispatcha evento con `type: 'GATEWAY_TIMEOUT'` y `dismissible: true`, sin `autoRetry`
4. **Error sin response:** No dispara ningún evento global-error-dispatch

---

## 4. Criterios de Aceptación del Gate

- [ ] `MainLayout.spec.ts` — 4 escenarios pasando
- [ ] `useMenuStore.spec.ts` — 4 escenarios pasando
- [ ] `usePreferencesStore.spec.ts` — 3 escenarios pasando
- [ ] `apiClient.ts` interceptor 5xx diferenciado semánticamente (ADR-014)
- [ ] `apiClient.spec.ts` — 4 escenarios pasando
- [ ] `npm run test:unit` — 100% verde
- [ ] `npm run build` — Sin errores

## 5. Exclusiones

- NO crear componentes nuevos (eso es Fase 2)
- NO modificar la lógica de negocio de los stores — solo testear lo existente
- El interceptor SOLO se refactoriza, no se agrega retry logic real (eso es Fase 3A)

## 6. Archivos Impactados

| Archivo | Acción | Detalle |
|---------|--------|---------|
| `tests/layouts/MainLayout.spec.ts` | Crear | 4 escenarios |
| `tests/stores/useMenuStore.spec.ts` | Crear | 4 escenarios |
| `tests/stores/usePreferencesStore.spec.ts` | Crear | 3 escenarios |
| `services/apiClient.ts` | Modificar | L50-74 interceptor |
| `tests/services/apiClient.spec.ts` | Crear/Ampliar | 4 escenarios |
