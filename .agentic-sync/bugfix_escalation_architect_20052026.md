# 🚀 BUGFIX ESCALATION TO ARCHITECT
**De:** BUG-FIX LEAD
**Para:** ARQUITECTO LÍDER
**Fecha:** 2026-05-20

## Resumen del Bugfix
- **Bug Identificado:** Creación de nuevos Roles devolvía 500 (`InvalidFormatException: Cannot deserialize value of type java.util.UUID from String`).
- **Causa Raíz:** En `IdentityGovernance.vue`, al crear un nuevo rol mediante la pestaña "Fábrica de Roles", el Frontend adjuntaba el valor temporal de inicialización de UI (ej: `"id": "R_"`) en el body JSON. La entidad `RoleEntity.java` del backend espera que `id` sea una auto-generación tipo UUID o nula durante la creación.
- **Acción Quirúrgica:** 
  1. Se modificó el payload en la invocación de `apiClient.post('/admin/roles')` dentro de la función `saveRole()` para suprimir la variable temporal `id` y `topology` del cuerpo de la solicitud POST.
  2. Tras una respuesta exitosa, se atrapa el UUID asignado por el backend (`res.data.id`) y se sobreescribe la variable local `roleForm.value.id` a fin de que la siguiente lógica Reactiva (actualización de la matriz) tenga a disposición la verdadera clave primaria.

## Ramas y Artefactos
- **Rama Actual:** `bugfix/DevDavid-role-uuid-deserialization`
- **Artefactos Forenses:** `.agentic-sync/bug_diagnosis_role_uuid_500.md`

## Solicitud de Aprobación
Se solicita al Arquitecto Líder certificar el parche para hacer merge con la rama base `DevDavid`. No se afectó infraestructura, librerías, ni contratos de APIs pre-existentes más allá de ajustar la carga útil a la interfaz esperada por Jackson y Spring Boot.
