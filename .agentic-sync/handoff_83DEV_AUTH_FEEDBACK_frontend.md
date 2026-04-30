# Handoff Frontend — Feedback Diferenciado en Login Break-Glass

> **Emisor:** Arquitecto Líder  
> **Receptor:** Agente Frontend  
> **Iteración:** 83-DEV  
> **Sprint:** 6.2  
> **Prioridad:** Alta  
> **Dependencia:** `handoff_83DEV_AUTH_FEEDBACK_backend.md` (debe implementarse primero)  

---

## 1. Contexto

Actualmente el componente `Login.vue` (línea 246-248) captura **cualquier** error del endpoint `POST /auth/emergency-login` con un `alert()` genérico:

```javascript
} catch (e) {
    alert('Credenciales de bóveda rechazadas o IP denegada.');
}
```

Esto no diferencia entre los distintos motivos de fallo (usuario inexistente, contraseña incorrecta, cuenta deshabilitada), generando confusión operativa. Se requiere mostrar un **mensaje contextual específico** en la interfaz según el código de error del backend.

---

## 2. Requerimiento

Reemplazar el `alert()` genérico en `handleEmergencyLogin()` con un componente visual inline que muestre mensajes diferenciados según el `code` del response body del backend.

---

## 3. Contrato Backend (Referencia)

El backend retornará los siguientes `code` en el body JSON de respuesta:

| HTTP Status | `code` | Significado |
|-------------|--------|-------------|
| 401 | `USER_NOT_FOUND` | El email no existe en la base de datos |
| 401 | `INVALID_PASSWORD` | El email existe pero la contraseña es incorrecta |
| 403 | `ACCOUNT_DISABLED` | La cuenta existe pero está deshabilitada |
| 400 | `MISSING_FIELDS` | Falta email o password en el request |

---

## 4. Archivo a Modificar

| Archivo | Ruta Completa |
|---------|---------------|
| `Login.vue` | `frontend/src/views/Login.vue` |

---

## 5. Especificación de Cambios

### 5.1 Agregar Estado Reactivo para el Error

En la sección `<script setup>`, agregar una variable reactiva para almacenar el mensaje de error:

```typescript
// Agregar junto a las otras variables (después de línea 164)
const loginError = ref<{ code: string; message: string } | null>(null);
```

### 5.2 Refactorizar `handleEmergencyLogin()` (Líneas 239-249)

```typescript
const handleEmergencyLogin = async () => {
    loginError.value = null; // Limpiar error previo
    try {
        console.log(`[BREAK-GLASS] Forzando POST /auth/emergency-login para ${email.value}`);
        const response = await apiClient.post('/auth/emergency-login', { 
            email: email.value, 
            password: password.value 
        });
        const { token } = response.data;
        authStore.login(token);
        router.push('/workdesk');
    } catch (e: any) {
        const responseData = e?.response?.data;
        const code = responseData?.code || 'UNKNOWN';
        const message = responseData?.message;

        switch (code) {
            case 'USER_NOT_FOUND':
                loginError.value = {
                    code,
                    message: message || 'No existe una cuenta asociada al correo proporcionado.'
                };
                break;
            case 'INVALID_PASSWORD':
                loginError.value = {
                    code,
                    message: message || 'La contraseña proporcionada es incorrecta.'
                };
                break;
            case 'ACCOUNT_DISABLED':
                loginError.value = {
                    code,
                    message: message || 'La cuenta se encuentra deshabilitada. Contacte al administrador.'
                };
                break;
            case 'MISSING_FIELDS':
                loginError.value = {
                    code,
                    message: message || 'Debe ingresar correo y contraseña.'
                };
                break;
            default:
                loginError.value = {
                    code: 'UNKNOWN',
                    message: 'Error de conexión con el servidor. Verifique que el backend esté activo.'
                };
        }
    }
};
```

### 5.3 Agregar Banner Visual en el Template

Insertar **inmediatamente antes del `<form>`** (entre línea 61 y 63) el siguiente bloque:

