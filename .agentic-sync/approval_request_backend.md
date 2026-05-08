# Solicitud de Aprobación de Arquitectura
**Agente:** Backend
**Historia de Usuario:** US-036 (Identity Governance)
**Criterios de Aceptación:** CA-29 a CA-32

Estimado Arquitecto Líder, he finalizado el diagnóstico para la fase de topología dinámica y gobernanza de caché, y he diseñado el siguiente plan técnico. 

## Plan de Ejecución Propuesto

### 1. CA-30 (Superposición Inclusiva) y CA-31 (Endpoint de Layout)
El endpoint `GET /api/v1/users/me/menu-layout` ya fue creado parcialmente en la iteración previa (`UserController`). 
El servicio `MenuLayoutService` actualmente recorre todos los roles del usuario e inserta los permisos en un `Set<String>`, lo cual matemáticamente genera una unión (Superposición Inclusiva) resolviendo **CA-30**. 
**Acción a realizar:** Mapearemos las nomenclaturas de los permisos brutos para asegurar que devuelva explícitamente los 7 Módulos Macro requeridos: `["WORKDESK", "SERVICE_DELIVERY", "BAM", "MODELER", "INTEGRATION", "PROJECTS", "ADMINISTRATION"]`.

### 2. CA-32 (Caché Híbrida y Auto-Curación Zero-Trust)
Actualmente `MenuLayoutService` usa `@Cacheable("menuTopology")` para optimizar la carga del menú. Sin embargo, no se está purgando automáticamente cuando los roles o permisos mutan.
**Acciones a realizar:**
1. Añadir `@CacheEvict(value = "menuTopology", key = "#result.username")` en `UserService.updateUser` y `deactivateUser`.
2. Añadir `@CacheEvict(value = "menuTopology", allEntries = true)` en los métodos mutables de `RoleService` (`updateRole`, `updateProcessPermissions`, `deleteRole`, `assignTemplateToUsers`) para forzar una purga total (Zero-Trust) y evitar accesos residuales al modificar un rol que puede pertenecer a N usuarios concurrentes.
3. Verificar la presencia de `@EnableCaching` en el proyecto.

---

### Solicitud de Confirmación
Solicito su autorización formal (`MODO EXECUTION`) para proceder con la implementación estricta mediante TDD y el protocolo SRE de auto-compilación. 

Por favor confirmar si aprueba:
1. El mapeo propuesto a un array de strings para la UI (`["WORKDESK", ...]`).
2. El uso de `allEntries = true` en la evicción de caché para modificaciones de Roles (para garantizar Zero-Trust sin queries complejos de invalidación selectiva).
