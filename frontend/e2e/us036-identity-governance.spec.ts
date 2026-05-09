import { test, expect } from '@playwright/test';

test.describe('US-036: Identity Governance - Zero-Trust Reset Password', () => {
  
  test('Fail-Secure en error 500 de reset-password (No asigna Offline password)', async ({ page }) => {
    // Simulamos que el admin está en la vista de usuarios.
    // Usaremos el mock login configurado previamente (asumiendo que global-setup.ts y us025-seed.ts funcionan)
    // o simplemente interceptamos la red y navegamos a la vista donde se resetea la contraseña.
    
    // Inyección de un token falso para evitar el login modal (siguiendo el patrón de us025-seed.ts o el auth.json)
    // Playwright auth.json inyecta el estado por defecto. Si no, forzamos:
    await page.addInitScript(() => {
        localStorage.setItem('ibpms_token', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBpYnBtcy5sb2NhbCIsInJvbGVzIjpbImlicG1zX3JvbF9ST0xFX1NVUEVSX0FETUlOIl19.signature');
    });

    // Interceptar la solicitud REST de reset-password
    await page.route('**/api/v1/admin/users/*/reset-password', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Internal Server Error', message: 'Database unreachable' })
      });
    });

    // Mock de la lista de usuarios para asegurar que se muestre la grilla independientemente de la base de datos
    await page.route('**/api/v1/admin/users', route => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
            { id: '123', username: 'testuser', email: 'test@example.com', roles: [], isActive: true, isExternalIdp: false }
        ])
      });
    });

    // Navegar a la sección de administración de usuarios
    // (Asumiendo que es /admin/security/identity según el enrutador)
    await page.goto('/admin/security/identity', { waitUntil: 'domcontentloaded' });
    
    // Interceptar también la petición de la lista de usuarios para que la grilla cargue al menos un usuario
    // Como es Zero-Mock y Backend REAL, /api/v1/admin/users debería responder con data.
    // Si la BD tiene data de us025-seed, aparecerán los usuarios.
    
    // Esperar a que cargue la grilla de usuarios y exista al menos un botón "Editar"
    const editBtn = page.locator('button:has-text("Editar")').first();
    await editBtn.waitFor({ state: 'visible', timeout: 15000 });
    
    // Abrir el modal del usuario
    await editBtn.click();
    
    // Log de requests para debug
    page.on('request', req => console.log('REQ:', req.url()));

    // Buscar el botón "REINICIAR KEY" dentro del modal
    const resetBtn = page.locator('button:has-text("REINICIAR KEY")').first();
    await resetBtn.waitFor({ state: 'visible', timeout: 5000 });
    
    // Hacer clic en reiniciar contraseña
    await resetBtn.click();
    
    // Aserciones de Zero-Trust Fail-Secure
    // 1. Debe aparecer un Toast/Notificación ROJA o componente de error
    const errorToast = page.locator('.bg-red-600').first();
    await expect(errorToast).toBeVisible({ timeout: 10000 });
    
    // 2. NO debe aparecer un modal o cuadro con una contraseña temporal (e.g. "Clave Temporal Generada")
    const tempPasswordModal = page.locator('text="Clave Temporal Generada"');
    await expect(tempPasswordModal).toHaveCount(0);
  });

});
