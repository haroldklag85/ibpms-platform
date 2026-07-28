# 🧠→👥 Handoff: Lead Architect → Teamwork Preview
# US-005-VT: Process Version Tag Homologation and Timeline Log Correction

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 👥 TEAMWORK - DEVELOPERS TEAM
**Fecha:** 2026-06-10T04:06:00-05:00
**Sprint:** 6 — Iteración 3
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skills obligatorios asignados para esta tarea
cat .agents/skills/addyosmani_planning/SKILL.md
cat .agents/skills/addyosmani_sre_discipline/SKILL.md
cat .agents/skills/addyosmani_code_review/SKILL.md
cat .agents/skills/yudhi_architecture_compliance/SKILL.md
cat .agents/skills/yudhi_database_migrations/SKILL.md
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 3. ADRs relevantes
cat docs/architecture/adr-001-hexagonal-architecture.md
cat docs/architecture/adr_010_testing_pyramid_governance.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación @Traceability o comentario `// @Traceability: US-005, CA-15`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Durante la revisión del versionamiento en el diseñador BPMN de la US-005, se han identificado las siguientes discrepancias:

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Coerción visual de versión de borrador (v0) a v1 | [BpmnDesigner.vue:1190](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue#L1190) | La expresión `log.version \|\| 1` descarta el valor `0` (evaluado como falsy) e impone visualmente `v1` para borradores. |
| Coerción lógica en restauración de versión borrador (v0) | [BpmnDesigner.vue:1204](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue#L1204) | `@click="restoreVersionFromLog(log.version \|\| 1)"` restaura incorrectamente la versión `1` en lugar de la versión `0`. |
| Ausencia de Auto-Sugerencia de Version Tag | [BpmnDesigner.vue:3100](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue#L3100) | Al iniciar o cargar un proceso nuevo (`currentVersion == 0`) sin tag, la UI no sugiere el valor por defecto `"1.0.0"`. |
| Falta de Validación de Formato SemVer de Version Tag en Backend | [CamundaBpmnValidationAdapter.java](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter/CamundaBpmnValidationAdapter.java) | El Pre-Flight Analyzer no valida que `camunda:versionTag` esté definido y cumpla con la convención de versión semántica (SemVer). |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Corrección de Timeline de Auditoría (Frontend)
**Archivo:** [BpmnDesigner.vue](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue)

- En el renderizado del timeline de logs (Línea 1190):
```diff
- v{{ log.version || 1 }}
+ v{{ log.version !== undefined && log.version !== null ? log.version : 1 }}
```
- En el evento `@click` del botón de restauración (Línea 1204):
```diff
- @click="restoreVersionFromLog(log.version || 1)"
+ @click="restoreVersionFromLog(log.version !== undefined && log.version !== null ? log.version : 1)"
```

### Paso 2: Auto-Sugerencia de Version Tag en import.done (Frontend)
**Archivo:** [BpmnDesigner.vue](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue)

- Modificar la sección del listener `import.done` para auto-sugerir `"1.0.0"` si el proceso es nuevo y carece de tag:
```typescript
            // @Traceability: US-005, CA-15
            const versionTagAttr = bo.get('camunda:versionTag');
            processVersionTag.value = versionTagAttr || '';
            
            if (!processVersionTag.value && currentVersion.value === 0) {
                // Auto-suggest 1.0.0 for new processes/drafts
                processVersionTag.value = '1.0.0';
                updateVersionTag();
            }
```

### Paso 3: Pruebas Unitarias de Frontend (TDD - Fase Roja a Verde)
**Archivo:** [BpmnDesigner.spec.ts](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts)

- Inyectar pruebas para validar que:
  1. Si `version` es `0`, el timeline badge renderiza `"v0"`.
  2. Si `currentVersion` es `0` y no hay tag, se asigna `"1.0.0"` por defecto.
- Ejecutar y corroborar que fallen inicialmente, y pasen a verde tras la implementación del Paso 1 y 2.

### Paso 4: Validación de Version Tag en Pre-Flight (Backend)
**Archivo:** [CamundaBpmnValidationAdapter.java](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter/CamundaBpmnValidationAdapter.java)

- En `validateBpmnStream` y `validateDraftXml`:
  - Obtener el elemento `<bpmn:process>` y leer el atributo `camunda:versionTag` (o `versionTag`).
  - Validar que no sea nulo ni vacío, y que coincida con el regex SemVer: `^[0-9]+\.[0-9]+\.[0-9]+.*$`.
  - Si no coincide, añadir un error indicando la violación:
    - En `validateBpmnStream`: `response.addError("Process", "La etiqueta de versión '" + versionTag + "' es inválida. Debe cumplir con el formato SemVer (Ej: 1.0.0)");`
    - En `validateDraftXml`: `result.addIssue(PreFlightResultDTO.Severity.ERROR, "INVALID_VERSION_TAG", ...);`

### Paso 5: Pruebas Unitarias del Pre-Flight (TDD - Fase Roja a Verde)
**Archivo:** [BpmnVersionTagValidationTest.java](file:///z:/home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/adapter/BpmnVersionTagValidationTest.java)

- Crear un test JUnit 5 `BpmnVersionTagValidationTest` para el adaptador.
- Casos de prueba:
  - XML con `camunda:versionTag="1.0.0"` -> Pasa la validación.
  - XML con `camunda:versionTag="2.3.1-SNAPSHOT"` -> Pasa la validación.
  - XML con `camunda:versionTag="v1.0"` -> Falla (formato inválido).
  - XML sin `camunda:versionTag` -> Falla (ausente/vacío).

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | El Timeline Log en la UI renderiza `v0` si la versión del log es `0` | Inspección visual / Aserción del test de vitest |
| 2 | El Version Tag en la UI auto-sugiere `1.0.0` para nuevos procesos borradores | Inspección visual / Aserción del test de vitest |
| 3 | El Pre-Flight Analyzer rechaza XMLs con Version Tags inválidos o ausentes | JUnit tests `BpmnVersionTagValidationTest` PASS |
| 4 | Suite de pruebas de Vitest pasa con éxito en WSL | `wsl npm run test -- --run` -> Todos en verde |
| 5 | Compilación exitosa en Backend y Frontend | `mvn test "-Djacoco.skip=true"` PASS y `wsl npm run build` exitoso |
| 6 | Integridad Git sin Stash | `git status` limpio en rama `sprint-6` tras commit final |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crear las pruebas unitarias fallantes (Paso 3 y Paso 5).
2. Ejecutar las pruebas en rojo y verificar los fallos.
3. Implementar correcciones del frontend (Paso 1 y Paso 2).
4. Implementar validación en backend (Paso 4).
5. Correr las pruebas unitarias y verificar el paso a verde.
6. Ejecutar build del frontend: `wsl npm run build`.
7. Ejecutar compilación del backend: `mvn clean compile`.
8. Git: `git add . && git commit -m "feat(us-005): implement version tag homologation and timeline fallback correction" && git push origin sprint-6`.

---

## 📋 Instrucciones para Copiar y Pegar (Prompt para Teamwork Subagents)

```
Asume el rol de 👥 TEAMWORK - DEVELOPERS TEAM.

Tu misión es resolver la homologación del version tag y corregir el timeline de auditoría de la US-005.

ANTES DE EMPEZAR, lee obligatoriamente los siguientes archivos en este orden exacto:
1. cat .cursorrules
2. cat .agents/skills/addyosmani_planning/SKILL.md
3. cat .agents/skills/addyosmani_sre_discipline/SKILL.md
4. cat .agents/skills/addyosmani_code_review/SKILL.md
5. cat .agents/skills/yudhi_architecture_compliance/SKILL.md
6. cat .agents/skills/yudhi_database_migrations/SKILL.md
7. cat .agents/skills/clean_code_standards/SKILL.md
8. cat .agents/skills/zero_mock_enforcement/SKILL.md
9. cat docs/architecture/adr-001-hexagonal-architecture.md
10. cat docs/architecture/adr_010_testing_pyramid_governance.md
11. cat .agentic-sync/handoff_US005_version_tag_homologation.md

INSTRUCCIONES CLAVE:
1. Iniciar con pruebas fallando (TDD Fase Roja) en frontend y backend:
   - Añadir tests en `BpmnDesigner.spec.ts` para verificar render de 'v0' y auto-sugerencia de '1.0.0'.
   - Crear `BpmnVersionTagValidationTest.java` en backend para validar tags correctos e incorrectos.
2. Ejecutar y confirmar que fallan.
3. Implementar el fix en frontend (`BpmnDesigner.vue` timeline rendering y callback auto-sugerencia `1.0.0`).
4. Implementar validación SemVer en backend (`CamundaBpmnValidationAdapter.java` para `validateBpmnStream` y `validateDraftXml`).
5. Confirmar paso a verde de todos los tests.
6. Realizar build del frontend (`wsl npm run build`) y mvn compile backend para validar cero roturas.
7. Confirmar éxito agregando marcas de trazabilidad '// @Traceability: US-005, CA-15'.
8. Hacer commit convencional: `git add . && git commit -m "feat(us-005): version tag homologation and timeline log fix" && git push origin sprint-6`.
```
