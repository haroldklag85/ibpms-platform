# [⚙️ BACKEND - JAVA] Solicitud de Aprobación — US-007 Ejecución BPMN

> **Rol**: Backend Java 17 / Spring Boot
> **Fecha**: 2026-06-07
> **Sprint**: PM-01, Slot 3
> **Rama destino**: `sprint-8/pm-01/us-007-030-bpmn`

---

## 1. CONTEXTO Y ANÁLISIS RAG

### 1.1 Discrepancia Detectada (Sin Bloqueo)

> [!IMPORTANT]
> La **US-007 en el SSOT** (`docs/requirements/epics/epic_B_formularios_bpmn.md` L1927) es "Generador Cognitivo de DMN (NLP a Tablas de Decisión)" con 30/32 CAs cubiertos.
> Sin embargo, el **handoff** (`handoff_backend_US007.md`) ordena implementar "Ejecución BPMN" — endpoints para iniciar procesos y completar tareas de usuario.
>
> **Resolución**: El handoff fue emitido por el Arquitecto Líder y define un scope que complementa la capa de ejecución del motor BPMN. Los CAs no pertenecen a US-007 del SSOT sino al alcance operativo genérico de Ejecución BPMN que vertebra a US-001, US-002, US-005 y US-029. Procedo según la directiva del handoff pero dejo constancia para trazabilidad.

### 1.2 Estado Actual del Codebase (RAG Profundo)

| Artefacto | Estado | Observación |
|---|---|---|
| `ProcesoBpmPort` (Puerto de Salida) | ✅ Existe | Ya define `iniciarProceso()`, `completarTarea()`, `reclamarTarea()` |
| `CamundaBpmAdapter` (Adaptador) | ✅ Existe | Implementa `ProcesoBpmPort` con `RuntimeService`, `TaskService` |
| `CompletarTareaUseCase` (Puerto de Entrada) | ✅ Existe | Interface + Service (`CompletarTareaService`) con SoD, Idempotencia |
| `TaskController` (REST) | ✅ Existe | Expone `POST /tasks/{taskId}/complete` via `CompletarTareaUseCase` |
| `AnonymousProcessController` | ✅ Existe | `POST /api/v1/process/{key}/start-anonymous` (solo anónimo) |
| **StartProcessUseCase** (Puerto Entrada) | ❌ NO EXISTE | Falta el Use Case hexagonal para inicio autenticado |
| **Endpoint `POST /api/bpmn/instances`** | ❌ NO EXISTE | El contrato está en `API_CONTRACTS.md` (L568) pero no implementado |
| **BpmnExecutionController** (REST) | ❌ NO EXISTE | No hay controlador dedicado a ejecución BPMN autenticada |

**Conclusión**: La capa de "Completar Tarea" ya existe E2E, pero la capa de "Iniciar Proceso Autenticado" falta por completo como Use Case hexagonal. Solo existe el bypass anónimo (`AnonymousProcessController`) que NO cumple la gobernanza (no usa puertos).

---

## 2. PLAN DE IMPLEMENTACIÓN

### FASE 1: Puerto de Entrada — `StartProcessUseCase`

#### [NEW] `StartProcessUseCase.java`
- **Paquete**: `com.ibpms.poc.application.port.in`
- **Contrato**:
  ```java
  public interface StartProcessUseCase {
      StartProcessResult start(String processDefinitionKey, String businessKey,
                                Map<String, Object> variables, String initiatorUsername);
  }
  ```
- **Justificación**: Aplica Hexagonal Architecture (ADR-001). El controlador REST invoca el puerto de entrada, nunca el RuntimeService directamente.

#### [NEW] `StartProcessResult.java`
- **Paquete**: `com.ibpms.poc.application.dto`
- **Tipo**: `record` inmutable (Clean Code §2)
- **Campos**: `processInstanceId`, `processDefinitionKey`, `businessKey`, `startedAt`, `startedBy`

---

### FASE 2: Servicio de Aplicación — `StartProcessService`

#### [NEW] `StartProcessService.java`
- **Paquete**: `com.ibpms.poc.application.service`
- **Implementa**: `StartProcessUseCase`
- **Dependencias (inyección por constructor)**:
  - `ProcesoBpmPort` (para delegar `iniciarProceso()` al motor Camunda)
  - `RepositoryService` (para validar que el `processDefinitionKey` existe antes de ejecutar)
- **Lógica**:
  1. Validar que `processDefinitionKey` no sea nulo/vacío.
  2. Verificar existencia de la definición de proceso en Camunda (`RepositoryService.createProcessDefinitionQuery().processDefinitionKey(key).latestVersion().singleResult()`).
  3. Si no existe → lanzar `ProcessDefinitionNotFoundException` (excepción de dominio nueva).
  4. Inyectar variable de trazabilidad: `ibpms_initiator_id` = `initiatorUsername`.
  5. Delegar a `ProcesoBpmPort.iniciarProceso()`.
  6. Construir y retornar `StartProcessResult`.

---

### FASE 3: Excepción de Dominio

