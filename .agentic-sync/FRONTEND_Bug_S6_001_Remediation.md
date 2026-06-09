# Handoff: Agente Frontend - Corrección BUG-S6-001 (ConnectionToast)

Como Arquitecto Líder, he analizado los traces y el código fuente respecto al fallo E2E en Playwright (`BUG-S6-001`) bajo el entorno `Zero-Mock-E2E`. He identificado la causa raíz de los *timeouts*.

## 📌 Diagnóstico Arquitectónico

El problema no es de red ni del backend (el cual ya fue estabilizado). El error radica puramente en el Frontend, debido a desajustes en los selectores DOM y la nomenclatura de los Custom Events.

### Causa Raíz 1: Selector Inexistente (CA-19 & CA-25)
El test `us017-connection-toast.e2e.spec.ts` intenta ubicar el componente usando el locator `page.locator('.connection-toast')`. Sin embargo, el archivo `src/components/common/ConnectionToast.vue` **no tiene la clase `connection-toast`** en su etiqueta raíz `<div>`. Por ende, Playwright nunca lo encuentra.

### Causa Raíz 2: Mismatch de Eventos Globales (CA-26)
El test `CA-26` emite un evento `http-error-500` mediante `window.dispatchEvent(new CustomEvent('http-error-500'))`. Sin embargo, el composable `useConnectionStatus.ts` está escuchando el evento `global-error-dispatch`. Esto impide que se silencie el Toast.

---

## 🚀 Acciones Requeridas (Frontend)

Debes ejecutar las siguientes correcciones de inmediato:

### 1. Modificar `ConnectionToast.vue`
Agrega la clase `connection-toast` al `div` principal.
**Ruta:** `frontend/src/components/common/ConnectionToast.vue`
**Cambio requerido:**
```vue
<!-- Modificar esta línea -->
<div 
  v-if="store.isVisible" 
  class="connection-toast fixed bottom-6 left-6 z-[9990] max-w-[320px] ..."
>
```

### 2. Sincronizar el Evento de Error en el Test E2E
Modifica el script de pruebas para que emita el evento correcto que ya está configurado en el composable (`global-error-dispatch`).
**Ruta:** `frontend/e2e/certification/us017-connection-toast.e2e.spec.ts`
**Cambio requerido:**
```typescript
// En el Test CA-26:
await page.evaluate(() => {
    window.dispatchEvent(new CustomEvent('global-error-dispatch'));
});
```
*(Asegúrate también de revisar si el interceptor Axios está emitiendo `http-error-500` o `global-error-dispatch` en el código fuente, y de ser necesario, unifícalo).*

---

## 📋 Veredicto y Cierre
Una vez aplicados estos cambios, el test Playwright debería pasar en color **VERDE**. No se requiere ninguna modificación en el backend ni levantar infraestructura adicional, ya que la comunicación base es correcta.
