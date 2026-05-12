# 🧠→🧪 Handoff: Arquitecto Líder → QA - Playwright
# T-07: E2E Certification CQRS & Event Sourcing (US-017)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🧪 QA - PLAYWRIGHT
**Fecha:** 2026-05-12T09:30:00-05:00
**Sprint:** 7 — Sprint 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** Backend y Frontend deben estar desarrollados.

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/adr_010_testing_governance.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación `// @Traceability: US-XXX, CA-XX`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Al introducir CQRS, el envío del formulario `/complete` se comporta diferente, respondiendo un `eventReference`. Necesitamos un test E2E que asegure que el usuario final llega al éxito, y otro para validar el Auto-Claim (bloqueo implicito).

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Ausencia de test E2E de CQRS | `e2e/` | No hay archivo validando el envío CQRS ni el Auto-Claim CA-04. |
| Ausencia de test de Toast | `e2e/` | Falta certificar el comportamiento offline del Toast CA-22. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Certificación CQRS Happy Path y Auto-Claim

**Archivo:** `ibpms-platform/e2e/certification/us017-cqrs-event-sourcing.spec.ts`

```typescript
// @Traceability: US-017, CA-01, CA-04
import { test, expect } from '@playwright/test';

test.describe('US-017: CQRS and Event Sourcing', () => {
  test('CU-01: Auto-Claim and Submit Form', async ({ page }) => {
    await page.goto('/workdesk');
    // ... setup
    // 1. Entrar a tarea de grupo (Unassigned)
    // 2. Dar click a Completar
    // 3. Validar response con `eventReference` de la API real.
    const responsePromise = page.waitForResponse('/api/v1/workbox/tasks/*/complete');
    await page.getByRole('button', { name: 'Completar' }).click();
    
    const response = await responsePromise;
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.eventReference).toBeDefined();
  });
});
```

### Paso 2: Certificación del Toast Offline

**Archivo:** `ibpms-platform/e2e/certification/us017-cqrs-toast.spec.ts`

```typescript
// @Traceability: US-017, CA-22, CA-25
test('CU-02: Offline Toast appears when context goes offline', async ({ context, page }) => {
  await page.goto('/workdesk');
  // Simular red desconectada
  await context.setOffline(true);
  
  // Validar Toast inferior izquierdo
  const toast = page.locator('text=Trabajando sin conexión');
  await expect(toast).toBeVisible();
  
  // Restablecer red
  await context.setOffline(false);
  // Validar si es necesario la resincronización...
});
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Test Happy Path Pasa | `npx playwright test us017-cqrs-event-sourcing.spec.ts` -> PASS. |
| 2 | Test Toast Pasa | `npx playwright test us017-cqrs-toast.spec.ts` -> PASS. |
| 3 | Trazabilidad Inversa | `grep -r "@Traceability: US-017" e2e/certification/` -> N resultados. |
| 4 | Cero Mocks Backend | No debe usarse `page.route` para mockear respuestas 200 de la API. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crea el archivo `us017-cqrs-event-sourcing.spec.ts`
2. Crea el archivo `us017-cqrs-toast.spec.ts`
3. Ejecuta los tests: `cd ibpms-platform/e2e && npx playwright test us017`
4. Commit: `git add . && git commit -m "test(e2e): add certification suite for CQRS US-017" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 🧪 QA - PLAYWRIGHT.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat ibpms-platform/.cursorrules
2. cat ibpms-platform/.agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
4. cat ibpms-platform/docs/architecture/adr_010_testing_governance.md
5. cat ibpms-platform/.agentic-sync/handoff_qa_T07_CQRS_US017.md

TU MISIÓN:

1. Implementa el E2E `us017-cqrs-event-sourcing.spec.ts` garantizando aserciones sobre el JSON de respuesta real (`eventReference`).
2. Implementa el E2E `us017-cqrs-toast.spec.ts` utilizando la simulación offline nativa de Playwright.
3. Build/Compile: `cd ibpms-platform/e2e && npx playwright test` (o similar si hay scripts específicos).
4. Commit: `git add . && git commit -m "test(e2e): create test suite for US-017 CQRS and UX toasts" && git push`

REGLAS INQUEBRANTABLES:
- DEBES incluir comentarios `// @Traceability: US-017, CA-XX` en cada bloque `test()` creado.
- PROHIBIDO utilizar APIs falsas/mocks para la validación del Backend. Tienes que enviar a la Base de Datos real del entorno E2E.
```
