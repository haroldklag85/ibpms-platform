# Solicitud de Revisión Arquitectónica - Backend (DevDavid)

**Iteración:** 09-DEV-REMEDIATION (US-036 Remediación)
**Agente:** Backend
**Criterios:** CA-14, CA-16, CA-17, CA-20b, CA-21, CA-23, CA-24, CA-25

## Resumen del Plan de Implementación (implementation_plan.md)

1. **Refactor de Seguridad:**
   - Eliminaremos `JwtSecurityFilter.java` y su inyección en `SecurityConfig.java` para prevenir duplicación de filtros.
   - Refactorizaremos `JwtBlacklistService.java` para conectarlo exclusivamente a Redis (removiendo el `ConcurrentHashMap`) con política Fail-Open.
   - Conectaremos `JwtAuthFilter` con `JwtBlacklistService` unificando la TRL (Token Revocation List) sobre Redis.

2. **Reportes de Auditoría (CA-16, CA-24):**
   - Crearemos `AuditReportService.java` para centralizar la generación CSV y el cálculo del SHA-256.
   - Expondremos el servicio correctamente bajo el endpoint `POST /api/v1/security/audit/reports` en `AuditReportController.java`.

3. **Traza Indeleble (CA-17):**
   - Inyectaremos la interfaz `AuditLogPort` en `UserService.java`. Se escribirán logs explícitos de auditoría en la tabla `ibpms_audit_log` tras cualquier mutación de status o asignación de roles de un usuario.

4. **Delegación In-Flight (CA-23):**
   - Habilitaremos el método `.revertAssignee()` (usando `TaskService`) dentro del bloque `evaluateAndRevertTaskIfNeeded` de `TaskDelegationService`.
   - Se inyectará `AuditLogPort` para registrar explícitamente en base de datos la reversión on-the-fly.

5. **Unión Multirrol RLS (CA-20b):**
   - Integraremos globalmente `DataSegregationService` en `BpmTaskService.java` (y otros extractores si aplica), reemplazando las consultas hardcodeadas (`taskAssignee().or().taskCandidateGroupIn()`) para unificar y blindar contra ataques IDOR/BOLA.

Solicito su aprobación formal para proceder al modo `EXECUTION`.
