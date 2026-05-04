import { APIRequestContext } from '@playwright/test';

export async function seedUS025Users(request: APIRequestContext) {
  const users = [
    { username: 'admin@ibpms.local', email: 'admin@ibpms.local', roles: ['ROLE_SUPER_ADMIN'], password: 'password' },
    { username: 'operador@ibpms.local', email: 'operador@ibpms.local', roles: ['ROLE_OPERADOR'], password: 'password' },
    { username: 'sac.lider@ibpms.local', email: 'sac.lider@ibpms.local', roles: ['ROLE_SAC_LIDER'], password: 'password' },
    { username: 'pm@ibpms.local', email: 'pm@ibpms.local', roles: ['ROLE_PM'], password: 'password' },
    { username: 'multirole@ibpms.local', email: 'multirole@ibpms.local', roles: ['ROLE_SUPER_ADMIN', 'ROLE_OPERADOR'], password: 'password' }
  ];

  for (const u of users) {
    // Intentar verificar si el usuario existe
    const res = await request.get(`/api/v1/users/${u.username}`);
    if (!res.ok()) {
      // Si no existe, crearlo
      await request.post('/api/v1/users', { data: u });
    }
  }
}

export async function loginAs(page: any, username: string, roles: string[]) {
  // Simulador de token JWT para el frontend (evitando depender del auth real si la BD no está pre-configurada perfectamente, aunque intentamos Zero-Mock).
  // Se inserta en localStorage para que el Pinia store lo tome al cargar la app.
  const payload = { sub: username, roles: roles.map(r => `ibpms_rol_${r}`) };
  const fakeToken = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${btoa(JSON.stringify(payload))}.signature`;
  
  await page.addInitScript((token: string) => {
    localStorage.setItem('ibpms_token', token);
  }, fakeToken);
}
