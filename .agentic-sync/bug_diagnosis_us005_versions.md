# Diagnóstico de Bug: Historial de Versiones (US-005)
**ID del Bug:** BUG-US005-VERSIONS
**Fecha:** 2026-06-06T13:55:00-05:00

## 🔬 Diagnóstico Forense

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Excepción HTTP 400 sin capturar | [BpmnDesignController.java](file:///y:/home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java#L255) | Al consultar `/api/v1/design/processes/{processDefinitionKey}/versions` para un proceso no persistido, `bpmnDesignService.obtenerPorTechnicalId(key)` lanza `IllegalArgumentException` que genera un error 400. Debería retornar `200 OK` con lista vacía `[]`. |
| Fallback con datos mock fijos | [BpmnDesigner.vue](file:///y:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue#L2256) | La función `fetchVersions` captura cualquier error de API e inyecta un mock estático ficticio (`v3 BORRADOR` de Ana García y `v2 ACTIVO` de Carlos M.) en lugar de manejar la lista vacía o reportar error. |
| Incompatibilidad de contrato de datos | Backend / Frontend | El Backend retorna `versionId`, `deploymentId`, `isLatest`, pero el Frontend espera `version`, `date`, `author`, `status` para renderizar el panel. |

## 💡 Causa Raíz
1. Si un proceso BPMN es un borrador nuevo (no guardado en DB), el servicio del backend lanza `IllegalArgumentException` porque no encuentra la entidad en base de datos.
2. El controlador no captura esta excepción localmente, delegando a un manejador global que responde HTTP 400.
3. El frontend recibe HTTP 400, entra al bloque `catch` de `fetchVersions` y carga los datos de prueba mock quemados.
4. Si el proceso sí existiera, el desajuste de llaves (`versionId` vs `version`, `isLatest` vs `status`) provocaría que el panel se dibuje sin datos o vacío porque no coinciden las llaves del JSON retornado.

## 🎯 Solución
1. **Backend:** En `getProcessVersions`, capturar `IllegalArgumentException` y retornar `ResponseEntity.ok(List.of())`. Si `dto.getCurrentVersion() == 0`, retornar también `ResponseEntity.ok(List.of())`. Si el proceso existe y tiene versión válida, retornar la lista de versiones con el contrato mapeado incluyendo: `versionId`, `deploymentId`, `isLatest`, `date` (mapeado de `dto.getUpdatedAt()`), `author` (mapeado de `dto.getCreatedBy()`), `status` (mapeado de `dto.getStatus()`).
2. **Frontend:** Remover el mock de fallback en `fetchVersions()`, mapear las llaves recibidas (`versionId` a `version`, `isLatest` a `status` [ACTIVO/ARCHIVADO], etc.), y agregar un mensaje visual `"No hay versiones publicadas aún"` si no existen versiones.
