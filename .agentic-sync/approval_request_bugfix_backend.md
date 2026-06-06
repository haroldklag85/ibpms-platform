# Approval Request - Backend BUG-FIX US-005 (Versions History API)

**Solicitado por:** [⚙️ BACKEND - JAVA]
**Para:** 🧠 ARQUITECTO LÍDER / BUG-FIX LEAD
**Fecha:** 2026-06-06T13:58:00-05:00

## 1. Resumen del Diagnóstico y Solución
El endpoint `/api/v1/design/processes/{processDefinitionKey}/versions` fallaba con `IllegalArgumentException` (HTTP `400 Bad Request`) cuando el proceso no existía en base de datos o era un borrador sin versiones desplegadas (`currentVersion == 0`).

Se propone aplicar la corrección directamente en `BpmnDesignController.java` de la siguiente manera:
- Interceptar `IllegalArgumentException` lanzada por `bpmnDesignService.obtenerPorTechnicalId` y retornar `200 OK` con una lista vacía `List.of()`.
- Validar si el proceso tiene `currentVersion == null` o `currentVersion == 0`, retornando también `List.of()`.
- Retornar la lista con el mapeo completo de campos requerido por el frontend (`versionId`, `version`, `deploymentId`, `isLatest`, `date`, `author`, `status`), proporcionando defaults seguros en caso de valores nulos.
- Mantener la trazabilidad estricta: `// @Traceability: US-005, CA-15, BUG-FIX: Retornar lista vacía de versiones si el proceso no tiene despliegues`.

Además, en `BpmnDeployContractTest.java`, se sembró el diseño del proceso correspondiente en base de datos para asegurar el comportamiento correcto de guardado de borrador y consulta de versiones de un proceso existente.

## 2. Plan de Pruebas y Verificación
- **Pruebas Objetivo**: 
  1. `SandboxGovernanceTest.testGetProcessVersionsNotFoundReturnsEmptyList`
  2. `BpmnDeployContractTest.testGetVersionsForExistentProcessReturnsAlignedFields`
- **Comando de Ejecución**: `mvn test -Dtest=SandboxGovernanceTest,BpmnDeployContractTest` en consola WSL nativa.

El ticket de corrección backend está listo para aprobación y paso a la fase de **EJECUCIÓN**.

