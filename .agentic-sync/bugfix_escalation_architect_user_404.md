# 🚀 BUGFIX ESCALATION TO ARCHITECT
**De:** BUG-FIX LEAD
**Para:** ARQUITECTO LÍDER
**Fecha:** 2026-05-20

## Resumen del Bugfix
- **Bug Identificado:** Falla crítica al crear/actualizar Usuarios en la pestaña "Usuarios y Sesiones". El navegador reportaba 404 Not Found a la ruta `/api/v1/api/v1/admin/users`.
- **Causa Raíz:** 
  1. **Routing frontend corrupto:** Las invocaciones a la red contenían un hardcoding de `/api/v1`, produciendo duplicación ya que la base de Axios (`apiClient`) inyecta dicho segmento automáticamente.
  2. **Payload divergente:** Al remover el 404, la creación del usuario fallaba porque el modelo inyectado no cuadraba con `UserCreateRequestDTO` (`name` vs `username`, `roles` vs `roleIds`).
- **Acción Quirúrgica:** 
  1. Uso de expresiones regulares para purgar masivamente el prefijo hardcodeado `/api/v1` en todas las llamadas `apiClient` e `integrationStore` dentro de `IdentityGovernance.vue`.
  2. Refactorización explícita de `saveUser()` aislando un bloque `updatePayload` (para PUT) y un bloque `createPayload` (para POST), obligando el mapeo explícito de variables a la nomenclatura del backend.

## Ramas y Artefactos
- **Rama Actual:** `bugfix/DevDavid-user-creation-404-payload-mapping`
- **Artefactos Forenses:** `.agentic-sync/bug_diagnosis_user_creation_404.md`

## Solicitud de Aprobación
Se solicita al Arquitecto Líder certificar el parche para hacer merge con la rama base `DevDavid`. No se alteró la lógica profunda de seguridad de Spring Boot, logrando resolver el fallo netamente desde la correcta composición y orquestación del Frontend hacia la API.
