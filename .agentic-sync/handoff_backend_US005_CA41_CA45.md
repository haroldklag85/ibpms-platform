# Contrato de Arquitectura Backend (Iteración 24 | US-005: CA-41, CA-42, CA-43, CA-45)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Desarrollar los túneles seguros para instanciar (y asesinar inmediatamente) tokens temporales en Camunda como Sandbox. Proveer los metadatos de conectores del HUB.

## 📋 Contexto y Criterios de Implementación:

### Tarea 1: Simulador Hardcore Camunda V1 (CA-41)
*   Modifica el Controller pre-existente para atrapar `POST /api/v1/design/processes/sandbox-spawn`.
*   A nivel servicio, debe disparar `runtimeService.startProcessInstanceByKey(key, "SANDBOX_TEST-" + UUID.randomUUID())` en Camunda.
*   Inmediatamente en la siguiente línea de código tras arrancar, ejecuta `runtimeService.deleteProcessInstance(instanceId, "SIMULACION_SANDBOX_TERMINADA")` para matar la instancia y que no manche los dashboards de producción.
*   Devuelve HTTP 200 si todo fluye sin excepciones de parser Camunda.

### Tarea 2: Mock Hub de Integraciones (CA-45)
*   Crea un `IntegrationHubController.java`.
*   Expón el Endpoint `GET /api/v1/integrations/connectors`.
*   Retorna obligatoriamente los 3 sistemas dictaminados en los criterios:
    1. Microsoft O365 / Exchange (Tipo REST).
    2. Microsoft SharePoint (Tipo REST).
    3. Oracle NetSuite (Tipo SOAP).

### Tarea 3: Historial Git-Log Audit (CA-42) y Defensiva de Lock (CA-43)
*   Crea `GET /api/v1/design/processes/{key}/audit-logs` que devuelva un mock JSON de 3 acciones históricas ("IMPORT XML", "REQUEST DEPLOY", "ARCHIVED").
*   Asegúrate de que la Caché / Mapa In-Memory que guarda los Locks (Iteración 19) no tenga ningún Timeout/Eviction Policy automático. Los Locks deben ser duros y persistentes según mandato del CA-43.

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Compila tu arquitectura pre-vuelo Camunda y escuda en stash:
`git stash save "temp-backend-US005-ca41-ca45"`

Informa textualmente la confirmación del guardado.
