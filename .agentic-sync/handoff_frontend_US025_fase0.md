# 🟢 Handoff Frontend — US-025 Fase 0: Hotfix Seguridad + Violación ADR-014

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** Frontend  
> **Prioridad:** 🔴 P0 — EJECUTAR DE INMEDIATO  
> **Rama Git:** sprint-6  
> **Pre-requisito:** Ninguno  
> **Gate de Salida:** `npm run build` sin errores + `npm run test:unit` 100%

---

## 1. Contexto y Diagnóstico Forense

La auditoría arquitectónica US-025 identificó 2 defectos P0 en el App Shell:

### Defecto ARQ-025-01: Interceptor 5xx Genérico (Activo — ADR-014 Violado)
- **Archivo:** `frontend/src/services/apiClient.ts` → Líneas 62-87
- **Problema:** Los códigos `[500, 502, 503, 504]` se agrupan bajo UN solo mensaje:
  ```typescript
  message: `Colapso del Servidor / Integración Cíclica`  // ← L84
  ```
- **Impacto:** Los usuarios y developers no distinguen entre "bug en backend" (500), "backend caído" (502/503), y "proxy timeout" (504). Esto provocó >5 horas de debugging inútil (incidente documentado en ADR-014).

### Defecto ARQ-025-02: Token Rotator + window.location.href (Bajo riesgo)
- **Archivo:** `frontend/src/stores/authStore.ts` → Línea 90
- **Problema:** Dentro del `catch` del Token Rotator, después de `logout()` se ejecuta `window.location.href = '/login'`. Esto es redundante (logout() ya limpia estado) y causa un hard-reload innecesario.
- **Nota:** El `logout()` en `MainLayout.vue` L287-290 ya es correcto (`authStore.logout()`). No se requiere cambio ahí.

---

## 2. Tareas

### Tarea F0.1 — Refactorizar Interceptor 5xx en apiClient.ts (ADR-014)

**Archivo:** `frontend/src/services/apiClient.ts`  
**Líneas a modificar:** 62-87

**CÓDIGO ACTUAL (DEFECTUOSO):**
```typescript
// CA-21, CA-1, CA-37: Alertas Rojas Imborrables / Captura Global 5xx
if (error.response && [500, 502, 503, 504].includes(error.response.status)) {
    console.error('Fatal Level 0 Dispatching', error.response.status);
    
    // CA-37: Generic 500 Error Toast
    if (error.response.status === 500) {
        const body = document.querySelector('body');
        if (body && !document.getElementById('server-error-toast')) {
            const toast = document.createElement('div');
            toast.id = 'server-error-toast';
            toast.style.cssText = '...';
            toast.innerHTML = '❌ Error interno del servidor. Inténtelo más tarde.';
            body.appendChild(toast);
            setTimeout(() => {
                toast.style.opacity = '0';
                setTimeout(() => toast.remove(), 500);
            }, 4000);
        }
    }
    
    const event = new CustomEvent('global-error-dispatch', { detail: { 
        code: error.response.status,
        message: `Colapso del Servidor / Integración Cíclica`  // ← VIOLACIÓN ADR-014
    }});
    window.dispatchEvent(event);
    return Promise.reject(error);
}
```

