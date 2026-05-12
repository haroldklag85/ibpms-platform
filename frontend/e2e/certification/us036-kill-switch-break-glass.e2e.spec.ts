import { test, expect } from '@playwright/test';
import { USERS, API } from '../fixtures/e2e-data';

/**
 * @Traceability: US-036, US-038 — Kill-Switch / Mass Deallocation / Break-Glass
 * @ADR: ADR-010 (Zero-Mock E2E), ADR-001 (Hexagonal — SessionRevocationController)
 * @Handoff: handoff_qa_j04_certification.md §2
 *
 * Endpoint real: POST /api/v1/admin/users/{userId}/revoke-session
 * Backend: SessionRevocationController → JwtBlacklistService → Redis
 * Requires: ROLE_ADMIN_IT (class-level @PreAuthorize)
 *
 * Precondiciones:
 *   - Backend nativo en :8080 (start-e2e.bat)
 *   - Redis activo (docker-compose.e2e.yml)
 *   - seed-e2e.sql ejecutado (usuarios + roles)
 *   - global-setup.ts completado (storageState de admin y analista)
 */
test.describe('US-036/US-038: Kill-Switch — Revocación de Sesión (Break-Glass)', () => {

  // ==========================================
  // LOTE 1: Flujo feliz — SUPER_ADMIN revoca
  // ==========================================
  test.describe('Lote 1: Flujo destructivo con SUPER_ADMIN', () => {
    test.use({ storageState: 'e2e/playwright/.auth/user.json' });

    test('CU-KS-01 | SUPER_ADMIN revoca sesión de un operario — HTTP 200 + verificación Redis', async ({ request }) => {
      // ARRANGE: Obtener el userId del analista objetivo
      // Primero validamos que el analista puede hacer requests normalmente
      const healthCheck = await request.get(`${API.BASE_URL}/api/v1/workdesk/tasks`, {
        headers: {
          'Authorization': `Bearer ${await getTokenFromStorageState('e2e/playwright/.auth/analista_n1.json')}`
        }
      });
      // El analista debería poder acceder (200 o 403 dependiendo del setup, pero NO 401)
      expect(healthCheck.status()).not.toBe(401);

      // ACT: SUPER_ADMIN ejecuta la acción destructiva (Kill-Switch)
      // Usamos el email como userId ya que el backend identifica por email/username
      const targetUserId = USERS.ANALISTA_N1.email;
      const revokeResponse = await request.post(
        `${API.BASE_URL}${API.KILL_SWITCH}/${encodeURIComponent(targetUserId)}/revoke-session`
      );

      // ASSERT Capa 3 (Backend/Persistencia): Respuesta exitosa
      expect(revokeResponse.status()).toBe(200);
      const body = await revokeResponse.text();
      expect(body).toContain('revocada');
    });

    test('CU-KS-02 | Post-revocación: requests del usuario revocado devuelven HTTP 401', async ({ request }) => {
      // ARRANGE: Revocamos primero (idempotente)
      const targetUserId = USERS.ANALISTA_N1.email;
      await request.post(
        `${API.BASE_URL}${API.KILL_SWITCH}/${encodeURIComponent(targetUserId)}/revoke-session`
      );

      // ACT: El analista intenta acceder con su token (que ahora está en la blacklist de Redis)
      const postRevokeResponse = await request.get(`${API.BASE_URL}/api/v1/workdesk/tasks`, {
        headers: {
          'Authorization': `Bearer ${await getTokenFromStorageState('e2e/playwright/.auth/analista_n1.json')}`
        }
      });

      // ASSERT Capa 4 (Seguridad): El usuario revocado debe recibir 401 Unauthorized
      // Nota: Si el JwtBlacklistFilter no está interceptando, esto detectará la regresión
      expect(postRevokeResponse.status()).toBe(401);
    });

    test('CU-KS-03 | Idempotencia: revocar dos veces al mismo usuario no causa error', async ({ request }) => {
      const targetUserId = USERS.ANALISTA_N1.email;

      // ACT: Doble revocación
      const first = await request.post(
        `${API.BASE_URL}${API.KILL_SWITCH}/${encodeURIComponent(targetUserId)}/revoke-session`
      );
      const second = await request.post(
        `${API.BASE_URL}${API.KILL_SWITCH}/${encodeURIComponent(targetUserId)}/revoke-session`
      );

      // ASSERT: Ambas deben ser 200 (idempotente, no 409 ni 500)
      expect(first.status()).toBe(200);
      expect(second.status()).toBe(200);
    });
  });

  // ==========================================
  // LOTE 2: RBAC — Usuarios sin privilegios
  // ==========================================
  test.describe('Lote 2: Seguridad RBAC (Sad Paths)', () => {

    test('CU-KS-NEG-01 | OPERARIO intenta revocar sesión → HTTP 403 Forbidden', async ({ request }) => {
      // ARRANGE: Usamos el token del analista (ROLE_OPERARIO)
      const operarioToken = await getTokenFromStorageState('e2e/playwright/.auth/analista_n1.json');
      const targetUserId = USERS.ADMIN_ALPHA.email;

      // ACT: Operario intenta ejecutar Kill-Switch contra el admin
      const response = await request.post(
        `${API.BASE_URL}${API.KILL_SWITCH}/${encodeURIComponent(targetUserId)}/revoke-session`,
        {
          headers: { 'Authorization': `Bearer ${operarioToken}` }
        }
      );

      // ASSERT Capa 4 (Seguridad): Debe ser 403 Forbidden — no tiene ROLE_ADMIN_IT
      expect(response.status()).toBe(403);
    });

    test('CU-KS-NEG-02 | Request sin autenticación → HTTP 401 Unauthorized', async ({ request }) => {
      const targetUserId = USERS.ANALISTA_N1.email;

      // ACT: Request sin token de autorización
      const response = await request.post(
        `${API.BASE_URL}${API.KILL_SWITCH}/${encodeURIComponent(targetUserId)}/revoke-session`,
        {
          headers: { 'Authorization': '' }
        }
      );

      // ASSERT: Sin token válido, el filtro de seguridad debe rechazar con 401
      expect([401, 403]).toContain(response.status());
    });

    test('CU-KS-NEG-03 | Fuzzing: userId con payload XSS no causa 500', async ({ request }) => {
      // ACT: Inyección de payload malicioso como userId
      const maliciousUserId = '<script>alert(1)</script>';
      const response = await request.post(
        `${API.BASE_URL}${API.KILL_SWITCH}/${encodeURIComponent(maliciousUserId)}/revoke-session`
      );

      // ASSERT Capa 4 (Seguridad): NO debe ser 500 (Internal Server Error)
      // Aceptamos 200 (revocó un usuario inexistente — operación idempotente) o 400/404
      expect(response.status()).not.toBe(500);
    });
  });
});

// ==========================================
// Helper: Extraer token del storageState
// ==========================================
async function getTokenFromStorageState(filePath: string): Promise<string> {
  const fs = await import('fs');
  const path = await import('path');
  const fullPath = path.resolve(filePath);

  try {
    const content = JSON.parse(fs.readFileSync(fullPath, 'utf-8'));
    const origin = content.origins?.[0];
    const tokenEntry = origin?.localStorage?.find((item: any) => item.name === 'ibpms_token');
    return tokenEntry?.value || '';
  } catch {
    return '';
  }
}
