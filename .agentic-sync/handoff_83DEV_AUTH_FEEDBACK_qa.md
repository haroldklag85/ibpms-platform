# Handoff QA — Validación de Feedback Diferenciado en Emergency Login

> **Emisor:** Arquitecto Líder  
> **Receptor:** Agente QA  
> **Iteración:** 83-DEV  
> **Sprint:** 6.2  
> **Prioridad:** Alta  
> **Dependencias:**  
> - `handoff_83DEV_AUTH_FEEDBACK_backend.md` (Backend)  
> - `handoff_83DEV_AUTH_FEEDBACK_frontend.md` (Frontend)  

---

## 1. Contexto

Se ha solicitado que el flujo de Emergency Login (`Break-Glass CA-4`) proporcione feedback diferenciado al usuario dependiendo del motivo exacto del fallo de autenticación. Anteriormente, el sistema mostraba un `alert()` genérico que decía "Credenciales de bóveda rechazadas o IP denegada" para todos los casos.

Tras la implementación por parte de los agentes Backend y Frontend, el QA debe validar que:
- El backend retorna los códigos de error correctos según el escenario.
- El frontend renderiza el banner visual adecuado con el color, icono y texto correspondiente.
- No existe regresión en el flujo feliz (login exitoso).

---

## 2. Datos de Prueba

### 2.1 Usuarios en la Base de Datos UAT

Los siguientes usuarios están sembrados en `ibpms-postgres-uat` vía `seed-dev.sql`:

| Email | Password (raw) | Estado | Rol |
|-------|----------------|--------|-----|
| `admin@alpha.com` | `Test123!` | Activo | ROLE_SUPER_ADMIN |
| `analista_n1@alpha.com` | `Test123!` | Activo | ROLE_OPERARIO |
| `root@ibpms.local` | _(desconocido)_ | Activo | Sin rol asignado |

> [!NOTE]
> El hash BCrypt `$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri` corresponde al password `Test123!`.

### 2.2 Emails que NO Existen

Para las pruebas de "usuario no encontrado", usar cualquier email que no esté en la tabla anterior:
- `no-existe@alpha.com`
- `fake@empresa.com`
- `inventado@test.com`

---

## 3. URL de Prueba

```
http://localhost:5174/login?emergency=true
```

> El parámetro `?emergency=true` fuerza la activación automática del modo Break-Glass.

---

## 4. Escenarios de Prueba

### ESC-01: Usuario No Encontrado

| Campo | Valor |
|-------|-------|
| **Precondición** | Navegador en `http://localhost:5174/login?emergency=true` |
| **Email** | `no-existe@alpha.com` |
| **Password** | `cualquiera` |
| **Acción** | Click en "Forzar Acceso Local" |
| **Resultado Backend** | HTTP `401` con body `{ "code": "USER_NOT_FOUND", "message": "No existe una cuenta asociada al correo proporcionado." }` |
| **Resultado Frontend** | Banner de color **ámbar** (`bg-amber-50`) con icono `person_off` y texto "No existe una cuenta asociada al correo proporcionado." |
| **Verificación DOM** | Elemento con `data-testid="login-error-banner"` visible |

### ESC-02: Contraseña Incorrecta

| Campo | Valor |
|-------|-------|
| **Precondición** | Navegador en `http://localhost:5174/login?emergency=true` |
| **Email** | `admin@alpha.com` |
| **Password** | `WrongPassword999` |
| **Acción** | Click en "Forzar Acceso Local" |
| **Resultado Backend** | HTTP `401` con body `{ "code": "INVALID_PASSWORD", "message": "La contraseña proporcionada es incorrecta." }` |
| **Resultado Frontend** | Banner de color **rojo** (`bg-red-50`) con icono `lock` y texto "La contraseña proporcionada es incorrecta." |

### ESC-03: Login Exitoso (No Regresión)

| Campo | Valor |
|-------|-------|
| **Precondición** | Navegador en `http://localhost:5174/login?emergency=true` |
| **Email** | `admin@alpha.com` |
| **Password** | `Test123!` |
| **Acción** | Click en "Forzar Acceso Local" |
| **Resultado Backend** | HTTP `200` con body `{ "token": "eyJ...", "message": "Emergency login successful" }` |
| **Resultado Frontend** | Redirección exitosa a `/workdesk`. **No se muestra ningún banner de error.** |

### ESC-04: Cuenta Deshabilitada

| Campo | Valor |
|-------|-------|
| **Precondición** | Deshabilitar un usuario en la BD: `UPDATE ibpms_security_user SET is_active = false WHERE email = 'analista_n1@alpha.com';` |
| **Email** | `analista_n1@alpha.com` |
| **Password** | `Test123!` |
| **Acción** | Click en "Forzar Acceso Local" |
| **Resultado Backend** | HTTP `403` con body `{ "code": "ACCOUNT_DISABLED", "message": "..." }` |
| **Resultado Frontend** | Banner de color **gris** (`bg-gray-100`) con icono `block` |
| **Postcondición** | Restaurar: `UPDATE ibpms_security_user SET is_active = true WHERE email = 'analista_n1@alpha.com';` |

### ESC-05: Limpieza de Banner al Reintentar

| Campo | Valor |
|-------|-------|
| **Precondición** | Ejecutar ESC-01 (banner ámbar visible) |
| **Acción** | Cambiar el email a `admin@alpha.com`, password a `Test123!` y hacer click en "Forzar Acceso Local" |
| **Resultado** | El banner ámbar **desaparece inmediatamente** al hacer click, y el login se ejecuta exitosamente (redirección a `/workdesk`) |

