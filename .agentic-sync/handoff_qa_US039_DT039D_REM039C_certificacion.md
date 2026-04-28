# Handoff QA — US-039 | Certificación de Cierre DT-039-02 + REM-039-C

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Cierre Deuda Técnica — Iteración 3 |
| **Sprint** | 6 |
| **Rama Git** | `sprint-6` |
| **User Story** | US-039 — Formulario Genérico Base |
| **Deudas a Certificar** | DT-039-02 (Caffeine Cache) + REM-039-C (Draft Banner Test) |
| **SSOT** | `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_B_formularios_bpmn.md` |
| **Flujo de Trabajo** | Backend (DT-039-02) → Frontend (REM-039-C) → **QA (certificación)** → Arquitecto (veredicto) |
| **Prerequisitos** | QA ejecuta DESPUÉS de que Backend y Frontend completen sus handoffs |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Aplicables
- **ADR-010 (Testing Pyramid):** Este handoff requiere validación en Nivel 2 (Component/Integration). Vitest para Frontend, Spring Boot Test para Backend.
- **Ley de Correspondencia Gherkin:** Cada deuda técnica cerrada debe tener al menos 1 test que la certifique.

> Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.

---

## 3. Matriz de Certificación

### 3.1 Certificar DT-039-02 (Caffeine Cache — Backend)

| Checkpoint | Método de Verificación | Resultado Esperado |
|------------|----------------------|-------------------|
| **QA-DT039D-01:** Dependencia Caffeine en classpath | Ejecutar `mvn dependency:tree` y buscar `caffeine` | Línea: `com.github.ben-manes.caffeine:caffeine:jar:X.X.X` presente |
| **QA-DT039D-02:** Configuración en application.yml | Leer `backend/ibpms-core/src/main/resources/application.yml` | Sección `spring.cache.type: caffeine` + `spec: maximumSize=100,expireAfterWrite=5m` presente |
| **QA-DT039D-03:** @EnableCaching activo | Buscar `@EnableCaching` en `/src/main/java/` | Al menos 1 clase `@Configuration` con `@EnableCaching` |
| **QA-DT039D-04:** Compilación exitosa | Ejecutar protocolo SRE Backend | `BUILD SUCCESS` sin errores |
| **QA-DT039D-05:** Contexto Spring arranca | Ejecutar `mvn spring-boot:run` (o test de contexto) | Sin excepciones `BeanCreationException` ni `CacheManager` missing |

**Comandos de verificación sugeridos:**
```bash
# Verificar dependencia
cd backend/ibpms-core && mvn dependency:tree -Dincludes=caffeine

# Verificar que @Cacheable sigue funcionando
grep -rn "@Cacheable" src/main/java/ --include="*.java"

# Verificar @EnableCaching
grep -rn "@EnableCaching" src/main/java/ --include="*.java"
```

### 3.2 Certificar REM-039-C (Draft Banner Test — Frontend)

| Checkpoint | Método de Verificación | Resultado Esperado |
|------------|----------------------|-------------------|
| **QA-REM039C-01:** Test file existe | `ls frontend/src/tests/views/admin/GenericForm/GenericFormView.spec.ts` | Archivo presente |
| **QA-REM039C-02:** Tests pasan | `npx vitest run src/tests/views/admin/GenericForm/GenericFormView.spec.ts` | 3/3 tests PASS |
| **QA-REM039C-03:** Nombres de test siguen convención | Inspeccionar archivo de test | Tests nombrados `QA-039-C-01`, `QA-039-C-02`, `QA-039-C-03` |
| **QA-REM039C-04:** Suite existente no se rompe | `npx vitest run src/tests/components/forms/generic/DraftRestorationBanner.spec.ts` | 3/3 tests previos siguen PASS |
| **QA-REM039C-05:** Build completo | `npm run build` | Sin errores TS |

**Comandos de verificación sugeridos:**
```bash
cd frontend

# Test nuevo
npx vitest run src/tests/views/admin/GenericForm/GenericFormView.spec.ts

# Test anterior (regresión)
npx vitest run src/tests/components/forms/generic/DraftRestorationBanner.spec.ts

# Suite completa de formularios genéricos
npx vitest run src/tests/components/forms/generic/
```

---

## 4. Reporte de Certificación (Template)

Al finalizar la verificación, el agente QA debe emitir un reporte con el siguiente formato:

```markdown
## Reporte de Certificación QA — US-039 (DT-039-02 + REM-039-C)

| ID | Checkpoint | Estado | Evidencia |
|----|-----------|--------|-----------|
| QA-DT039D-01 | Caffeine en classpath | ✅/❌ | [output] |
| QA-DT039D-02 | Config application.yml | ✅/❌ | [líneas] |
| QA-DT039D-03 | @EnableCaching | ✅/❌ | [clase] |
| QA-DT039D-04 | Compilación | ✅/❌ | BUILD SUCCESS |
| QA-DT039D-05 | Contexto Spring | ✅/❌ | [log] |
| QA-REM039C-01 | Test file existe | ✅/❌ | [path] |
| QA-REM039C-02 | 3/3 tests PASS | ✅/❌ | [output vitest] |
| QA-REM039C-03 | Convención nombres | ✅/❌ | [inspection] |
| QA-REM039C-04 | Regresión suite previa | ✅/❌ | [output vitest] |
| QA-REM039C-05 | Build completo | ✅/❌ | npm run build |

**Veredicto:** PASS / FAIL
**Fecha:** YYYY-MM-DD
```

---

## 5. Mensaje de Despacho

> **Instrucciones para el Agente QA:**
>
> Lee este documento completo. Tu tarea es **certificar** el trabajo ejecutado por los agentes Backend y Frontend. No debes escribir código productivo.
>
> 1. Espera a que Backend y Frontend confirmen que completaron sus handoffs.
> 2. Ejecuta los checkpoints de la sección 3 en orden.
> 3. Rellena el template de la sección 4 con los resultados reales.
> 4. Si algún checkpoint falla, documenta el error y devuelve al equipo correspondiente con instrucciones de remediación.
>
> **Rama:** `sprint-6`. PROHIBIDO trabajar en `main`.
