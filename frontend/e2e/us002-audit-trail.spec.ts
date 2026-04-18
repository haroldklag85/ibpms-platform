import { test, expect } from '@playwright/test';

test.describe('US-002 CA-9: Audit Trail Timeline', () => {
  test('Visualización cronológica de eventos de reclamación y liberación', async ({ page }) => {
    await page.route('**/api/v1/workdesk/tasks/task-AT/preview', route => {
        route.fulfill({ status: 200, body: JSON.stringify({ unifiedId: 'task-AT', status: 'ACTIVE' }) });
    });

    await page.route('**/api/v1/workdesk/tasks/task-AT/audit', route => {
      route.fulfill({
          status: 200,
          body: JSON.stringify([
            { id: 1, action: 'CLAIM', actor: 'userX', timestamp: '2026-04-18T10:00:00Z' },
            { id: 2, action: 'UNCLAIM', actor: 'userX', timestamp: '2026-04-18T11:00:00Z', reason: 'Abandono' },
            { id: 3, action: 'CLAIM', actor: 'userY', timestamp: '2026-04-18T12:00:00Z' },
            { id: 4, action: 'FORCE_UNCLAIM', actor: 'boss', timestamp: '2026-04-18T13:00:00Z', reason: 'Vencimiento SLA' }
          ])
      });
    });

    await page.goto('/workdesk?previewTask=task-AT');

    const timeline = page.locator('.timeline');
    await timeline.waitFor({ state: 'visible' });

    await expect(page.getByText('userX')).toHaveCount(2);
    await expect(page.getByText('boss')).toBeVisible();
    await expect(page.getByText('Vencimiento SLA')).toBeVisible();
  });
});
