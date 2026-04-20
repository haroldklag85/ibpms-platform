import { test, expect } from '@playwright/test';
import { USERS, API } from '../fixtures/e2e-data';

test.describe('P0: IDOR BpmnCopilotController — Cross-Tenant Session Wipe', () => {
  
  test('CU-JSEC-02: Tenant Alpha NO puede destruir sesión de Tenant Beta', async ({ request }) => {
    // 1. Autenticar como Arquitecto de Tenant Alpha
    const loginRes = await request.post(`${API.BASE_URL}/api/v1/auth/login`, {
      data: { email: USERS.ADMIN_ALPHA.email, password: USERS.ADMIN_ALPHA.password }
    });
    const { token } = await loginRes.json();
    
    // 2. Intentar destruir sesión que pertenece a Tenant Beta
    const deleteRes = await request.delete(`${API.BASE_URL}${API.COPILOT_SESSION}?sessionId=session_beta_001`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    
    // 3. MUST be 403 Forbidden — NOT 200
    expect(deleteRes.status()).toBe(403);
  });

  test('CU-JSEC-02b: Tenant Alpha SÍ puede destruir su propia sesión', async ({ request }) => {
    const loginRes = await request.post(`${API.BASE_URL}/api/v1/auth/login`, {
      data: { email: USERS.ADMIN_ALPHA.email, password: USERS.ADMIN_ALPHA.password }
    });
    const { token } = await loginRes.json();
    
    const deleteRes = await request.delete(`${API.BASE_URL}${API.COPILOT_SESSION}?sessionId=session_alpha_001`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    
    expect(deleteRes.status()).toBe(200);
  });
});
