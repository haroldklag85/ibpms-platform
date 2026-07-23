# Handoff QA — ARQ-028-04 | Certificación de Cohesión Mixta

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica y Refactorización (sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Cerrar** | **ARQ-028-04:** Entidad con cohesión mixta |
| **Dependencia** | Ejecutar **DESPUÉS** de que Backend notifique éxito y haga push. |

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto en QA |
|-----|---------------|
| `adr-001-hexagonal-architecture.md` | Verificar que `FormCertificationEntity` vive en `infrastructure/jpa/entity/` y que `FormCertificationService` NO importa directamente la entidad. |
| `adr_010_testing_pyramid_governance.md` | Los tests de integración deben cubrir el flujo de certificación end-to-end usando la nueva tabla. |

---

## 3. Rutas Exactas y Contexto Preexistente

| Archivo | Verificación |
|---------|-------------|
| `backend/.../infrastructure/jpa/entity/FormDefinitionEntity.java` | Grep para confirmar que **NO** contiene `isQaCertified`. |
| `backend/.../infrastructure/jpa/entity/FormCertificationEntity.java` | Confirmar existencia con anotación `@Table(name = "ibpms_form_certifications")`. |
| `backend/.../resources/db/changelog/35-arq02804-split-certification.sql` | Confirmar existencia del changeset Liquibase. |
| `backend/.../application/service/FormCertificationService.java` | Verificar que NO importa `FormDefinitionEntity` directamente (hexagonal). |

---

## 4. Matriz de QA — Puntos de Certificación

| ID | Checkpoint | Método de Verificación | Resultado Esperado |
|----|-----------|----------------------|-------------------|
| **QA-02804-01** | Liquibase Script | `ls backend/.../db/changelog/35-arq02804-split-certification.sql` | Archivo presente |
| **QA-02804-02** | FormDefinition Limpio | `grep "isQaCertified" backend/.../entity/FormDefinitionEntity.java` | **SIN RESULTADOS** (exit code 1) |
| **QA-02804-03** | Nueva Entidad Creada | `grep "ibpms_form_certifications" backend/.../entity/FormCertificationEntity.java` | **MATCH ENCONTRADO** |
| **QA-02804-04** | Repository Creado | `grep "FormCertificationRepository" backend/.../repository/` | Archivo presente con `findByFormDefinitionId` |
| **QA-02804-05** | API Contract Intacto | Ejecutar endpoint de certificación y verificar que la respuesta JSON mantiene los campos `isQaCertified`, `certifiedBy`, etc. | **Retrocompatibilidad 100%** |
| **QA-02804-06** | Violación Hexagonal | `grep "FormDefinitionEntity" backend/.../application/service/FormCertificationService.java` | **SIN RESULTADOS** |
| **QA-02804-07** | Compilación y Tests | `cd backend/ibpms-core && mvn clean test` | **BUILD SUCCESS** |

> 📚 Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.

---

## 5. Reporte de Certificación (Template)

```markdown
## Reporte de Certificación QA — ARQ-028-04

| ID | Checkpoint | Estado | Evidencia |
|----|-----------|--------|-----------|
| QA-02804-01 | Script BD | ✅/❌ | |
| QA-02804-02 | Entidad Limpia | ✅/❌ | |
| QA-02804-03 | Nueva Entidad | ✅/❌ | |
| QA-02804-04 | Repository | ✅/❌ | |
| QA-02804-05 | API Contract | ✅/❌ | |
| QA-02804-06 | Hexagonal | ✅/❌ | |
| QA-02804-07 | Tests | ✅/❌ | |

**Veredicto:** PASS / FAIL
```

---

## 6. Instrucciones Operativas y de Comunicación

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6`. Queda estrictamente prohibido usar git stash.
