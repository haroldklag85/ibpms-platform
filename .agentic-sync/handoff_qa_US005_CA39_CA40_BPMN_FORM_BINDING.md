# 🧠→🧪 Handoff: ARQUITECTO LÍDER → QA - E2E
# sprint-01-DevDavid-BPMN: Certificación E2E de Vinculación Formulario-BPMN (US-005 CA-39/CA-40)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🧪 QA - E2E
**Fecha:** 2026-06-22T15:24:00-05:00
**Sprint:** sprint-01-DevDavid-BPMN
**Prioridad:** 🔴 Alta — Requerimiento urgente del cliente
**Dependencia:** ✅ Frontend DEBE haber completado y pusheado `handoff_frontend_US005_CA39_CA40_BPMN_FORM_BINDING.md`
**Rama de trabajo:** `DevDavid`

---

## 📖 LECTURAS OBLIGATORIAS

```bash
# 1. Arquitectura Core
cat docs/architecture/arquitecturar.md

# 2. Skill principal QA
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADR de testing
cat docs/architecture/adr_010_testing_pyramid_governance.md

# 5. SSOT
cat docs/requirements/epics/epic_B_formularios_bpmn.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** Todos los tests DEBEN llevar
> `// @Traceability: US-005, CA-39, CA-40`.

---

## 🔬 Diagnóstico del Arquitecto

Se ha corregido el endpoint de catálogo de formularios (Backend) y el dropdown FormKey del BpmnDesigner (Frontend). El QA debe certificar que el flujo E2E funciona:

1. Crear un formulario simple en el Form Designer
2. Abrir el BPMN Designer y ver ese formulario en el dropdown de FormKey
3. Vincular el formulario a una UserTask
4. Desplegar el proceso BPMN
5. Ejecutar el proceso y verificar que la tarea muestra el formulario correcto

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Crear test E2E con Playwright

**Archivo:** `frontend/e2e/certification/us005-bpmn-form-binding.e2e.spec.ts`

```typescript
// @Traceability: US-005, CA-39, CA-40
import { test, expect } from '@playwright/test'

test.describe('US-005 CA-39/CA-40: BPMN Form Binding', () => {
  
  test('CA-39: El dropdown FormKey muestra formularios activos reales', async ({ page }) => {
    // 1. Navegar al BPMN Designer
    await page.goto('/admin/modeler/bpmn')
    
    // 2. Crear o seleccionar una UserTask
    // 3. Verificar que el dropdown de FormKey tiene opciones reales (no vacío)
    // 4. Verificar que NO hay formularios mock ("Aprobación Rápida", etc.)
  })
  
  test('CA-40: El dropdown filtra por patrón Simple vs Maestro', async ({ page }) => {
    // 1. Navegar al BPMN Designer con un proceso tipo SIMPLE
    // 2. Verificar que solo se muestran formularios con patrón SIMPLE
  })
  
  test('E2E: Flujo completo crear form → vincular a BPMN → ver en dropdown', async ({ page }) => {
    // 1. Crear un formulario simple en /admin/modeler/forms/designer
    // 2. Guardar el formulario
    // 3. Navegar al BPMN Designer
    // 4. Crear UserTask
    // 5. Verificar que el formulario recién creado aparece en el dropdown
    // 6. Seleccionar el formulario
    // 7. Verificar que camunda:formKey se escribió en el XML
  })
})
```

### Paso 2: Verificación manual con evidencia

Capturar screenshots de:
1. El Form Designer con un formulario simple guardado
2. El BPMN Designer mostrando el dropdown con formularios reales
3. El XML BPMN exportado mostrando `camunda:formKey`

### Paso 3: Verificar endpoint Backend

```bash
curl -s http://localhost:8080/api/v1/forms/active | python -m json.tool
# Debe retornar lista no vacía de formularios

curl -s "http://localhost:8080/api/v1/forms/active?pattern=SIMPLE" | python -m json.tool
# Debe retornar solo formularios con pattern SIMPLE
```

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Test E2E `us005-bpmn-form-binding.e2e.spec.ts` pasa al 100% | `npx playwright test us005-bpmn-form-binding` → 3/3 PASSED |
| 2 | El endpoint retorna formularios reales (no vacío) | curl output con al menos 1 formulario |
| 3 | NO existen datos mock en el código del Frontend | `grep -rn "mock\|fallback.*form" frontend/src/views/admin/Modeler/BpmnDesigner.vue` → 0 |
| 4 | Screenshots de evidencia adjuntos | Archivos en `frontend/e2e/results/` |
| 5 | Commit en rama DevDavid con tests pasando | `git log -1` muestra el commit |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. `git pull origin DevDavid` para obtener cambios de Backend y Frontend
2. Verificar backend corriendo: `curl -s http://localhost:8080/actuator/health`
3. Verificar frontend corriendo: `curl -s http://localhost:5173`
4. Escribir test E2E en `frontend/e2e/certification/us005-bpmn-form-binding.e2e.spec.ts`
5. Ejecutar tests: `npx playwright test us005-bpmn-form-binding`
6. Capturar screenshots de evidencia
7. Verificar endpoint con curl
8. Actualizar `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`:
   > "Se certificó mediante pruebas automatizadas que los formularios creados por el usuario se pueden vincular correctamente a los flujos de trabajo BPMN."
9. `git add . && git commit -m "test(e2e): certificar vinculación formulario-BPMN US-005 CA-39/CA-40" && git push origin DevDavid`

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo:
> 1. `curl -s http://localhost:8080/actuator/health` → `{"status":"UP"}`
> 2. Si no responde: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`
> 3. Docker: `docker ps` → PostgreSQL, Redis, RabbitMQ `Up (healthy)`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 🧪 Agente QA E2E.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente:

1. cat docs/architecture/arquitecturar.md
2. cat .agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat .agents/skills/zero_mock_enforcement/SKILL.md
4. cat docs/architecture/adr_010_testing_pyramid_governance.md
5. cat .agentic-sync/handoff_qa_US005_CA39_CA40_BPMN_FORM_BINDING.md

TU MISIÓN:

1. Crear test E2E Playwright para certificar vinculación form-BPMN (CA-39/CA-40)
2. Ejecutar tests y capturar evidencia
3. Verificar endpoint Backend con curl
4. Bitácora: CHANGELOG_NO_TECNICO.md
5. Commit: git add . && git commit -m "test(e2e): certificar vinculación formulario-BPMN US-005 CA-39/CA-40" && git push origin DevDavid

REGLAS INQUEBRANTABLES:
- PROHIBIDO declarar "PASS" sin ejecutar los tests empíricamente.
- PROHIBIDO crear mocks o stubs. Tests contra el stack real (Backend + BD real).
- Aplica la Ley de Correspondencia Gherkin del skill qa_e2e_validation_audit.
- Todo test DEBE tener // @Traceability: US-005, CA-39, CA-40
- Es OBLIGATORIO actualizar el CHANGELOG_NO_TECNICO.md antes del commit final.
```

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia en modo `PLANNING`. Elabora plan en `implementation_plan.md`.
2. **PROHIBIDO pedirle al Humano que apruebe.** Guarda solicitud en `.agentic-sync/approval_request_QA_US005_CA39.md`.
3. Dile al Humano: *"He dejado mi solicitud en `.agentic-sync/approval_request_QA_US005_CA39.md`. Llévala al Arquitecto Líder."*
4. Espera el veredicto. Si aprobado, ejecuta.
5. Actualiza CHANGELOG_NO_TECNICO.md. Commit + push a `DevDavid`.
