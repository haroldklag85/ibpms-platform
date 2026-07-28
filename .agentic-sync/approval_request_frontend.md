# Solicitud de Revisión Arquitectónica — Frontend UAT R2

**Fecha:** 2026-07-17T17:49:00-05:00  
**Agente:** Frontend Developer  
**Rama:** `DevDavid`  
**Handoff:** `handoff_frontend_84DEV_UAT_R2.md`

---

## Resumen del Plan

Solicito aprobación para implementar **2 correcciones quirúrgicas** en el frontend:

### Bug R2-02: Interceptor 403 destruye menú
- **Archivo:** `frontend/src/services/apiClient.ts` (líneas 248-266)
- **Causa raíz:** El bloque `else` catch-all del interceptor 403 ejecuta `purgeTopology()` para cualquier 403 no reconocido, incluyendo 403 operacionales (deploy sin permisos).
- **Corrección:** Cambiar el `else` a `else if` con validación explícita de `code === 'ACCESS_REVOKED' || code === 'ROLE_REVOKED'`. Los 403 operacionales ahora solo se loguean sin destruir el menú.
- **Garantía:** Los bloques de `SECURITY_VIOLATION`/`PROMPT_INJECTION` (CA-05) y `PRIVILEGES_CHANGED` (CA-7) NO se modifican.

### Bug R2-03: fetchForm mapea campos inexistentes
- **Archivo:** `frontend/src/stores/useFormDesignerStore.ts` (líneas 284-296)
- **Causa raíz:** El frontend usa `schemaVariables`, `title`, y `versionId` que NO existen en `FormDesignDTO.java`.
- **Verificación DTO:** Se leyó `FormDesignDTO.java` y se confirmaron los campos reales: `formFields`, `name`, `version`.
- **Corrección:** Reemplazar `schemaVariables` → `formFields`, `title` → `name`, `versionId` → `version`.

### Archivos NO modificados (cumplimiento de prohibición)
- ❌ `BpmnDesigner.vue`
- ❌ `IdentityGovernance.vue`
- ❌ CSS/HTML del panel Lane

## Solicitud Formal

Arquitecto Líder, solicito su aprobación para proceder con la ejecución de estos cambios. El plan detallado con diffs está documentado en el `implementation_plan.md` de mi sesión de trabajo.
