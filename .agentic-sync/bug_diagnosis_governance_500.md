# 🩺 Diagnóstico Forense: Bug-Fix Governance 500
**ID:** `bug_diagnosis_governance_500`
**Fecha:** 2026-05-19
**Agente:** BUG-FIX LEAD

## Descripción del Bug
El usuario (super admin) reporta que al navegar a la pestaña "Gobernanza" el sistema arroja una "ALERTA DEL SISTEMA: NIVEL 0" indicando un Error 500, bloqueando por completo la interfaz. La consola de red muestra errores 500 en llamadas a `/api/v1/design/processes` y `/api/v1/security/delegations`. La consola del backend arroja `NoResourceFoundException: No static resource api/v1/security/delegations`.

## Análisis Forense (Quadruple Check)
1. **Síntoma / Capa Probable:** Full Stack (Backend arrojando 500 y Frontend consumiendo endpoints inexistentes/erróneos).
2. **Archivos Sospechosos y Causas:**
   - `frontend/src/stores/rbacStore.js`: La acción `fetchSystemProcesses` apuntaba a `/design/processes` pero el backend espera `GET /catalog` al final de la ruta.
   - `frontend/src/stores/rbacStore.js`: La acción `fetchDelegations` apuntaba a `/security/delegations`, un endpoint que aún no existe en el backend (funcionalidad pendiente de implementarse).
   - `backend/ibpms-core/.../GlobalExceptionHandler.java`: En Spring Boot 3.2, un endpoint inexistente levanta `NoResourceFoundException`. Al no estar explícitamente manejada en el GlobalExceptionHandler, esta excepción caía en el manejador genérico `Exception.class` (Línea 172) que la enmascaraba como un `500 INTERNAL_SERVER_ERROR`. Esto causaba que el interceptor global del Frontend (`apiClient.ts`) detectara un error >= 500 y lanzara la "Alerta Nivel 0" rompiendo el flujo.

## Plan Quirúrgico
- **Frontend (`rbacStore.js`):** Corregir el endpoint de `fetchSystemProcesses` a `/design/processes/catalog`.
- **Backend (`GlobalExceptionHandler.java`):** Añadir un manejador explícito para `NoResourceFoundException` que devuelva `HttpStatus.NOT_FOUND (404)`.
- **Resultado Esperado:** Al devolver 404 para los endpoints no implementados (delegations, anomalies, etc.), el bloque `try-catch` del `rbacStore` absorberá el 404 e imprimirá un log en consola sin interrumpir `Promise.all`. La UI de Governance cargará con degradación grácil y los procesos del sistema cargarán correctamente.
