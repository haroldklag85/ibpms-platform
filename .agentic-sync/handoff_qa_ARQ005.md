# 🕵️ Handoff QA — Certificación ARQ-005 (US-005 Core Deploy Pipeline)

## 1. Metadatos y SSOT
- **Iteración:** Remediación Arquitectónica Post-Auditoría US-005
- **Rama Git:** `sprint-6`
- **SSOT:** `docs/requirements/epics/epic_B_formularios_bpmn.md` → US-005 (CA-1 a CA-14)
- **Hallazgos Origen:** `audit_arquitectura_US005.md` → ARQ-005-01, ARQ-005-02, ARQ-005-03
- **Orden de Ejecución:** Infra/BD → Backend → **QA** → Frontend (informativo)
- **Prerequisito:** El agente Backend DEBE haber completado su refactor antes de esta certificación.

## 2. Alineación Arquitectónica

| ADR | Checkpoint QA |
|-----|--------------|
| `adr-001-hexagonal-architecture.md` | Verificar que NO existen imports `infrastructure.jpa.entity` en capa `application/service/`. Verificar que NO existen imports `Repository` en capa `infrastructure/web/` (Controllers). |
| `adr-003-camunda7-embedded.md` | Verificar que NO existen imports `org.camunda` en capa `application/service/` (excepto en DTOs de resultado si aplica). |
| `adr_010_testing_pyramid_governance.md` | Verificar que BUILD SUCCESS se mantiene tras el refactor. |

## 3. Matriz de Certificación (7 Checkpoints)

| ID | Checkpoint | Comando/Verificación | Resultado Esperado |
|----|-----------|---------------------|-------------------|
| QA-005-01 | **Hexagonal Controller** | `grep -rn "Repository" backend/.../infrastructure/web/BpmnDesignController.java` | **SIN RESULTADOS** — El Controller no debe inyectar repos. |
| QA-005-02 | **Hexagonal Controller (Entidades)** | `grep -rn "infrastructure.jpa.entity" backend/.../infrastructure/web/BpmnDesignController.java` | **SIN RESULTADOS** — El Controller no debe crear entidades. |
| QA-005-03 | **Hexagonal PreFlight (Camunda)** | `grep -rn "org.camunda" backend/.../application/service/PreFlightAnalyzerService.java` | **SIN RESULTADOS** — Camunda API debe estar en un Adapter. |
| QA-005-04 | **Hexagonal PreFlight (JPA)** | `grep -rn "infrastructure.jpa" backend/.../application/service/PreFlightAnalyzerService.java` | **SIN RESULTADOS** — Entidades/repos JPA deben estar en Adapters. |
| QA-005-05 | **Hexagonal BpmnDesignService** | `grep -rn "infrastructure.jpa.entity" backend/.../application/service/BpmnDesignService.java` | **SIN RESULTADOS** — Service debe usar Puertos. |
| QA-005-06 | **Adapter Exists** | `find backend/ -name "CamundaBpmnValidationAdapter.java" -o -name "BpmnDesignJpaAdapter.java"` | **Al menos 2 archivos encontrados** en `infrastructure/adapters/`. |
| QA-005-07 | **BUILD SUCCESS** | `mvn clean test -pl ibpms-core` ejecutado desde `backend/` | **BUILD SUCCESS** — Sin regresiones. |

## 4. Protocolo de Ejecución

1. Ejecuta `git pull origin sprint-6` para obtener los cambios del Backend.
2. Ejecuta los 7 checkpoints secuencialmente.
3. Deposita el reporte en `.agentic-sync/qa_report_ARQ005.md` con la tabla de resultados.
4. Si algún checkpoint FALLA, el veredicto es **FAIL** y debes reportar al Arquitecto para corrección.

## 5. Mensaje de Despacho

> Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin. Todo checkpoint sin evidencia debe reportarse como Cobertura Faltante.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md`.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Debes guardar tu solicitud de revisión en `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder."*
5. Espera el veredicto. Si aprobado, ejecuta los 7 checkpoints, deposita el reporte, y haz `git commit` + `git push` en `sprint-6`.

> 📚 **SKILLS OBLIGATORIOS:**
> - Aplica `.agents/skills/tdd_first/SKILL.md` y `.agents/skills/clean_code_standards/SKILL.md`.
