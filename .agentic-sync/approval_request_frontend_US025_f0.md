# 🏛️ Approval Request: Frontend US-025 Fase 0 (Hotfix Seguridad)

## 📌 Resumen de Cambios

He ejecutado la Fase 0 (Hotfix Seguridad) indicada por el Arquitecto Líder para corregir el bug ARQ-025-01.

1. **Refactorización de `MainLayout.vue`**:
   Se sustituyó la eliminación manual del token (`localStorage.removeItem('ibpms_token');`) por la llamada centralizada `authStore.logout();` para asegurar la correcta limpieza del estado zombie.

2. **Verificación de `authStore.ts`**:
   Se auditó la función `logout()` en `frontend/src/stores/authStore.ts`. Se verificó que realiza exhaustivamente:
   - `sseSource.close()`
   - `stopTokenRotator()`
   - `token.value = null`
   - `user.value = null`
   - `effectiveRoles.value = []`
   - `isGlobal404.value = false`
   - `localStorage.removeItem('ibpms_token')`

## ✅ Validación del Gate
- `npm run build`: **PASS** (Compilación limpia y sin errores).
- `npm run test:unit` (equivalente `npm run test`): **FAIL**.
  **Alerta Técnica**: Los tests existentes en la rama `sprint-6` fallan masivamente (`14 failed | 53 passed | 7 skipped`). Los errores (2330) están relacionados principalmente con promesas no manejadas en `fetch-event-source` durante `useDmnStore.spec.ts` y advertencias globales de sobreescritura de Pinia (`Symbol(pinia)`). Estas fallas son preexistentes a mi intervención en `MainLayout.vue`.

Dado que las instrucciones estrictamente indican "NO modificar la lógica de authStore.logout()", solicito revisión del Arquitecto Líder sobre cómo proceder ante el fallo preexistente de la suite de pruebas para alcanzar el Gate de Salida.

---
**Status:** `AWAITING_ARCHITECT_REVIEW`
