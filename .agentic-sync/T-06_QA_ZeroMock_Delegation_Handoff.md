# 🧠→🕵️ Handoff: Arquitecto Líder → Agente QA
# T-06: Certificación E2E Zero-Mock de Delegación Múltiple (CA-04)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA - PLAYWRIGHT
**Fecha:** 2026-05-12T19:00:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🟡 Media
**Dependencia:** T-20 (Infraestructura de Playwright base)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3, 4)
cat .cursorrules

# 2. Skill principal del agente receptor (QA Playwright)
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales aplicables (Zero-Mock)
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes (Testing Pyramid)
cat docs/architecture/adr_010_testing_pyramid_governance.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación `@Traceability` o comentario `// @Traceability: US-001, CA-04`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Tras la auditoría forense de la tarea T-06 (Selector múltiple de delegantes), se ha encontrado que el Frontend cumple con la lógica (CA-04 de US-001), pero existe una **Deuda Técnica QA** crítica.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Ausencia de Certificación Zero-Mock | `frontend/e2e/workdesk-delegation.spec.ts` (Archivo Faltante) | No existe prueba E2E que certifique que al seleccionar un delegado en `Workdesk.vue`, se emita la petición HTTP real al backend inyectando el query param `delegatedUserId`. Viola Ley Global 2. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Crear el Spec E2E de Playwright (CA-04)

**Archivo:** `frontend/e2e/workdesk-delegation.spec.ts`

Crea o actualiza este archivo para interceptar la petición de red y validar la inyección del parámetro `delegatedUserId`.

```typescript
import { test, expect } from '@playwright/test';

// @Traceability: US-001, CA-04
test.describe('Workdesk - Delegación Múltiple (Zero-Mock)', () => {
  
  test('El selector de delegados inyecta delegatedUserId en el request de red', async ({ page }) => {
    // 1. Mock inicial si se requiere, pero enfocado en Zero-Mock (Network Intercept)
    // Escuchamos la petición de la grilla
    const requestPromise = page.waitForRequest(request => 
      request.url().includes('/workdesk/global-inbox') && 
      request.url().includes('delegatedUserId=')
    );

    // 2. Login y navegación a Workdesk
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin');
    await page.click('button:has-text("Ingresar")');
    await page.waitForURL('**/workdesk');

    // 3. Seleccionar "Delegar Bandeja" (Modo DELEGATED)
    const selectLocator = page.locator('select'); // Ajusta el locator según data-testid si existe
    await selectLocator.waitFor({ state: 'visible' });
    
    // Suponiendo que hay asistentes en la lista (se deben pre-cargar por BD o intercept de /delegatedAssistants)
    // Seleccionamos por índice 1 o por un valor conocido
    await selectLocator.selectOption({ index: 1 });

    // 4. Validar la captura de la red
    const request = await requestPromise;
    const url = new URL(request.url());
    const delegatedUserId = url.searchParams.get('delegatedUserId');
    
    // Aserción de Gobernanza
    expect(delegatedUserId).toBeTruthy();
    expect(delegatedUserId?.length).toBeGreaterThan(0);
  });
});
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Cobertura Playwright Implementada | El archivo `workdesk-delegation.spec.ts` existe. |
| 2 | Verificación de Intercepción de Red | El test pasa y captura un URL conteniendo `delegatedUserId=`. |
| 3 | Trazabilidad Inversa (Ley 3) | Inspeccionar el archivo asegurando que el marcador `// @Traceability: US-001, CA-04` exista. |
| 4 | Ejecución Exitosa | Comando `npx playwright test e2e/workdesk-delegation.spec.ts` reporta `1 passed`. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crear el archivo `frontend/e2e/workdesk-delegation.spec.ts`.
2. Escribir el test asegurando interceptar la red.
3. Ejecutar la suite: `cd frontend && npx playwright test e2e/workdesk-delegation.spec.ts`
4. Commit: `git add . && git commit -m "test(workdesk): T-06 certificar selector de delegantes en entorno Zero-Mock (CA-04)" && git push origin HEAD`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 🕵️ QA - PLAYWRIGHT.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat docs/architecture/adr_010_testing_pyramid_governance.md
4. cat .agentic-sync/T-06_QA_ZeroMock_Delegation_Handoff.md

TU MISIÓN:

1. Crea el test E2E en `frontend/e2e/workdesk-delegation.spec.ts` siguiendo el snippet proporcionado en la Sección 4.
2. Asegúrate de ajustar los locators al HTML real de `Workdesk.vue` (si tiene un id o clase específica).
3. Build/Compile: `cd frontend && npx playwright test e2e/workdesk-delegation.spec.ts`
4. Commit: `git add . && git commit -m "test(workdesk): T-06 certificar delegación CA-04" && git push origin HEAD`

REGLAS INQUEBRANTABLES:
- DEBES añadir `// @Traceability: US-001, CA-04` a cada componente de test (LEY GLOBAL 3).
- La aserción DEBE estar basada en la captura de red, no solo en cambios de UI (Ley Global 2, Zero-Mock).
- Si el test falla, arréglalo antes del commit.
```