#### [NEW] `ProcessDefinitionNotFoundException.java`
- **Paquete**: `com.ibpms.poc.domain.exception`
- **Extiende**: `RuntimeException`
- **Alineación**: Clean Code §2 — Excepciones concretas, nunca genéricas.

---

### FASE 4: Controlador REST — `BpmnExecutionController`

#### [NEW] `BpmnExecutionController.java`
- **Paquete**: `com.ibpms.poc.infrastructure.web.bpm`
- **Mapping base**: `/api/bpmn`
- **Endpoints**:

| Método | Path | Descripción | Contrato API_CONTRACTS.md |
|---|---|---|---|
| `POST` | `/api/bpmn/instances` | Iniciar instancia de proceso BPMN autenticada | L568 ✅ |
| `POST` | `/api/bpmn/tasks/{taskId}/complete` | Completar una UserTask (delega a `CompletarTareaUseCase` existente) | Nuevo |

**Endpoint 1: Iniciar Proceso**
- Request Body: `StartProcessRequest` (record con `processDefinitionKey`, `businessKey`, `variables`)
- Response: `201 Created` con `StartProcessResult`
- Seguridad: Extrae `username` de `SecurityContextHolder`
- Headers: `Idempotency-Key` (opcional, para prevenir doble inicio)

**Endpoint 2: Completar Tarea**
- Delega directamente al `CompletarTareaUseCase` existente (no duplica lógica).
- Request Body: Mapa de variables.
- Response: `204 No Content`
- Headers: `Idempotency-Key`, `If-Match` (optimistic locking)

> [!NOTE]
> El `TaskController` existente en `/tasks/{taskId}/complete` seguirá funcionando como ruta legacy. El nuevo endpoint `/api/bpmn/tasks/{taskId}/complete` actúa como alias normalizado bajo el namespace BPMN para coherencia con el contrato `API_CONTRACTS.md`.

#### [NEW] `StartProcessRequest.java`
- **Paquete**: `com.ibpms.poc.application.dto`
- **Tipo**: `record`
- **Validación Jakarta**: `@NotBlank processDefinitionKey`

---

### FASE 5: Manejo Global de Excepciones

#### [MODIFY] Verificar si `ProcessDefinitionNotFoundException` ya es capturada por un `@ControllerAdvice` existente
- Si existe `GlobalExceptionHandler`: agregar handler que retorne `404 Not Found` con el formato estándar de error.
- Si no existe: crear uno mínimo.

---

### FASE 6: Documentación y Trazabilidad

#### [MODIFY] `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`
- Agregar entrada de registro para la implementación de endpoints de Ejecución BPMN.

---

## 3. ARCHIVOS A CREAR/MODIFICAR

| Acción | Archivo | Propósito |
|---|---|---|
| `[NEW]` | `application/port/in/StartProcessUseCase.java` | Puerto de entrada |
| `[NEW]` | `application/dto/StartProcessResult.java` | DTO de respuesta (record) |
| `[NEW]` | `application/dto/StartProcessRequest.java` | DTO de request (record + validación) |
| `[NEW]` | `application/service/StartProcessService.java` | Implementación del Use Case |
| `[NEW]` | `domain/exception/ProcessDefinitionNotFoundException.java` | Excepción de dominio |
| `[NEW]` | `infrastructure/web/bpm/BpmnExecutionController.java` | Controlador REST |
| `[MODIFY]` | `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` | Registro no técnico |

**Total**: 6 archivos nuevos, 1 archivo modificado.

---

## 4. VERIFICACIÓN SRE (POST-IMPLEMENTACIÓN)

1. **Compilación nativa**: `mvn spring-boot:run` → Validar `Tomcat started on port 8080`.
2. **Curl de Humo**:
   - `curl -X POST http://localhost:8080/api/bpmn/instances -H "Content-Type: application/json" -d '{"processDefinitionKey":"...", "variables":{}}'`
3. **Zero Mocks**: Integración directa contra `RuntimeService` y `TaskService` de Camunda 7 embebido. Sin mocks, sin stubs, sin interceptores.

---

## 5. RIESGOS Y MITIGACIONES

| Riesgo | Mitigación |
|---|---|
| No hay definición de proceso desplegada en el motor para probar | El `E2EDataSeedConfig.java` ya despliega procesos BPMN al arranque en perfil `dev` |
| Spring Security bloquea el endpoint nuevo | Verificar `SecurityConfig` para permitir acceso a `/api/bpmn/**` con Bearer JWT |
| Colisión de beans entre `CamundaEngineAdapter` y `CamundaBpmAdapter` | Ambos existen e implementan puertos distintos (`ProcessEnginePort` vs `ProcesoBpmPort`). No hay colisión. |

---

## 6. ALINEACIÓN ARQUITECTÓNICA

- ✅ **ADR-001 (Hexagonal)**: Use Case → Port → Adapter. El Controller nunca toca `RuntimeService`.
- ✅ **ADR-003 (Camunda Embedded)**: Usa la API Java nativa de Camunda 7, no REST.
- ✅ **Zero Mocks**: Integración directa contra el motor embebido.
- ✅ **Clean Code**: Records para DTOs, inyección por constructor, excepciones concretas, `@Slf4j`.
