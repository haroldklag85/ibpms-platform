# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: us039-draft-recovery.e2e.spec.ts >> US-039: Draft Recovery Experience (UX) >> QA-039-12: Amber banner prompts for recovery of unsubmitted generic form
- Location: e2e\certification\us039-draft-recovery.e2e.spec.ts:6:3

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('.p-message-warn, [data-testid="draft-recovery-banner"]')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "soft toBeVisible" with timeout 5000ms
  - waiting for locator('.p-message-warn, [data-testid="draft-recovery-banner"]')

```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | import { USERS } from '../fixtures/e2e-data';
  3  | 
  4  | test.describe('US-039: Draft Recovery Experience (UX)', () => {
  5  | 
  6  |   test('QA-039-12: Amber banner prompts for recovery of unsubmitted generic form', async ({ page, context }) => {
  7  |     // 1. Iniciar sesión estándar
  8  |     await page.goto('/login');
  9  |     await page.click('[data-testid="break-glass-toggle"]');
  10 |     await page.fill('[data-testid="email-input"]', USERS.ANALISTA_N1.email);
  11 |     await page.fill('[data-testid="password-input"]', USERS.ANALISTA_N1.password);
  12 |     await page.click('[data-testid="login-submit"]');
  13 |     
  14 |     // Inyectar un draft artificial directamente en localStorage
  15 |     const fakeTaskId = '11111111-2222-3333-4444-555555555555';
  16 |     await page.evaluate(({ taskId }) => {
  17 |       localStorage.setItem(`draft_sys_generic_form_${taskId}`, JSON.stringify({
  18 |         obs: 'Borrador E2E',
  19 |         _timestamp: new Date().toISOString()
  20 |       }));
  21 |     }, { taskId: fakeTaskId });
  22 | 
  23 |     // 2. Simular entrada a un formulario asociado a esa tarea
  24 |     // Since we don't have a live task in Camunda guaranteed, we test if the component loads it
  25 |     // Mocks / route handling should happen here if frontend is isolated, or we navigate directly
  26 |     await page.goto(`/workdek/form/sys_generic_form?taskId=${fakeTaskId}`);
  27 | 
  28 |     // Wait and observe the banner
  29 |     // Should see Amber banner asking to restore
  30 |     const draftBanner = page.locator('.p-message-warn, [data-testid="draft-recovery-banner"]');
  31 |     
  32 |     // Si la ruta no existe, la vista tal vez redirija. Solo es aserción si la página se sostiene.
  33 |     // Usamos soft expect por si falla el enrutamiento.
> 34 |     await expect.soft(draftBanner).toBeVisible({ timeout: 5000 });
     |                                    ^ Error: expect(locator).toBeVisible() failed
  35 |   });
  36 | 
  37 | });
  38 | 
```