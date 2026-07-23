# Handoff: AI DEVELOPER AGENT - BACKEND
**Iteración:** 65-DEV (US-002 / CA-1 al CA-8)
**Contexto de Memoria Aislada:** Backend Spring Boot. NO conoces Vue.

## 1. MISIÓN Y REGLA DE ORO V2
Tu misión es construir el Backend del Motor de Reclamos (US-002) blindándolo contra concurrencia maliciosa y ataques.
**REGLA DE ORO V2:** Analiza semánticamente CA-1 al CA-8. Excluye estrictamente componentes de V2 como Analítica Predictiva o Dashboards Masivos. Programa solo transaccionalidad V1.

## 2. NOTAS DE IMPLEMENTACIÓN TÉCNICA (TIN)
1. **Pessimistic Locking (CA-01):** El `POST /api/tasks/{id}/claim` debe usar el método nativo `taskService.claim()`. Intercepta `TaskAlreadyClaimedException` o `OptimisticLockingException` y retorna obligatoriamente `HTTP 409 Conflict`. PROHIBIDO el uso de `synchronized`.
2. **IDOR & Cross-Tenant (CA-06):** Extrae el principal JWT. Ejecuta: `if (task.getTenantId() !== jwt.getTenantId()) throw new AccessDeniedException()`. No confíes en el ID de la URL.
3. **Bulk Claim Throttling (CA-02):** El endpoint `POST /api/tasks/claim-batch` requiere `@Size(max = 50)` en su DTO de entrada.
4. **Data Purge Hook (S3 Orphans - CA-07):** En `POST /api/tasks/{id}/unclaim`, busca en BD si existen uploads huérfanos del usuario atados a la tarea. Si los hay, usa `@Async` para borrar en AWS S3.
5. **XSS Protection (CA-05):** Si hay un `handoffMessage`, sanitízalo imperativamente con `OWASP Java HTML Sanitizer` ANTES de persistirlo con `taskService.createComment()`.
6. **Data Masking PII (CA-04):** En `GET /api/tasks/{id}`, si `task.assignee !== currentUser.id`, ofusca las strings marcadas como `@PII` con Regex (`str.replace(/.(?=.{4})/g, '*')`) y omite retornar URLs (Presigned) de adjuntos S3.

## 3. ENTREGABLE ESTRICTO
Realiza las pruebas y empaqueta en tu rama local usando *exactamente*:
`git stash save "temp-backend-US002-CA1-8"`
Notifica al humano cuando finalices.
