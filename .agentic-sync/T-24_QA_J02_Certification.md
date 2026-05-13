# 🧠→🕵️ Handoff: Arquitecto → QA E2E
# T-24-QA: Certificación E2E Zero-Mock V2 (J-02 BPMN/DMN)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA E2E
**Fecha:** 2026-05-13T17:55:00-05:00
**Sprint:** 7 — Iteración 7.2
**Prioridad:** 🔴 Alta
**Dependencia:** T-24-INFRA (Scripts semilla) y T-24-FRONTEND (Testability)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales) - ¡Especial atención a LEY GLOBAL 4!
cat .cursorrules

# 2. Skill principal del agente receptor
cat ibpms-platform/.agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales aplicables
cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md

# 4. ADRs relevantes
cat ibpms-platform/docs/architecture/adr_010_testing_pyramid_governance.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación `@Traceability` o comentario `// @Traceability: Certificación E2E J-02 (T-24)`. Esto es INNEGOCIABLE.
> ⚠️ **LEY GLOBAL 4 — Inmutabilidad de Regresión:** Modificar un test antiguo simplemente para que vuelva a pasar está ESTRICTAMENTE PROHIBIDO. Las aserciones son inmutables. Se arregla el código, NUNCA el test.

---

## 🔬 Diagnóstico del Arquitecto

La arquitectura ha sido purgada de Mocks (ADR-010). J-02 (BPMN/DMN) interactúa nativamente con JPA y REST. Se requiere una auditoría absoluta para conocer el estado real del proyecto frente a una base de datos vacía.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Ausencia de Certificación | `frontend/e2e/certification/` | J-02 (BPMN/DMN) carece de una suite de regresión validada contra infraestructura real (Zero-Mock). |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Certificación Funcional Integral J-02

**Archivo:** `frontend/e2e/certification/us005-bpmn-modeler-persistence.e2e.spec.ts` (y archivos DMN correspondientes)

Debes crear la matriz de pruebas E2E en Playwright garantizando cobertura total (Happy Path y Excepciones RBAC):

```typescript
// Snippet prescriptivo — Estructura base requerida
import { test, expect } from '@playwright/test';

// @Traceability: Certificación E2E J-02 (T-24)
test.describe('J-02: Zero-Mock BPMN/DMN Suite', () => {
  test('US-005 BPMN: Debe persistir un borrador exitosamente', async ({ page }) => {
    // 1. Navegar y renderizar canvas
    // 2. Agregar StartEvent, UserTask
    // 3. Trigger Save Draft (interceptar llamada real a backend, no mockear respuesta)
    // 4. Validar HTTP 200 OK
  });

  test('US-007 DMN: Debe denegar edición sin rol sysadmin (Anti-Spoofing)', async ({ page }) => {
    // 1. Iniciar sesión con analista base
    // 2. Intentar guardar Grid DMN
    // 3. Validar interceptación de HTTP 403 Forbidden y Modal de UI
  });
});
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Ejecución Exitosa E2E J-02 | Salida de `npx playwright test` mostrando tests de BPMN/DMN en verde. |
| 2 | Validación RBAC (Anti-Spoofing) Incluida | Aserción explícita de rechazo HTTP 403 para usuarios sin permisos en DMN. |
| 3 | Trazabilidad Inyectada | `grep -r "@Traceability" frontend/e2e/certification/` arroja resultados en los scripts nuevos. |
| 4 | Cumplimiento Ley Global 4 | Diff demuestra que no se modificaron aserciones antiguas para forzar pases. |
| 5 | Build & Commit | Ejecución de commit limpio a la rama `sprint-7`. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. `npx playwright test --project=chromium` para verificar estado base.
2. Implementar/Actualizar scripts en `frontend/e2e/certification/`.
3. `npx playwright test --project=chromium` para validar scripts.
4. Commit: `git add . && git commit -m "test(e2e): matriz Zero-Mock J-02" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🕵️ QA E2E.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat ibpms-platform/.cursorrules
2. cat ibpms-platform/.agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
4. cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md
5. cat ibpms-platform/docs/architecture/adr_010_testing_pyramid_governance.md
6. cat ibpms-platform/.agentic-sync/T-24_QA_J02_Certification.md

TU MISIÓN:

1. Ejecuta y diseña las pruebas E2E para J-02 asegurando evaluar los flujos de éxito y las validaciones estrictas de RBAC (Anti-Spoofing) sobre base de datos.
2. Build/Compile: `npx playwright test --project=chromium`
3. Commit: `git add . && git commit -m "test(e2e): matriz Zero-Mock J-02" && git push`

REGLAS INQUEBRANTABLES:
- DEBES OBLIGATORIAMENTE cumplir la LEY GLOBAL 4 de .cursorrules (Inmutabilidad de Regresión).
- PROHIBIDO el uso de aserciones contra Pinia Mocks o mockAdapter de red.
- OBLIGATORIO inyectar `// @Traceability: Certificación E2E J-02 (T-24)` en todos los tests.
```
