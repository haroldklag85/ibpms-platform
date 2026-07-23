# 🧠→🕵️ Handoff: Arquitecto Líder → QA - TESTER
# US-004: Certificación E2E Playwright de Triaje Humano

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA - TESTER
**Fecha:** 2026-05-25T13:20:00-05:00
**Sprint:** 7 — Iteración 1
**Prioridad:** 🔴 Alta
**Dependencia:** Esperar a que el Backend y Frontend hayan integrado la rama de US-004.

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
cat docs/architecture/ADR-010-Zero-Mock.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

La pantalla de Triaje Humano ha sido implementada pero carece de pruebas E2E robustas bajo el modelo Zero-Mock V2, lo cual impide la certificación final de la US-004 en los pipelines de Playwright.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Cobertura 0% | `frontend/e2e/us004-triage.spec.ts` | No existe la suite de pruebas E2E para interceptar las validaciones de triaje. |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Implementar Suite Playwright Zero-Mock

**Archivo:** `frontend/e2e/us004-triage.spec.ts`

Desarrollar el script de Playwright interceptando las rutas de red (Zero-Mock) para aislar la UI.

```typescript
import { test, expect } from '@playwright/test';
// @Traceability: US-004, CA-9

test.describe('US-004: Human Triage Validation', () => {
    test('Aprobación de elemento de Triaje interceptando red', async ({ page }) => {
        // Mock de datos iniciales
        await page.route('**/intake/triage', route => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify([{ id: '1', subject: 'Correo Sospechoso' }])
            });
        });

        // Intercept action
        await page.route('**/intake/triage/1/process', async route => {
            const request = route.request();
            expect(request.method()).toBe('POST');
            const postData = JSON.parse(request.postData() || '{}');
            expect(postData.action).toBe('APPROVE');
            
            await route.fulfill({ status: 200, body: JSON.stringify({ success: true }) });
        });

        await page.goto('/intake/triage');
        
        const approveBtn = page.locator('button', { hasText: 'Aprobar' });
        await expect(approveBtn).toBeVisible();
        await approveBtn.click();
    });
});
```

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Test Playwright Implementado | `us004-triage.spec.ts` existe. |
| 2 | Mocks Estáticos eliminados | Todo mock está encapsulado en `page.route` (Zero-Mock V2). |
| 3 | Trazabilidad inyectada | `@Traceability` en el archivo de prueba. |
| 4 | Test Exitoso | Ejecutar `npx playwright test us004-triage.spec.ts` retorna PASS. |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crear el script en la ruta indicada.
2. Ejecutar prueba: `npx playwright test us004-triage.spec.ts`
3. Commit: `git add . && git commit -m "test(e2e): US-004 triage view playwright tests" && git push`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🕵️ QA - TESTER.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat .agents/skills/zero_mock_enforcement/SKILL.md
4. cat C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\brain\70f5a9fc-0715-4dbd-b999-23a6c6833584\artifacts\handoff_qa_US004.md

TU MISIÓN:

1. Implementar la prueba Playwright para asegurar que los botones "Aprobar" emiten el evento correcto a la red interceptada.
2. Ejecutar la prueba: npx playwright test us004-triage.spec.ts
3. Commit: git add . && git commit -m "test(e2e): US-004 triage view playwright tests" && git push

REGLAS INQUEBRANTABLES:
- DEBES inyectar @Traceability en el spec.
- PROHIBIDO conectar directamente a la BD (usar page.route para aislar frontend).
```
