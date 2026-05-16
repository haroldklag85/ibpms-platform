# [🕵️ QA - E2E] Reporte de Certificación J-04 — Iteración 2

**Fecha:** 2026-05-11T20:46:30-05:00
**Backend:** Nativo en :8080 (perfil `e2e`) ✅
**Infraestructura:** PostgreSQL :5433, RabbitMQ :5673, Redis :6380, Camunda :8085 — ALL HEALTHY ✅
**Spec Legacy:** `us008-kanban-hub.spec.ts` → Deprecado a `.deprecated` ✅

---

## 📊 Resultado General: 8 PASSED / 8 FAILED / 1 SKIPPED (17 tests, 8.8 min)

---

## Spec 1: `us004-webhook-intake-pipeline.e2e.spec.ts` — 5 PASS / 1 FAIL / 1 SKIP

| # | Test ID | Resultado | HTTP Real | Diagnóstico |
|---|---------|:---------:|:---------:|-------------|
| 1 | CU-WH-01 | ✅ PASS | 410 GONE | Legacy endpoint bloqueado correctamente |
| 2 | CU-WH-02 | ✅ PASS | 410 GONE | Sin ClientState → sigue 410 (no 403) |
| 3 | CU-WH-03 | ❌ FAIL | ? | HMAC placeholder rechazado — endpoint activo pero firma inválida |
| 4 | CU-WH-04 | ✅ PASS | 400 | Auto-responder bloqueado correctamente |
| 5 | CU-WH-05 | ⏭️ SKIP | — | Skipped: HMAC no configurado → no puede validar idempotencia |
| 6 | CU-WH-NEG-01 | ✅ PASS | ≠500 | XSS en subject no causa 500 |
| 7 | CU-WH-NEG-02 | ✅ PASS | ≠500 | Request vacío → error controlado |

**Hallazgo WH-03:** El test fue diseñado como tolerante (aceptar 202 o 401), pero falló en el retry. Requiere revisión del response body parsing cuando HMAC falla.

---

## Spec 2: `us008-kanban-zeromock.e2e.spec.ts` — 0 PASS / 4 FAIL

| # | Test ID | Resultado | Error | Diagnóstico |
|---|---------|:---------:|-------|-------------|
| 1 | CU-KB-01 | ❌ FAIL | `TimeoutError: page.waitForLoadState` 60s | La ruta `/kanban` no carga — ¿Frontend no tiene esta vista? |
| 2 | CU-KB-02 | ❌ FAIL | `TimeoutError: page.waitForLoadState` 60s | Misma causa raíz que KB-01 |
| 3 | CU-KB-03 | ❌ FAIL | `TimeoutError: page.waitForLoadState` 60s | Misma causa raíz que KB-01 |
| 4 | CU-KB-04 | ❌ FAIL | `TimeoutError: page.waitForLoadState` 60s | Misma causa raíz que KB-01 |

### 🔬 Análisis de Causa Raíz — Kanban
Los 4 tests de Kanban fallan con **timeout de 60 segundos** en `page.waitForLoadState('networkidle')`. Esto indica que:
1. La ruta `/kanban` no existe en el frontend actual, o
2. La vista Kanban está en una ruta diferente (ej. `/kanban-board`, `/tasks/kanban`), o
3. La página carga pero nunca alcanza `networkidle` (polling activo, WebSocket, etc.)

**Acción:** El Frontend debe confirmar cuál es la ruta real del módulo Kanban en el router de Vue.

---

## Spec 3: `us036-kill-switch-break-glass.e2e.spec.ts` — 3 PASS / 3 FAIL

| # | Test ID | Resultado | HTTP Real | Diagnóstico |
|---|---------|:---------:|:---------:|-------------|
| 1 | CU-KS-01 | ❌ FAIL | **403** (esperaba 200) | El usuario autenticado (`user.json`) NO tiene `ROLE_ADMIN_IT` ni `ROLE_SUPER_ADMIN` |
| 2 | CU-KS-02 | ❌ FAIL | **500** (esperaba 401) | El backend crashea al intentar usar el endpoint después de la revocación fallida |
| 3 | CU-KS-03 | ❌ FAIL | **403** (esperaba 200) | Misma causa raíz que KS-01: RBAC rechaza al usuario |
| 4 | CU-KS-NEG-01 | ✅ PASS | 403 | OPERARIO rechazado correctamente |
| 5 | CU-KS-NEG-02 | ✅ PASS | 401 o 403 | Sin token → rechazado correctamente |
| 6 | CU-KS-NEG-03 | ✅ PASS | ≠500 | XSS en userId no causa 500 |

### 🔬 Análisis de Causa Raíz — Kill-Switch
**El `storageState` `user.json` NO corresponde a un usuario con `ROLE_SUPER_ADMIN` o `ROLE_ADMIN_IT`.**

El `global-setup.ts` autentica con `root@ibpms.local` y guarda en `e2e/playwright/.auth/user.json`. Para que los tests KS-01/02/03 pasen, se necesita que:
1. El usuario `root@ibpms.local` (o `admin@alpha.com`) tenga el role `ROLE_SUPER_ADMIN` en la base de datos, **O**
2. Se cree un segundo `storageState` específico para `SUPER_ADMIN` (ej. `admin_superadmin.json`)

**HTTP 500 en KS-02:** Indica un bug en el backend — cuando la revocación falla (403) y luego se intenta un request con el token del analista, el backend responde 500 en vez de 401 o 200. Esto podría ser un `NullPointerException` en el `JwtBlacklistFilter`.

---

## 📋 Resumen de Hallazgos para el Arquitecto

| # | Hallazgo | Severidad | Responsable | Fix Sugerido |
|---|----------|:---------:|:-----------:|--------------|
| H-01 | `user.json` no tiene `ROLE_SUPER_ADMIN` → KS-01/02/03 reciben 403 | P1 | Backend/Infra | Seed RBAC: asignar `ROLE_SUPER_ADMIN` a `root@ibpms.local` |
| H-02 | HTTP 500 post-revocación (KS-02) sugiere bug en JwtBlacklistFilter | P1 | Backend | Investigar stack trace en logs del backend |
| H-03 | Ruta `/kanban` no carga — 4 tests en timeout 60s | P2 | Frontend | Confirmar ruta real en `router/index.ts` |
| H-04 | HMAC placeholder causa fallo en WH-03 | P3 | Infra | Documentar el secreto HMAC para E2E |

---

## ✅ Tests que SÍ certifican (8/17) — Sin mocks, contra backend real

| Test | Módulo | Capa Validada |
|------|--------|---------------|
| CU-WH-01 | Webhook Legacy | Backend: 410 GONE ✅ |
| CU-WH-02 | Webhook Legacy | Seguridad: sin ClientState = 410 ✅ |
| CU-WH-04 | Webhook Nuevo | Backend: auto-responder bloqueado ✅ |
| CU-WH-NEG-01 | Webhook Fuzzing | Seguridad: XSS no causa 500 ✅ |
| CU-WH-NEG-02 | Webhook Fuzzing | Seguridad: request vacío controlado ✅ |
| CU-KS-NEG-01 | Kill-Switch RBAC | Seguridad: OPERARIO → 403 ✅ |
| CU-KS-NEG-02 | Kill-Switch Auth | Seguridad: sin token → 401/403 ✅ |
| CU-KS-NEG-03 | Kill-Switch Fuzzing | Seguridad: XSS userId → no 500 ✅ |
