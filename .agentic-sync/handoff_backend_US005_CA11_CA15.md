# Contrato de Arquitectura Backend (Iteración 18 | US-005: CA-12 al CA-15)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Desarrollar controladores integrados a la API de Camunda para gestión de incidentes, versionamiento histórico y estampillado de auditoría forense.

## 📋 Contexto y Criterios de Aceptación:

Nos adentramos en el core de Resiliencia y Control de Versiones Operativas (DRP BPMN).

*   **CA-12 (Late Binding DMN por Defecto):**
    *   **Acción:** En tu clase `PreFlightAnalyzerService`, si detectas una Tarea de Regla de Negocio (`businessRuleTask` que apunte a DMN), INYECTA o valida que contenga la propiedad `camunda:decisionRefBinding="latest"`. Si falta, y no hay versión explícita, debes parchearlo o generar un Warning forzado en el Pre-Flight.
*   **CA-13 (Centro de Incidentes):**
    *   Crea el controlador `IncidentController.java` (`/api/v1/admin/incidents`).
    *   `GET`: Lista incidentes (Equivalente al RuntimeService de Camunda).
    *   `POST .../retry`: Intenta re-ejecutar el job asíncrono fallido (`managementService.executeJob(...)`).
    *   `DELETE .../{id}`: Anula tajantemente el proceso (`runtimeService.deleteProcessInstance(...)`).
*   **CA-14 (Sello Forense de Migración):**
    *   Ve a la clase `ProcessMigrationService.java` de la iteración anterior.
    *   Añade la lógica: Inmediatamente después de migrar una instancia a la V2, inyecta por fuerza bruta en Camunda (RuntimeService.setVariable) la variable de negocio inmutable: `SYS_MIGRATION_AUDIT` con el valor `"[⚠️ MIGRACIÓN ESTRUCTURAL: v1 -> v2 el Fecha/Hora]"`. *(Las aserciones de Pantalla 17 en frontend se encargarán de leerlo luego).*
*   **CA-15 (Rollback Un Clic):**
    *   Extiende `BpmnDesignController.java`.
    *   `GET .../processes/{key}/versions`: Retorna lista ordenada de versiones de esa Definition Key.
    *   `POST .../processes/{key}/rollback/{versionId}`: Lee el XML de la BD para la versión especificada y lo redespliega al motor tal como está, creando así una nueva definición "Latest" idéntica a la restaurada.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Congela tus Controladores en un stash cuando superes la compilación local:
`git stash save "temp-backend-US005-ca11-ca15"`

Informa textualmente al humano que lo lograste.
