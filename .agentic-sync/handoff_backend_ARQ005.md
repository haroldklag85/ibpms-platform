# 🔧 Handoff Backend — Remediación ARQ-005 (Bloque 1: US-005 Core Deploy Pipeline)

## 1. Metadatos y SSOT
- **Iteración:** Remediación Arquitectónica Post-Auditoría US-005
- **Rama Git:** `sprint-6`
- **SSOT:** `docs/requirements/epics/epic_B_formularios_bpmn.md` → US-005 (CA-1 a CA-14)
- **Hallazgos Origen:** `audit_arquitectura_US005.md` → ARQ-005-01, ARQ-005-02, ARQ-005-03
- **Orden de Ejecución:** Backend → QA → Frontend (informativo)

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto |
|-----|---------|
| `adr-001-hexagonal-architecture.md` | Los Controllers NO deben inyectar Repositories JPA. Los Application Services NO deben importar entidades JPA. |
| `adr-003-camunda7-embedded.md` | La API de Camunda (Model API, TaskService, RuntimeService) solo debe existir en Adaptadores Secundarios (`infrastructure/adapters/`). |

**Principio violado:** Los Application Services (`PreFlightAnalyzerService`, `BpmnDesignService`) importan directamente entidades JPA y API Camunda. El Controller (`BpmnDesignController`) inyecta repositorios JPA.

## 3. Rutas Exactas y Contexto Preexistente

### Archivos a MODIFICAR:

| Archivo | Estado Actual | Problema |
|---------|--------------|----------|
| `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java` (448 líneas) | Inyecta `ExternalTaskTopicRepository` (L19,31) y `DataMappingRepository` (L32). Crea `DataMappingEntity` directamente (L336-344). | ARQ-005-01 |
| `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/PreFlightAnalyzerService.java` (436 líneas) | Importa 15+ clases `org.camunda.bpm.model.bpmn.*` (L22-36). Importa `BpmnDesignAuditLogEntity`, `BpmnProcessDesignEntity`, `RoleEntity`, `ExternalTaskTopicEntity` (L4-8, L54, L144). | ARQ-005-02 |
| `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/BpmnDesignService.java` (261 líneas) | Importa 4 entidades JPA y 4 repos directamente (L6-13). Sin Port/Adapter. Tiene intención DDD parcial (L49 `BpmnProcessDesign.crear()`). | ARQ-005-03 |

### Archivos a CREAR:

| Archivo (Nuevo) | Propósito |
|-----------------|-----------|
| `backend/.../application/port/out/BpmnDesignPort.java` | Puerto de salida para CRUD de `BpmnProcessDesign` |
| `backend/.../application/port/out/BpmnAuditPort.java` | Puerto de salida para audit logs del designer |
| `backend/.../application/port/out/ProcessLockPort.java` | Puerto de salida para lock pesimista |
| `backend/.../application/port/out/DeployRequestPort.java` | Puerto de salida para solicitudes de despliegue |
| `backend/.../application/port/out/BpmnValidationPort.java` | Puerto de salida para validación XML BPMN (encapsula Camunda Model API) |
| `backend/.../application/port/out/ExternalTaskTopicPort.java` | Puerto de salida para catálogo de topics |
| `backend/.../application/port/out/DataMappingPort.java` | Puerto de salida para data mappings |
| `backend/.../infrastructure/adapters/BpmnDesignJpaAdapter.java` | Implementa `BpmnDesignPort` usando `BpmnProcessDesignRepository` |
| `backend/.../infrastructure/adapters/BpmnAuditJpaAdapter.java` | Implementa `BpmnAuditPort` usando `BpmnDesignAuditLogRepository` |
| `backend/.../infrastructure/adapters/ProcessLockJpaAdapter.java` | Implementa `ProcessLockPort` usando `ProcessLockRepository` |
| `backend/.../infrastructure/adapters/DeployRequestJpaAdapter.java` | Implementa `DeployRequestPort` usando `DeployRequestRepository` |
| `backend/.../infrastructure/adapters/CamundaBpmnValidationAdapter.java` | Implementa `BpmnValidationPort` usando `org.camunda.bpm.model.bpmn.*` |
| `backend/.../infrastructure/adapters/ExternalTaskTopicJpaAdapter.java` | Implementa `ExternalTaskTopicPort` |
| `backend/.../infrastructure/adapters/DataMappingJpaAdapter.java` | Implementa `DataMappingPort` |

## 4. Snippets Prescriptivos

### 4.1 — Puerto de Validación BPMN (Encapsulando Camunda)

```java
// application/port/out/BpmnValidationPort.java
package com.ibpms.poc.application.port.out;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import java.io.InputStream;

public interface BpmnValidationPort {
    DeploymentValidationResponse validateBpmnStream(InputStream bpmnStream, java.util.List<String> activeTopics, java.util.List<String> vipRoleNames);
}
```

### 4.2 — Adaptador Camunda (Infraestructura)

```java
// infrastructure/adapters/CamundaBpmnValidationAdapter.java
package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.port.out.BpmnValidationPort;
import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
// ... todos los imports de org.camunda aquí
import org.springframework.stereotype.Component;
import java.io.InputStream;

@Component
public class CamundaBpmnValidationAdapter implements BpmnValidationPort {
    @Override
    public DeploymentValidationResponse validateBpmnStream(InputStream bpmnStream, java.util.List<String> activeTopics, java.util.List<String> vipRoleNames) {
        // MOVER TODA la lógica del método `analizar(InputStream)` de PreFlightAnalyzerService aquí.
        // L125-311 del archivo actual.
    }
}
```

### 4.3 — Puerto genérico de Lock

```java
// application/port/out/ProcessLockPort.java
package com.ibpms.poc.application.port.out;

import java.util.Optional;

public interface ProcessLockPort {
    Optional<ProcessLockInfo> findLock(String processKey);
    void saveLock(String processKey, String userId, String sessionId);
    void deleteLock(String processKey);
    
    record ProcessLockInfo(String processKey, String lockedBy, java.time.LocalDateTime lockedAt, String browserSessionId) {}
}
```

### 4.4 — Limpieza del Controller (ARQ-005-01)

En `BpmnDesignController.java`:
- **ELIMINAR** las inyecciones de `ExternalTaskTopicRepository` y `DataMappingRepository` del constructor.
- **DELEGAR** los endpoints `getExternalTaskTopics()` (L320-323) y `getDataMappings()` (L328-331) y `createDataMapping()` (L333-345) a `BpmnDesignService` que a su vez usará los Puertos.
- **ELIMINAR** la línea `new DataMappingEntity(...)` (L336-342) del Controller.

## 5. Matriz de QA y Testing

| Test Name | Hallazgo | Aserción Esperada |
|-----------|----------|-------------------|
| `PreFlightAnalyzerService_NoImportaCamunda` | ARQ-005-02 | `grep "org.camunda" PreFlightAnalyzerService.java` → 0 resultados |
| `BpmnDesignController_NoImportaRepos` | ARQ-005-01 | `grep "Repository" BpmnDesignController.java` → 0 resultados (excepto en imports del Service) |
| `BpmnDesignService_NoImportaEntities` | ARQ-005-03 | `grep "infrastructure.jpa.entity" BpmnDesignService.java` → 0 resultados |
| `CamundaBpmnValidationAdapter_ExisteComo_Component` | ARQ-005-02 | El adaptador tiene `@Component` y vive en `infrastructure/adapters/` |
| `DeployEndpoint_ReturnsCreated` | CA-1 | `POST /deploy` con .bpmn válido → HTTP 201 con campos del contrato (CA-65) |

## 6. Mensaje de Despacho

> Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