**CÓDIGO CORRECTO (REEMPLAZO COMPLETO de L62-L87):**
```typescript
// ═══ ADR-014: Diferenciación Semántica de Errores 5xx ═══
if (error.response && error.response.status >= 500) {
    const status = error.response.status;
    const traceId = error.response.headers?.['x-correlation-id'] || 'N/A';
    
    if (status === 500) {
        // Categoría 1: Bug en el backend — Toast imborrable con traceId
        console.error(`[ADR-014] Error 500 — Trace: ${traceId}`);
        const event = new CustomEvent('global-error-dispatch', { detail: { 
            code: 500,
            type: 'SERVER_ERROR',
            message: `Error interno del servidor (Trace: ${traceId}). Contacte soporte.`,
            dismissible: false
        }});
        window.dispatchEvent(event);
        
        // Toast DOM fallback (CA-37)
        const body = document.querySelector('body');
        if (body && !document.getElementById('server-error-toast')) {
            const toast = document.createElement('div');
            toast.id = 'server-error-toast';
            toast.style.cssText = 'position:fixed; bottom:20px; right:20px; background:#ef4444; color:white; padding:12px 20px; border-radius:8px; z-index:99999; box-shadow:0 10px 15px -3px rgba(0,0,0,0.1); font-family:sans-serif; font-size:14px; font-weight:bold;';
            toast.innerHTML = `❌ Error interno del servidor (Trace: ${traceId}). Contacte soporte.`;
            body.appendChild(toast);
            // NO auto-remove: este toast es imborrable per ADR-014
        }
    } else if (status === 502 || status === 503) {
        // Categoría 2: Servidor no disponible — Toast dismissible + auto-retry ya manejado arriba (L49-60)
        console.warn(`[ADR-014] Servidor no disponible (${status})`);
        const event = new CustomEvent('global-error-dispatch', { detail: { 
            code: status,
            type: 'SERVICE_UNAVAILABLE',
            message: `El servidor no está disponible (${status}). Verificando conexión...`,
            dismissible: true,
            autoRetry: true
        }});
        window.dispatchEvent(event);
    } else if (status === 504) {
        // Categoría 3: Timeout del proxy — Toast dismissible
        console.warn(`[ADR-014] Gateway Timeout (504)`);
        const event = new CustomEvent('global-error-dispatch', { detail: { 
            code: 504,
            type: 'GATEWAY_TIMEOUT',
            message: 'Tiempo de espera agotado. Verifique que el servidor esté activo.',
            dismissible: true
        }});
        window.dispatchEvent(event);
    }
    return Promise.reject(error);
}
```

---

### Tarea F0.2 — Eliminar `window.location.href` redundante en authStore.ts

**Archivo:** `frontend/src/stores/authStore.ts`  
**Línea:** 90

**CÓDIGO ACTUAL:**
```typescript
// Dentro del catch del Token Rotator (L86-93):
} catch (error) {
    console.error('[AuthStore] Falla en la Rotación del Token...');
    alert('Sesión expirada o privilegios revocados. Inicie sesión nuevamente.');
    logout();
    window.location.href = '/login';  // ← REDUNDANTE: logout() ya limpia estado, RouteGuard redirige
}
```

**CÓDIGO CORRECTO:**
```typescript
} catch (error) {
    console.error('[AuthStore] Falla en la Rotación del Token. Forzando cierre de sesión.');
    logout();
    // RouteGuard detecta token=null y redirige a /login automáticamente
}
```

**Cambios:**
- Eliminar `alert()` bloqueante (anti-patrón UX)
- Eliminar `window.location.href` (causa hard-reload que destruye estado de Vue)
- `logout()` + RouteGuard es el patrón correcto per ADR-002

---

## 3. Archivos Impactados

| Archivo | Líneas | Acción | Defecto |
|---------|:------:|:------:|---------|
| `services/apiClient.ts` | 62-87 | Reemplazar bloque | ARQ-025-01 (ADR-014) |
| `stores/authStore.ts` | 87-91 | Simplificar catch | ARQ-025-02 |

---

## 4. Límites (NO Tocar)

- ❌ NO modificar `MainLayout.vue` — El logout() ya es correcto (L287-290)
- ❌ NO crear tests nuevos en esta fase — Los tests son Fase 1A
- ❌ NO crear componentes nuevos — Los toasts Vue son Fase 3A
- ❌ NO instalar dependencias nuevas
- ❌ NO tocar el interceptor de errores 401 (L99-104) — Ya funciona correctamente
- ❌ NO tocar el interceptor de errores 403 (L125-184) — Funciona correctamente

---

## 5. Gate de Validación

```bash
cd frontend
npm run build    # Debe compilar sin errores
npm run test:unit  # Tests existentes deben pasar 100%
```

**Verificación manual adicional:**
1. Levantar backend Docker
2. Navegar a la app
3. Apagar el backend → Debe mostrar "El servidor no está disponible (502)" (no "Colapso del Servidor")
4. Volver a encender → El toast 502 debe desaparecer tras retry exitoso

---

## 6. Próximo Handoff

Al completar Fase 0, tu siguiente handoff es:
→ `.agentic-sync/handoff_frontend_US025_fase1A.md` (Tests fundacionales + tests del interceptor refactorizado)

Espera aprobación del Arquitecto Líder antes de avanzar.