```html
<!-- Banner de Error Diferenciado (Break-Glass) -->
<div 
    v-if="loginError" 
    data-testid="login-error-banner"
    class="rounded-lg p-3 text-center mb-4 border animate-fade-in"
    :class="{
        'bg-amber-50 border-amber-200': loginError.code === 'USER_NOT_FOUND',
        'bg-red-50 border-red-200': loginError.code === 'INVALID_PASSWORD',
        'bg-gray-100 border-gray-300': loginError.code === 'ACCOUNT_DISABLED',
        'bg-yellow-50 border-yellow-200': loginError.code === 'MISSING_FIELDS',
        'bg-red-100 border-red-300': loginError.code === 'UNKNOWN'
    }"
>
    <div class="flex items-center justify-center gap-2">
        <span class="material-symbols-outlined text-[18px]"
            :class="{
                'text-amber-600': loginError.code === 'USER_NOT_FOUND',
                'text-red-600': loginError.code === 'INVALID_PASSWORD' || loginError.code === 'UNKNOWN',
                'text-gray-600': loginError.code === 'ACCOUNT_DISABLED',
                'text-yellow-600': loginError.code === 'MISSING_FIELDS'
            }"
        >
            {{ loginError.code === 'USER_NOT_FOUND' ? 'person_off' : 
               loginError.code === 'INVALID_PASSWORD' ? 'lock' : 
               loginError.code === 'ACCOUNT_DISABLED' ? 'block' : 
               'error' }}
        </span>
        <p class="text-sm font-semibold"
            :class="{
                'text-amber-800': loginError.code === 'USER_NOT_FOUND',
                'text-red-800': loginError.code === 'INVALID_PASSWORD' || loginError.code === 'UNKNOWN',
                'text-gray-800': loginError.code === 'ACCOUNT_DISABLED',
                'text-yellow-800': loginError.code === 'MISSING_FIELDS'
            }"
        >
            {{ loginError.message }}
        </p>
    </div>
</div>
```

### 5.4 Limpiar Error al Cambiar de Vista

En la función `disableBreakGlass()` (línea 186), agregar limpieza del error:

```typescript
const disableBreakGlass = () => {
    isBreakGlass.value = false;
    loginError.value = null;  // ← AGREGAR
    router.replace({ query: {} });
    email.value = '';
    password.value = '';
};
```

---

## 6. Iconografía del Banner

| Código | Icono Material | Color de Fondo | Color de Texto |
|--------|---------------|----------------|----------------|
| `USER_NOT_FOUND` | `person_off` | `amber-50` | `amber-800` |
| `INVALID_PASSWORD` | `lock` | `red-50` | `red-800` |
| `ACCOUNT_DISABLED` | `block` | `gray-100` | `gray-800` |
| `MISSING_FIELDS` | `error` | `yellow-50` | `yellow-800` |
| `UNKNOWN` | `error` | `red-100` | `red-800` |

---

## 7. Comportamiento UX Esperado

1. El banner **se oculta** al cargar la página (`loginError = null`).
2. El banner **aparece con animación** (`animate-fade-in`) tras un intento fallido.
3. El banner **se limpia automáticamente** cuando el usuario envía un nuevo intento (primera línea de `handleEmergencyLogin`).
4. El banner **se destruye** si el usuario vuelve al flujo SSO con "← Volver al SSO Corporativo".
5. **No se usa `alert()`** bajo ninguna circunstancia.

---

## 8. Atributos para Testing

| Elemento | `data-testid` |
|----------|---------------|
| Banner contenedor | `login-error-banner` |
| Input email | `email-input` (ya existe) |
| Input password | `password-input` (ya existe) |
| Botón submit | `login-submit` (ya existe) |

---

## 9. Criterios de Aceptación

| # | Criterio | Verificación |
|---|----------|-------------|
| CA-1 | Al ingresar un email que no existe, se muestra un banner ámbar con icono `person_off` y texto que indica que la cuenta no existe | Visual + QA E2E |
| CA-2 | Al ingresar una contraseña incorrecta para un email válido, se muestra un banner rojo con icono `lock` y texto que indica contraseña incorrecta | Visual + QA E2E |
| CA-3 | Al enviar el form vacío (si HTML validation es bypasseada), se muestra banner amarillo con mensaje de campos faltantes | Visual |
| CA-4 | Si el backend está caído, se muestra banner rojo genérico con mensaje de conexión | Visual |
| CA-5 | El banner desaparece al reintentar el login | Funcional |
| CA-6 | No se usa `alert()` en ningún caso | Code review |
| CA-7 | El atributo `data-testid="login-error-banner"` existe para que QA lo utilice en Playwright | Inspección DOM |
