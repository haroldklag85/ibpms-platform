# Solicitud de Revisión de Arquitectura - BACKEND (MC-1)

**Iteración:** 84-DEV-LANE-ROLE-FIX
**Agente:** Backend Senior (Rama: DevDavid)

**Plan de Implementación propuesto:**
He elaborado el plan para resolver los defectos D-01, D-02, D-03, D-05, D-06 y D-07, respetando estrictamente el blast radius limitado a los 4 archivos autorizados. 

* `DesplegarDefinicionService`: Inyección de `BpmnDesignPort` para resolver `processDesignId` (D-01).
* `BpmnLaneService`: 
    * Inyección de `RoleRepository` y `EntityManager` para validar existencias (D-03) y usar referencias JPA en lugar del antipatrón proxy (D-07).
    * Uso de `SecurityContextHolder` para asentar correctamente la auditoría (D-02).
    * Guard clause en `syncLanesFromDeployment` para evitar `ConstraintViolationException` (D-01).
* `BpmnLanePort`: Declaración de `replaceAssignmentsForRole` y limpieza de métodos fantasma (D-05, D-06).
* `LaneAdminController`: Inyección pura de `BpmnLanePort` (interfaz), eliminando acoplamiento a implementaciones concretas (D-06).

Por favor Arquitecto Líder, ¿apruebas este plan para que proceda a la fase de EXECUTION?
