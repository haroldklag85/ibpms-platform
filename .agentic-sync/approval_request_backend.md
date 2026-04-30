# 🛡️ Solicitud de Revisión Backend: Remediación ARQ-005

**Para:** Arquitecto Líder
**De:** Agente Backend
**Iteración:** Remediación Arquitectónica ARQ-005

## Resumen del Plan de Implementación

He elaborado el plan para eliminar la deuda técnica que vincula directamente a `BpmnDesignController`, `PreFlightAnalyzerService` y `BpmnDesignService` con repositorios JPA y con la API embebida de Camunda.

### Acciones Estratégicas:
1. **Creación de Puertos (application/port/out/):**
   - `BpmnDesignPort`, `BpmnAuditPort`, `ProcessLockPort`, `DeployRequestPort`, `ExternalTaskTopicPort`, `DataMappingPort`.
   - `BpmnValidationPort`: Un puerto crucial para recibir el stream BPMN y devolver un `DeploymentValidationResponse`, abstrayendo a Camunda.
   
2. **Creación de Adaptadores (infrastructure/adapter/):**
   - Adaptadores JPA para persistencia (`BpmnDesignJpaAdapter`, `BpmnAuditJpaAdapter`, etc.)
   - `CamundaBpmnValidationAdapter`: Contendrá toda la lógica del SDK org.camunda.*, cumpliendo el principio del ADR-003.
   
3. **Refactorización de Servicios de Aplicación:**
   - **`PreFlightAnalyzerService`**: Será limpiado de 15+ imports de `org.camunda.bpm.model.bpmn.*` y usará el `BpmnValidationPort`.
   - **`BpmnDesignService`**: Será limpiado de todas las dependencias `infrastructure.jpa.entity.*` y utilizará exclusivamente los nuevos puertos.
   
4. **Limpieza del Controller:**
   - **`BpmnDesignController`**: Se eliminará la inyección de repositorios JPA y el manejo directo de entidades (`DataMappingEntity`). Los métodos pertinentes serán delegados a `BpmnDesignService`.

Solicito autorización (✅ PASS o ❌ REJECT) para iniciar la fase de EXECUTION en TDD según este plan.
