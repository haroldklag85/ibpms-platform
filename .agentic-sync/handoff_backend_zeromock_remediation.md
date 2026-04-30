# 🏗️ Handoff Consolidado BACKEND — Remediación Zero-Mock (Endpoint de Migración + Validaciones)

## 1. Metadatos y SSOT
- **Iteración:** 90-DEV-sprint6
- **Rama Git:** `sprint-6/uat-certification`
- **Objetivo:** Crear los endpoints de migración de instancias de proceso que el Frontend necesita para eliminar el mock de `InstancesManager.vue`.
- **SSOT:** `docs/architecture/zero_mock_violation_report.md` + auditoría forense del Arquitecto Líder
- **Flujo:** 1️⃣ Backend primero (Frontend espera estos endpoints para completar VIOL-004)

## 2. Alineación Arquitectónica y ADRs
- **ADR-001 (Arquitectura Hexagonal):** Los endpoints deben respetar la separación Puerto/Adaptador. El Controller es Infraestructura, el UseCase es Dominio.
- **Motor BPMN:** El backend ya integra Camunda (ver `BpmnDesignController.java`). Los endpoints de migración deben usar la API de Camunda directamente.

---

## 3. Endpoints a Crear

### 3.1. `GET /api/v1/design/processes/{processId}/instances`

**Propósito:** Listar instancias de proceso activas en Camunda para una definición de proceso específica. El Frontend las renderiza en el `InstancesManager.vue` (Gestor de Instancias - Cirugía Quirúrgica).

**Contrato de Respuesta:**
```json
[
  {
    "id": "10f9-a1b2-inst-001",
    "version": "V1",
    "currentNode": "Task_AprobarVentas",
    "isMigratable": true
  },
  {
    "id": "39a1-b6e5-inst-003",
    "version": "V1",
    "currentNode": "Task_LegacyVerification",
    "isMigratable": false
  }
]
```

**Lógica de Negocio:**
- Consultar las `ProcessInstances` activas en Camunda filtrando por `processDefinitionKey = processId`.
- Para cada instancia, obtener el nodo actual (activity ID del token).
- `isMigratable` = `true` si el nodo actual existe en la última versión del proceso desplegada.
- **CAs:** CA-8 (migración selectiva), CA-9 (validación topológica)

**Ubicación sugerida:**
- Controller: `infrastructure/web/design/BpmnInstanceController.java` [NUEVO]
- UseCase: `application/usecase/design/ListProcessInstancesUseCase.java` [NUEVO]

---

### 3.2. `POST /api/v1/design/processes/{processId}/migrate`

**Propósito:** Migrar instancias seleccionadas de una versión anterior a la versión activa del proceso.

**Contrato de Request:**
```json
{
  "instanceIds": ["10f9-a1b2-inst-001", "20c4-f8d9-inst-002"],
  "isSandbox": false
}
```

**Reglas de Negocio:**
- Si `isSandbox = true`, el backend DEBE limitar el máximo a 5 instancias (CA-67).
- Ejecutar la API de Camunda `RuntimeService.createProcessInstanceModification()` o `MigrationPlan` para mover tokens.
- **Zero Data-Patching (CA-10):** La migración solo mueve tokens, NUNCA modifica variables de proceso.
- Retornar resumen con cantidad de instancias migradas exitosamente y las fallidas.

**Contrato de Respuesta:**
```json
{
  "migrated": 2,
  "failed": 0,
  "details": [
    { "instanceId": "10f9-a1b2-inst-001", "status": "MIGRATED" },
    { "instanceId": "20c4-f8d9-inst-002", "status": "MIGRATED" }
  ]
}
```

**Ubicación sugerida:**
- Controller: `infrastructure/web/design/BpmnInstanceController.java` (mismo archivo)
- UseCase: `application/usecase/design/MigrateProcessInstancesUseCase.java` [NUEVO]

---

### 3.3. Validación del Endpoint `GET /api/v1/admin/roles/export-matrix`

**Contexto:** El Frontend tiene un mock CSV en `IdentityGovernance.vue` L815-823 que genera `MOCK_CISO_Access_Matrix.csv`. Según el análisis, el backend YA tiene el endpoint en `RoleAdminController.java` L78 (`roleService.exportRoleMatrixToCsv()`).

**Acción Requerida:**
- **Validar** que el endpoint retorna `Content-Type: text/csv` con los headers adecuados (`Content-Disposition: attachment; filename="Access_Matrix_CISO.csv"`).
- **Validar** que el CSV contenga columnas reales: `PROCESS, ROLE, INITIATE, EXECUTE, APPROVE, VIEW`.
- Si el endpoint ya funciona correctamente, **no se necesita trabajo adicional** — solo confirmarlo.

---

## 4. INSTRUCCIONES OPERATIVAS

1. Inicia en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md`.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera el veredicto. Si aprobado, pasa a `EXECUTION`.

📚 **SKILLS OBLIGATORIOS:**
- Aplica TDD (Red-Green-Refactor): `.agents/skills/tdd_first/SKILL.md`
- Aplica Hexagonal: `.agents/skills/hexagonal_enforcement/SKILL.md`

> **Build obligatorio:** `mvn clean test` debe pasar en verde. Ejecuta `mvn verify` antes de hacer push.

> **Commit y Push** obligatorio en `sprint-6/uat-certification`. Queda prohibido usar `git stash`.
