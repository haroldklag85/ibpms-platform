# Solicitud de Revisión de Arquitectura - Iteración 84-DEV-LANE-ROLE-UAT-R2

## Contexto
Se requiere solventar el Bug Crítico R2-01 donde el despliegue BPMN retorna 403. Esto se debe a la ausencia del rol `BPMN_Release_Manager` y a la falta de fallback para `SUPER_ADMIN`.

## Plan de Implementación (Cambios Atómicos)

1. **Modificación en `BpmnDesignController.java`:**
   - Importar y añadir la anotación `@Slf4j` a nivel de clase para habilitar el registro (log).
   - En `deployBpmnProcess` (~L120), modificar la validación de roles para incluir `SUPER_ADMIN`: 
     `boolean hasRole = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("BPMN_Release_Manager") || a.getAuthority().contains("SUPER_ADMIN"));`
   - En `deployBpmnProcess`, tras la validación de permisos exitosa (~L148), agregar log de auditoría:
     `log.info("Deploy autorizado para usuario={} con rol={}", auth != null ? auth.getName() : "anonymous", hasRole ? "BPMN_Release_Manager/SUPER_ADMIN" : "sandbox_mode");`
   - En `reviewDeployRequest` (~L500), cambiar `@PreAuthorize` para soportar ambos roles: `@PreAuthorize("hasAnyRole('BPMN_Release_Manager', 'SUPER_ADMIN')")`.
   - En `requestDeploymentApproval` (~L603), actualizar el valor retornado `"assignedGroup"` a `"BPMN_Release_Manager, SUPER_ADMIN"`.

2. **Creación del Seed Data (`063-seed-bpmn-release-manager-role.sql`):**
   - Crear el archivo en `backend/ibpms-core/src/main/resources/db/changelog/changes/` con el script de inserción para `ROLE_BPMN_Release_Manager`.

3. **Registro en Liquibase (`db.changelog-master.yaml`):**
   - Agregar el nuevo script SQL al final del archivo maestro de Liquibase.

Por favor, Arquitecto Líder, confirmar si aprueba este plan para proceder con la ejecución.
