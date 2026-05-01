## Reporte de Certificación QA — ARQ-005 (Core Deploy Pipeline)

| ID | Checkpoint | Comando/Verificación | Resultado | Estado |
|----|-----------|---------------------|-----------|--------|
| QA-005-01 | Hexagonal Controller | `grep -rn "Repository"` en Controller | SIN RESULTADOS | ✅ PASS |
| QA-005-02 | Hexagonal Controller | `grep -rn "infrastructure.jpa.entity"` en Controller | SIN RESULTADOS | ✅ PASS |
| QA-005-03 | Hexagonal PreFlight | `grep -rn "org.camunda"` en PreFlightAnalyzerService | SIN RESULTADOS | ✅ PASS |
| QA-005-04 | Hexagonal PreFlight | `grep -rn "infrastructure.jpa"` en PreFlightAnalyzerService | SIN RESULTADOS | ✅ PASS |
| QA-005-05 | Hexagonal BpmnDesign | `grep -rn "infrastructure.jpa.entity"` en BpmnDesignService | SIN RESULTADOS | ✅ PASS |
| QA-005-06 | Adapter Exists | Validar presencia de adaptadores en `infrastructure/adapters/` | Archivos encontrados exitosamente. | ✅ PASS |

---

## Checkpoint QA-005-07 — Iteración 7 (Definitivo)
- **Fecha:** 2026-04-30
- **Commit:** 96348536
- **Comando:** `mvn clean test -Dtest="BpmnDeployContractTest,SandboxIsolationTest,SandboxGovernanceTest,ProcessLockPersistenceTest,ExternalTaskTopicsCatalogTest,DeployRequestWorkflowTest,DataMappingIntegrityTest,BreakLockRbacTest,BpmnCopilotSseIntegrationTest,DlqAdminControllerApiIT,FormCertificationTest,IdentityGovernanceIntegrationTest,ApplicationTests" -pl ibpms-core`
- **Resultado compilación:** BUILD FAILURE (en ejecución de tests)
- **Tests ejecutados:** 35
- **Fallos de infraestructura (BeanCreation, servlet, etc.):** 1 (`FormCertificationTest` lanza `BeanCreationException` de Liquibase/Postgres debido a que no hereda `AbstractIntegrationTest` y trata de conectarse a un entorno que asume aprovisionado por `docker-compose`).
- **Fallos funcionales (401, assertions):** Múltiples errores funcionales pre-existentes detectados en los demás tests (no cuentan como regresión).
- **Veredicto Arquitectónico:** FAIL ❌
- **mvn clean compile (global):** BUILD SUCCESS (Compila exitosamente al 100%)

**Notas Adicionales:**
El criterio dictaba que un fallo por `BeanCreationException` resultaba en un FAIL. Dado que `FormCertificationTest` aún tiene problemas de inyección de contexto (al no levantar Testcontainers), se mantiene el veredicto arquitectónico en FAIL. El resto del código fuente del backend compiló correctamente en su totalidad.
