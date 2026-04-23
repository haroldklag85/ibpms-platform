# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: smoke-j04-operario.e2e.spec.ts >> Smoke J-04: Operario MVP — Happy Path >> CU-J04-01→06: Login → Workdesk → Claim → Form → Submit → Desaparición
- Location: e2e\certification\smoke-j04-operario.e2e.spec.ts:6:3

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('[data-testid="task-list"] [data-testid^="task-row-"]').first()
Expected: visible
Timeout: 30000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 30000ms
  - waiting for locator('[data-testid="task-list"] [data-testid^="task-row-"]').first()

```

# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - generic [ref=e2]:
    - generic [ref=e3]:
      - complementary [ref=e4]:
        - generic [ref=e5]:
          - generic [ref=e8] [cursor=pointer]: balance
          - button "chevron_right" [ref=e9] [cursor=pointer]:
            - generic [ref=e10]: chevron_right
        - navigation [ref=e11]:
          - generic "Sin Topología de Menús" [ref=e13]: security_update_warning
        - generic [ref=e14]:
          - button "logout Cerrar Sesión" [ref=e15] [cursor=pointer]:
            - generic [ref=e16]: logout
            - generic: Cerrar Sesión
          - generic [ref=e18]: US
      - main [ref=e19]:
        - generic [ref=e20]:
          - generic [ref=e21]:
            - link "business_center iBPMS" [ref=e22] [cursor=pointer]:
              - /url: /
              - generic [ref=e23]: business_center
              - generic [ref=e24]: iBPMS
            - generic [ref=e25]: /
            - generic [ref=e26]: Bandeja Unificada
          - generic [ref=e27]:
            - generic [ref=e28]:
              - generic [ref=e30]: search
              - textbox "Buscar expedientes..." [ref=e31]
            - generic [ref=e33]:
              - button "compress" [ref=e34] [cursor=pointer]:
                - generic [ref=e35]: compress
              - button "view_agenda" [ref=e36] [cursor=pointer]:
                - generic [ref=e37]: view_agenda
              - button "expand" [ref=e38] [cursor=pointer]:
                - generic [ref=e39]: expand
            - button "notifications" [ref=e40] [cursor=pointer]:
              - generic [ref=e41]: notifications
            - button "help" [ref=e43] [cursor=pointer]:
              - generic [ref=e44]: help
        - generic [ref=e46]:
          - generic [ref=e47]:
            - generic [ref=e48]:
              - generic [ref=e49]:
                - generic [ref=e50]: balance
                - heading "Bandeja Unificada Workdesk" [level=1] [ref=e51]
              - generic [ref=e52]:
                - generic [ref=e53]:
                  - button "📋 Mis Tareas" [ref=e54] [cursor=pointer]
                  - button "👤 Tareas de mi Asistente" [ref=e55] [cursor=pointer]
                - combobox [ref=e56] [cursor=pointer]:
                  - option "Todos los Tipos" [selected]
                  - option "Procesos (BPMN)"
                  - option "Proyectos (Kanban)"
                - combobox [ref=e57] [cursor=pointer]:
                  - option "Cualquier Nivel SLA" [selected]
                  - option "Vencido"
                  - option "Urgente"
                  - option "Normal"
            - generic [ref=e58]:
              - generic [ref=e59]:
                - generic [ref=e60]: search
                - searchbox "Buscar por ID, título o asignado..." [ref=e61]
              - button "sync" [ref=e62] [cursor=pointer]:
                - generic [ref=e63]: sync
          - main [ref=e64]:
            - generic [ref=e65]:
              - generic [ref=e66]:
                - generic [ref=e67]:
                  - button "dock_to_right" [ref=e68] [cursor=pointer]:
                    - generic [ref=e69]: dock_to_right
                  - generic [ref=e70]:
                    - generic [ref=e71]: filter_alt
                    - text: "Mostrando:"
                    - generic [ref=e72]: "0"
                    - text: resultados locales
                - generic [ref=e73]: "Total Global: 0"
              - generic [ref=e75]:
                - generic [ref=e77]: celebration
                - heading "🎉 ¡Bandeja Vacía!" [level=3] [ref=e78]
                - paragraph [ref=e79]: Has resuelto todas tus tareas pendientes. Excelente desempeño operativo.
                - paragraph [ref=e80]: "Última sincronización: 12:54:11 AM"
            - complementary [ref=e81]:
              - generic [ref=e82]:
                - generic [ref=e83]:
                  - heading "Resumen Operativo" [level=2] [ref=e84]
                  - generic [ref=e85]:
                    - generic [ref=e86]:
                      - generic [ref=e88]: "0"
                      - generic [ref=e89]:
                        - paragraph [ref=e90]: Total Tareas
                        - paragraph [ref=e91]: Bandeja Activa
                    - generic [ref=e92]:
                      - generic [ref=e94]: "0"
                      - generic [ref=e95]:
                        - paragraph [ref=e96]: Vencidas
                        - paragraph [ref=e97]: Crítico - SLA Cumplido
                    - generic [ref=e98]:
                      - generic [ref=e100]: "0"
                      - generic [ref=e101]:
                        - paragraph [ref=e102]: Por Expirar
                        - paragraph [ref=e103]: < 24 Horas
                - generic [ref=e105]:
                  - generic [ref=e106]:
                    - generic [ref=e107]: public
                    - paragraph [ref=e108]: CQRS Engine
                  - paragraph [ref=e109]:
                    - text: "Sync Eventual:"
                    - generic "Conexión Rechazada o STOMP Caído" [ref=e110]: OFFLINE
    - button "power_settings_new" [ref=e112] [cursor=pointer]:
      - generic [ref=e113]: power_settings_new
  - generic [ref=e114]:
    - generic [ref=e115]: warning
    - 'heading "ALERTA DEL SISTEMA: NIVEL 0" [level=1] [ref=e116]'
    - paragraph [ref=e117]: Colapso del Servidor / Integración Cíclica
    - paragraph [ref=e118]: "Código de Error: 500"
    - button "refresh REINICIAR CONTEXTO" [ref=e119] [cursor=pointer]:
      - generic [ref=e120]: refresh
      - text: REINICIAR CONTEXTO
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | import { USERS } from '../fixtures/e2e-data';
  3  | 
  4  | test.describe('Smoke J-04: Operario MVP — Happy Path', () => {
  5  |   
  6  |   test('CU-J04-01→06: Login → Workdesk → Claim → Form → Submit → Desaparición', async ({ page }) => {
  7  |     // Timeout extendido para backend real
  8  |     test.setTimeout(90000);
  9  |     
  10 |     // 2. Operario inicia sesión
  11 |     await page.goto('/login');
  12 |     await page.click('[data-testid="break-glass-toggle"]');
  13 |     await page.fill('[data-testid="email-input"]', USERS.ANALISTA_N1.email);
  14 |     await page.fill('[data-testid="password-input"]', USERS.ANALISTA_N1.password);
  15 |     await page.click('[data-testid="login-submit"]');
  16 |     
  17 |     // 2. Navegar a Workdesk
  18 |     await page.waitForURL(/workdesk/);
  19 |     
  20 |     // 3. Verificar lista de tareas cargada (no vacía)
  21 |     const taskList = page.locator('[data-testid="task-list"] [data-testid^="task-row-"]');
> 22 |     await expect(taskList.first()).toBeVisible({ timeout: 30000 });
     |                                    ^ Error: expect(locator).toBeVisible() failed
  23 |     
  24 |     // 4. Reclamar primera tarea disponible
  25 |     const firstTask = taskList.first();
  26 |     const claimButton = firstTask.locator('[data-testid="claim-button"]');
  27 |     await claimButton.click();
  28 |     
  29 |     // 5. Esperar confirmación de claim (toast o estado visual)
  30 |     await expect(page.locator('.p-toast-message-success, [data-testid="claim-success"]')).toBeVisible({ timeout: 15000 });
  31 |     
  32 |     // 6. Abrir formulario de la tarea reclamada
  33 |     await firstTask.click();
  34 |     await expect(page.locator('[data-testid="form-container"]')).toBeVisible({ timeout: 15000 });
  35 |     
  36 |     // 7. Llenar campos obligatorios (genéricos)
  37 |     const requiredInputs = page.locator('input[required], textarea[required], select[required]');
  38 |     const count = await requiredInputs.count();
  39 |     for (let i = 0; i < count; i++) {
  40 |       const input = requiredInputs.nth(i);
  41 |       const tagName = await input.evaluate(el => el.tagName.toLowerCase());
  42 |       if (tagName === 'select') {
  43 |         await input.selectOption({ index: 1 });
  44 |       } else {
  45 |         await input.fill('Valor de prueba E2E');
  46 |       }
  47 |     }
  48 |     
  49 |     // 8. Enviar formulario
  50 |     await page.click('[data-testid="form-submit"]');
  51 |     
  52 |     // 9. Verificar toast de éxito
  53 |     await expect(page.locator('.p-toast-message-success')).toBeVisible({ timeout: 15000 });
  54 |     
  55 |     // 10. Verificar desaparición del Workdesk (RYOW)
  56 |     await page.goto('/workdesk');
  57 |     // La tarea reclamada y completada NO debe aparecer
  58 |   });
  59 | });
  60 | 
```