### ESC-06: Limpieza de Banner al Volver a SSO

| Campo | Valor |
|-------|-------|
| **Precondición** | Ejecutar ESC-02 (banner rojo visible) |
| **Acción** | Click en "← Volver al SSO Corporativo" |
| **Resultado** | Se regresa a la vista SSO. Si vuelvo a Break-Glass, el banner **no se muestra** (limpieza completa) |

### ESC-07: Backend Caído (Error Genérico)

| Campo | Valor |
|-------|-------|
| **Precondición** | Detener el backend: `docker stop ibpms-core-dev` |
| **Email** | `admin@alpha.com` |
| **Password** | `Test123!` |
| **Acción** | Click en "Forzar Acceso Local" |
| **Resultado** | Banner de color **rojo oscuro** (`bg-red-100`) con texto "Error de conexión con el servidor. Verifique que el backend esté activo." |
| **Postcondición** | Reiniciar backend: `docker start ibpms-core-dev` |

---

## 5. Script Playwright E2E (Referencia)

```typescript
import { test, expect } from '@playwright/test';

const LOGIN_URL = 'http://localhost:5174/login?emergency=true';

test.describe('Emergency Login — Feedback Diferenciado', () => {

    test('ESC-01: Muestra banner ámbar cuando el usuario no existe', async ({ page }) => {
        await page.goto(LOGIN_URL);
        await page.fill('[data-testid="email-input"]', 'no-existe@alpha.com');
        await page.fill('[data-testid="password-input"]', 'cualquiera');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 5000 });
        await expect(banner).toContainText('No existe una cuenta asociada');
    });

    test('ESC-02: Muestra banner rojo cuando la contraseña es incorrecta', async ({ page }) => {
        await page.goto(LOGIN_URL);
        await page.fill('[data-testid="email-input"]', 'admin@alpha.com');
        await page.fill('[data-testid="password-input"]', 'WrongPassword999');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 5000 });
        await expect(banner).toContainText('contraseña proporcionada es incorrecta');
    });

    test('ESC-03: Login exitoso redirige a /workdesk sin banner', async ({ page }) => {
        await page.goto(LOGIN_URL);
        await page.fill('[data-testid="email-input"]', 'admin@alpha.com');
        await page.fill('[data-testid="password-input"]', 'Test123!');
        await page.click('[data-testid="login-submit"]');
        
        await expect(page).toHaveURL(/\/workdesk/, { timeout: 10000 });
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).not.toBeVisible();
    });

    test('ESC-05: Banner se limpia al reintentar', async ({ page }) => {
        await page.goto(LOGIN_URL);
        
        // Primer intento fallido
        await page.fill('[data-testid="email-input"]', 'no-existe@alpha.com');
        await page.fill('[data-testid="password-input"]', 'x');
        await page.click('[data-testid="login-submit"]');
        
        const banner = page.locator('[data-testid="login-error-banner"]');
        await expect(banner).toBeVisible({ timeout: 5000 });
        
        // Segundo intento exitoso
        await page.fill('[data-testid="email-input"]', 'admin@alpha.com');
        await page.fill('[data-testid="password-input"]', 'Test123!');
        await page.click('[data-testid="login-submit"]');
        
        await expect(page).toHaveURL(/\/workdesk/, { timeout: 10000 });
    });
});
```

---

## 6. Criterios de Aceptación QA

| # | Criterio | Tipo | Estado |
|---|----------|------|--------|
| QA-01 | ESC-01 pasa: banner ámbar con texto correcto para usuario inexistente | E2E | `[ ]` |
| QA-02 | ESC-02 pasa: banner rojo con texto correcto para contraseña incorrecta | E2E | `[ ]` |
| QA-03 | ESC-03 pasa: login exitoso sin banner, redirección a `/workdesk` | E2E | `[ ]` |
| QA-04 | ESC-04 pasa: banner gris para cuenta deshabilitada | Manual | `[ ]` |
| QA-05 | ESC-05 pasa: banner se limpia entre reintentos | E2E | `[ ]` |
| QA-06 | ESC-06 pasa: banner se destruye al volver a SSO | Manual | `[ ]` |
| QA-07 | ESC-07 pasa: banner genérico cuando backend está caído | Manual | `[ ]` |
| QA-08 | No existe ningún `alert()` nativo en el flujo Break-Glass | Code review | `[ ]` |
| QA-09 | El API backend retorna el campo `code` en todas las respuestas de error | API test | `[ ]` |

---

## 7. Comandos Útiles para Manipulación de Datos

```powershell
# Verificar usuarios actuales
docker exec ibpms-postgres-uat psql -U ibpms_user -d ibpms_db -c "SELECT email, is_active FROM ibpms_security_user;"

# Deshabilitar un usuario (para ESC-04)
docker exec ibpms-postgres-uat psql -U ibpms_user -d ibpms_db -c "UPDATE ibpms_security_user SET is_active = false WHERE email = 'analista_n1@alpha.com';"

# Restaurar usuario (post ESC-04)
docker exec ibpms-postgres-uat psql -U ibpms_user -d ibpms_db -c "UPDATE ibpms_security_user SET is_active = true WHERE email = 'analista_n1@alpha.com';"

# Verificar roles asignados
docker exec ibpms-postgres-uat psql -U ibpms_user -d ibpms_db -c "SELECT u.email, r.name FROM ibpms_security_user u JOIN ibpms_security_user_roles ur ON u.id = ur.user_id JOIN ibpms_security_role r ON r.id = ur.role_id;"
```
