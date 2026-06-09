# 🔧→🧠 Escalamiento de Bug-Fix al Arquitecto Líder (Gobernanza 500)

**Bug:** Error 500 y Alerta Sistema Nivel 0 al ingresar a Gobernanza de Identidad
**US/CA afectado:** US-000 (Manejo de Errores) y US-029 / US-036 (Identidad)
**Rama:** `bugfix/DevDavid-governance-500`
**Agente ejecutor:** Bug-Fix Lead / Full-Stack
**Archivos modificados:**
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/GlobalExceptionHandler.java`
- `frontend/src/stores/rbacStore.js`
**Certificación Bug-Fix Lead:** ✅ PASS

## Resumen de la Corrección

1. **Frontend - Corrección de Endpoint Existente**: El método `fetchSystemProcesses` apuntaba a `/design/processes`, el cual no es un endpoint válido (requería `/catalog`). Se ajustó para consumir `apiClient.get('/design/processes/catalog')` y ahora el Catálogo de Procesos del Sistema carga exitosamente en la UI de Gobernanza.
2. **Backend - Degradación Grácil (Fail-Safe) para Endpoints Pendientes**: La pestaña Gobernanza consume múltiples endpoints que aún no han sido desarrollados en el Backend (ej. `/security/delegations`, `/security/anomalies`, `/security/audit/reports`). Estos devolvían un `NoResourceFoundException`. El manejador global `GlobalExceptionHandler` no capturaba esta excepción específica, por lo que caía al handler genérico `Exception.class`, traduciéndose en un **Error 500 Crítico**. Esto desencadenaba la "Alerta Nivel 0" en el interceptor global del Frontend y rompía el flujo de `Promise.all`. 
   - **Solución Quirúrgica:** Se añadió el manejador `@ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)` para devolver un código HTTP 404 (NOT_FOUND). El interceptor del Frontend ignora los 404, y los bloques `try-catch` del `rbacStore` ahora absorben el error silenciosamente, permitiendo cargar el resto de la pestaña sin bloqueos (Degradación Grácil total).

*Nota: Ambos ecosistemas (frontend y backend) fueron reparados en caliente sin necesidad de reconstrucción, acorde con la solicitud directa de priorizar el arreglo (Hot-Fix) en el contexto vivo.*

## Solicitud
Se solicita la doble certificación del Arquitecto Líder para confirmar que el parche no viola ADRs, que restablece la funcionalidad del Tablero de Gobernanza, y que está habilitado para realizar merge a DevDavid (o main si corresponde).
