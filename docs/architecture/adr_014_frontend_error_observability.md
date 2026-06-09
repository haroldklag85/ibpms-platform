# ADR-014: Observabilidad de Errores HTTP en el Frontend

> **Autor:** Arquitecto Líder AI  
> **Fecha:** 2026-04-20  
> **Sprint:** 6.2  
> **Estado:** Propuesto  
> **Origen:** Incidente "Integración Cíclica 500" — [walkthrough_cyclic_integration_500.md](./auditoria/walkthrough_cyclic_integration_500.md)

---

## 1. Contexto

Durante el Sprint 6.2, un incidente crítico bloqueó la certificación UAT del Journey J-04 durante más de 5 horas. El síntoma visible era una pantalla de error permanente con el mensaje:

```
ALERTA DEL SISTEMA: NIVEL 0
Colapso del Servidor / Integración Cíclica
Código de Error: 500
```

La investigación forense reveló que el error **real** no era un HTTP 500, sino un **502 Bad Gateway** generado por el proxy de Vite cuando el backend estaba en un bucle de reinicio. Sin embargo, el interceptor de `apiClient.ts` agrupaba los códigos `[500, 502, 503, 504]` bajo un solo mensaje genérico, eliminando toda posibilidad de diagnóstico diferenciado desde la interfaz.

Este enmascaramiento provocó que el equipo invirtiera horas buscando un error inexistente en los controladores de autenticación, cuando el problema real era un fallo de infraestructura (bytecode huérfano en Docker).

---

## 2. Decisión

### Status Quo (Problemático)

```typescript
// apiClient.ts:50-74
if (error.response && [500, 502, 503, 504].includes(error.response.status)) {
    const event = new CustomEvent('global-error-dispatch', { detail: { 
        code: error.response.status,
        message: `Colapso del Servidor / Integración Cíclica`  // ← Único mensaje para 4 códigos
    }});
    window.dispatchEvent(event);
}
```

### Propuesta Aprobada: Diferenciación Semántica de Códigos 5xx

Se establece que el interceptor de errores del `apiClient.ts` **DEBE** diferenciar los códigos HTTP 5xx en **tres categorías semánticas** con mensajes distintos:

| Categoría | Códigos | Mensaje UI Propuesto | Significado Técnico |
|-----------|---------|---------------------|---------------------|
| **Error de Servidor** | `500` | "Error interno del servidor. Contacte soporte." | Bug en el código backend |
| **Servidor No Disponible** | `502`, `503` | "El servidor no está disponible. Reintentando..." | Backend caído, reiniciando, o en mantenimiento |
| **Timeout de Conexión** | `504` | "Tiempo de espera agotado. Verifique su conexión." | Proxy Vite no puede alcanzar al backend |

### Reglas de Implementación

1. **El código HTTP real debe ser visible** en el toast de error (no solo el mensaje genérico).
2. **Los errores 502/503 deben incluir reintento automático** con backoff exponencial (máximo 3 intentos) antes de mostrar la alerta destructiva.
3. **Los errores 500 deben preservar el `traceId`** (`X-Correlation-ID` del header de respuesta) y mostrarlo al usuario para facilitar triaje con el equipo de backend.
4. **Nunca agrupar códigos con semántica distinta** bajo un mismo mensaje de UI.

---

## 3. Consecuencias

### Positivas

- **Reducción de MTTR:** Los desarrolladores podrán distinguir inmediatamente entre "el backend tiene un bug" (500) y "el backend no está arriba" (502/503), eliminando horas de diagnóstico erróneo.
- **Mejor experiencia de usuario:** Los usuarios verán mensajes que reflejan la situación real ("servidor en mantenimiento" vs. "error interno").
- **Reintento automático:** Los errores transitorios (502/503 durante reinicio de Docker) se resolverán solos sin intervención del usuario.

### Negativas

- **Complejidad adicional:** El interceptor pasa de un `if` simple a una estructura condicional más elaborada.
- **Requiere coordinación:** El backend debe garantizar que los headers `X-Correlation-ID` estén presentes en todas las respuestas 500.

---

## 4. Archivos Impactados

| Archivo | Cambio Requerido |
|---------|-----------------|
| `frontend/src/services/apiClient.ts` | Refactorizar interceptor de errores 5xx (líneas 50-74) |
| `frontend/src/components/common/ErrorAlert.vue` | Actualizar soporte para mensajes diferenciados |
| `frontend/src/services/mockAdapter.ts` | Sin cambios (passthrough ya configurado) |

---

## 5. Patrón de Referencia: Interceptor Diferenciado

```typescript
// PROPUESTA: Reemplazo del bloque lines 50-74 en apiClient.ts

if (error.response) {
    const status = error.response.status;
    const traceId = error.response.headers?.['x-correlation-id'] || 'N/A';

    if (status === 500) {
        // Error real del servidor — bug en el código
        dispatchError({
            code: 500,
            type: 'SERVER_ERROR',
            message: `Error interno del servidor (Trace: ${traceId}). Contacte soporte.`,
            dismissible: false
        });
    } else if (status === 502 || status === 503) {
        // Servidor no disponible — probablemente reiniciando
        dispatchError({
            code: status,
            type: 'SERVICE_UNAVAILABLE',
            message: `El servidor no está disponible (${status}). Verificando conexión...`,
            dismissible: true,
            autoRetry: true
        });
    } else if (status === 504) {
        // Timeout del proxy
        dispatchError({
            code: 504,
            type: 'GATEWAY_TIMEOUT',
            message: 'Tiempo de espera agotado. Verifique que el servidor esté activo.',
            dismissible: true
        });
    }
}
```

---

## 6. Relación con Otros ADRs

| ADR | Relación |
|-----|----------|
| [ADR-011 Local CQRS](./adr_011_local_cqrs_v1.md) | Define el contrato de respuestas error del backend |
| [ADR Docker Runtime](./auditoria/ADR_DOCKER_RUNTIME_ARCHITECTURE.md) | Documenta el escenario de bootloop que genera 502/503 |
| [CA-37](../../../frontend/src/services/apiClient.ts) | Criterio de aceptación original que definió el toast genérico |

---

## 7. Matriz de Decisión

| Alternativa | Observabilidad | Complejidad | UX | Selección |
|-------------|---------------|-------------|-----|-----------|
| A) Mensaje único genérico (status quo) | ❌ Nula | ✅ Baja | ❌ Confusa | Rechazada |
| B) Código HTTP en el toast (mínima) | ⚠️ Parcial | ✅ Baja | ⚠️ Técnica | Descartada |
| **C) Diferenciación semántica + traceId** | ✅ Alta | ⚠️ Media | ✅ Clara | **Aprobada** |
| D) Observabilidad full (Sentry/Datadog) | ✅ Máxima | ❌ Alta | ✅ Clara | V2 Roadmap |
