# 🚀 BUGFIX ESCALATION TO ARCHITECT (UPDATE)
**De:** BUG-FIX LEAD
**Para:** ARQUITECTO LÍDER
**Fecha:** 2026-05-20

## Resumen del Bugfix (Segunda Iteración)
- **Bug Identificado:** Tras solucionar el enrutamiento (404), la creación del usuario devolvía un error **HTTP 400 Bad Request** silenciado (AxiosError).
- **Causa Raíz:** 
  1. **Discrepancia Regex (Contraseña):** El Frontend usaba una regla Zod para símbolos especiales (`/[!@#$%^&*]/`), pero el Backend omitía `#` y `^` en su regex (`[@$!%*?&]`). Esto ocasionaba que una contraseña estándar como `Root#Temp4Sys` pasara en UI pero detonara una falla `@Valid` en el Backend.
  2. **Enmascaramiento de UI:** El Backend (`GlobalExceptionHandler`) responde bajo el estándar `ProblemDetail` (depositando el mensaje en `detail`), pero el Frontend buscaba obtusamente la propiedad `message`, arrojando un toast genérico y ocultando el error real.
- **Acción Quirúrgica:** 
  1. **Sincronización de Contratos Regex:** Se inyectaron `#` y `^` en los patrones de `UserCreateRequestDTO` y `UserUpdateRequestDTO`. Se agregó el `?` faltante en el Frontend.
  2. **Captura Resiliente de Errores:** Se alteró el método `saveUser` (`IdentityGovernance.vue`) para inspeccionar `e.response?.data?.detail` antes de caer en fallbacks genéricos.

## Ramas y Artefactos
- **Rama Actual:** `bugfix/DevDavid-user-creation-404-payload-mapping`
- **Artefactos Forenses:** `.agentic-sync/bug_diagnosis_user_creation_400.md`

## Solicitud de Aprobación
Se solicita al Arquitecto Líder re-certificar este doble parche (Routing + Validación DTO). Ambos errores han sido erradicados con extrema precisión.
