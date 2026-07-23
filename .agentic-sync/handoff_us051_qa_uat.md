# Handoff QA: UAT Táctico Empírico (Modo Cazador) — US-051 Seguridad Perimetral

**Destinatario:** Ingeniero QA / Agente QA
**Emisor:** Arquitecto Líder de Software
**Fecha:** 2026-05-01 | **Sprint:** 6
**Workflow de Referencia:** `/pruebasUatE2e`
**Gobernanza:** ADR-011 (Pirámide de Testing) · Zero-Trust · Zero-Mock

---

## Contexto Estratégico

La US-051 contiene **18 Scenarios Gherkin** que definen la arquitectura de seguridad perimetral del iBPMS. Tras una auditoría estática profunda (Fase 1), se certificó que **16 de 18 escenarios tienen código funcional** y 2 fueron recientemente implementados por el agente Frontend (Portal.vue bifurcado).

Tu misión como Ingeniero QA es **certificar empíricamente** que todo funciona en un entorno vivo, atacando la plataforma como un usuario real y como un hacker.

---

## Fase 0: Setup DevOps (Pre-requisito obligatorio)

Antes de iniciar cualquier prueba, provisiona el ambiente:

### Backend (Docker)
```bash
cd C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform
docker-compose up -d ibpms-postgres ibpms-rabbitmq ibpms-redis ibpms-core
```
Espera a ver `ibpms-core-dev` Running y verifica los logs:
```bash
docker-compose logs -f ibpms-core
```
Solo continúa si ves `Started IbpmsCoreApplication` o `Tomcat started on port 8080`.

### Frontend (Vite Dev Server)
```bash
cd frontend
npm run dev
```
Confirma que la consola diga `VITE ready` con una URL local (ej: `http://localhost:5173/` o el puerto asignado).

### URL de Prueba
- **Login:** `http://localhost:{PUERTO_VITE}/login`
- **Portal:** `http://localhost:{PUERTO_VITE}/portal`
- **Admin:** `http://localhost:{PUERTO_VITE}/admin/identity-governance`

---

## Fase 1: Plan de Ataque por Lotes (5 CAs máximo por iteración)

### ═══════════════════════════════════════════════════
### LOTE 1: Identidad y Defensa Perimetral (CAs 1-5)
### ═══════════════════════════════════════════════════

| # | Scenario | Qué probar | Capa Principal |
|---|---|---|---|
| 1 | Hidratación Síncrona (Anti-Amnesia F5) | Refrescar la página con JWT válido | UX + Red |
| 2 | FOUC / Skeleton Loader | Verificar que aparece skeleton durante carga | UX |
| 3 | Gaslighting 404 | Acceder a URL restringida sin permisos | Seguridad |
| 4 | Jerarquía de Redirección | Navegar sin token → debe ir a Login | Red + Seguridad |
| 5 | Excepciones Perimetrales (isPublic) | Acceder a rutas públicas sin token | Seguridad |

#### Instrucciones detalladas por CA:

**CA-01: Hidratación Síncrona (Anti-Amnesia F5)**
- **Preparación:** Inicia sesión exitosamente con un usuario válido (SUPER_ADMIN o similar).
- **Acción:** Con la sesión activa en `/portal`, presiona F5 bruscamente 5 veces seguidas.
- **Validación UX:** La página NO debe mostrar la pantalla de Login. Debe aparecer brevemente el Skeleton ("Validando Identidad y Permisos IAM...") y luego resolver al Portal con tu nombre de usuario visible.
- **Validación Red (F12):** Verifica que el Skeleton NO dispara una nueva petición `/auth/login`. El JWT debe recuperarse de `localStorage.getItem('ibpms_token')` SIN consultar al servidor.
- **Criterio PASS:** Token sobrevive a F5 + Skeleton visible durante hidratación + Username renderizado correctamente.
- **Criterio FAIL:** Redirección a Login, pérdida de token, FOUC (flash blanco sin skeleton).

**CA-02: FOUC y Skeleton Loader**
- **Preparación:** Abre DevTools (F12) → Network → ✅ "Disable cache".
- **Acción:** Emula red lenta: DevTools → Network → Throttle → "Slow 3G". Recarga la página.
- **Validación UX:** Debes ver el skeleton (fondo `slate-50`, texto pulsante "Validando Identidad...") por al menos 1 segundo antes de que cargue el Portal.
- **Archivo a verificar:** `App.vue` líneas 30-37 — div con clase `animate-pulse`.
- **Criterio PASS:** Skeleton visible durante carga lenta. Cero FOUC.
- **Criterio FAIL:** Página aparece vacía o con HTML sin estilos antes del Skeleton.

**CA-03: Gaslighting 404 (Security by Obscurity)**
- **Preparación:** Inicia sesión con un usuario que tenga ROL limitado (ej: `ROLE_OPERADOR`, sin acceso a `/admin/identity-governance`).
- **Acción:** Escribe manualmente en la barra de direcciones: `http://localhost:{PUERTO}/admin/identity-governance`.
- **Validación UX:** La aplicación debe mostrar una pantalla genérica de **"404 Not Found"**, NO un "403 Forbidden" ni un "Acceso Denegado". El usuario nunca debe saber que esa ruta existe.
- **Validación Red:** La pestaña Network NO debe mostrar una petición `GET` al endpoint real de Identity Governance. El bloqueo ocurre en el Router de Vue (client-side), no en el servidor.
- **Archivo de referencia:** `RouteGuards.ts` — Si el usuario no tiene el rol requerido por `meta.requiredRoles`, redirige a `NotFound`.
- **Criterio PASS:** Pantalla 404 genérica sin indicios de ruta protegida.
- **Criterio FAIL:** Mensaje "Access Denied", error 403 visible, o carga parcial de la vista protegida.

**CA-04: Jerarquía de Redirección**
- **Preparación:** Cierra sesión o borra manualmente `localStorage.removeItem('ibpms_token')` desde la consola F12.
- **Acción:** Intenta navegar directamente a `/portal` o `/admin/roles`.
- **Validación Red:** El interceptor de Vue Router debe estrangular el intento ANTES de hacer cualquier petición HTTP al Backend. En la pestaña Network, NO debe haber ningún `GET /api/v1/...` saliendo.
- **Validación UX:** El usuario es redirigido automáticamente a `/login`.
- **Criterio PASS:** Redirección inmediata a Login sin tráfico de red al Backend.
- **Criterio FAIL:** Se ve brevemente el Portal, o se dispara una petición al API antes de redirigir.

**CA-05: Excepciones Perimetrales (isPublic)**
- **Preparación:** Cierra sesión completamente (sin token en localStorage).
- **Acción:** Navega a una ruta marcada como `isPublic: true` en el router (ej: `/login`, `/register`, o cualquier magic link público definido).
- **Validación Red:** La navegación debe completarse SIN disparar `hydrateAuth()`. Verifica en Network que no hay peticiones a `/auth/me` o `/auth/refresh`.
- **Validación Seguridad:** Ningún header `Authorization: Bearer ...` debe ser enviado en peticiones de rutas públicas.
- **Criterio PASS:** Rutas públicas accesibles sin token y sin intentos de hidratación.
- **Criterio FAIL:** Se dispara `hydrateAuth()` en rutas públicas, o aparece error de token inválido.

---

### ═══════════════════════════════════════════════════
### LOTE 2: Menú Dinámico y Dashboard (CAs 6-10)
### ═══════════════════════════════════════════════════

| # | Scenario | Qué probar | Capa Principal |
|---|---|---|---|
| 6 | Backend-Driven UI + Auto-Colapso Menú | Verificar menú lateral dinámico | Red + Backend |
| 7 | Dashboard Bifurcado (Portal.vue) | Verificar widgets por rol | UX + Seguridad |
| 8 | Privilegios Solo Lectura | Botones ocultos para roles limitados | Seguridad |
| 9 | Sudo Mode (Re-Autenticación) | Verificar modal de confirmación | UX + Seguridad |
| 10 | Ojo de Sauron (Auditoría de Secretos) | Telemetría al revelar datos | Red + Backend |

#### Instrucciones detalladas por CA:

**CA-06: Backend-Driven UI + Auto-Colapso + Caché Menú**
- **Acción:** Inicia sesión. En Network, busca una petición `GET /api/v1/ui/menu-layout`. Confirma que el JSON regresa un árbol con nodos que corresponden a los roles del usuario.
- **Validación UX:** El menú lateral (sidebar) debe renderizar SOLO las secciones permitidas por el rol. Si un nodo padre no tiene hijos visibles, NO debe aparecer (auto-colapso / poda de ramas).
- **Validación Backend:** El endpoint debe filtrar usando el `SecurityContext` (los roles vienen del JWT, no de un parámetro query).
- **Criterio PASS:** Menú coincide con los permisos del token. JSON viene de endpoint real. Nodos vacíos podados.
- **Criterio FAIL:** Menú muestra opciones a las que el rol no tiene acceso, o datos vienen de un mock.

**CA-07: Dashboard Bifurcado (Portal.vue)**
- **Acción con ROLE_OPERADOR:** Inicia sesión con un operador. Navega a `/portal`.
- **Validación:** Debe verse el panel "Mis Tareas Pendientes" (amarillo). NO debe verse el panel "Administración del Sistema" (índigo) ni "Auditoría Rápida" (rojo).
- **Acción con ROLE_SUPER_ADMIN:** Inicia sesión con un admin. Navega a `/portal`.
- **Validación:** Deben verse AMBOS paneles: "Panel Administrativo" y, si el admin también tiene ROLE_OPERADOR, el de tareas.
- **Archivo:** `Portal.vue` — `v-if="authStore.hasAnyRole(['ROLE_OPERADOR'])"` (L44) y `v-if="authStore.hasAnyRole(['ROLE_SUPER_ADMIN', 'ROLE_SYSTEM_ADMIN'])"` (L56).
- **Criterio PASS:** Widgets se muestran/ocultan según el rol.
- **Criterio FAIL:** Todos los widgets visibles para todos los roles, o ninguno visible.

**CA-08: Privilegios Solo Lectura (Granularidad CRUD)**
- **Acción:** Inicia sesión con un rol que NO sea `ROLE_AI_ADMIN`. Navega a `DmnIntelligence` o `ConnectorBuilder`.
- **Validación UX:** Los botones de "Reset a V1", "Publicar" y "Aprobar Configuración" deben estar OCULTOS (no deshabilitados, sino completamente ausentes del DOM).
- **Inspección DOM:** Abre F12 → Elements → Busca los botones. Si el `v-if` funciona correctamente, el botón NO debe existir en el HTML renderizado.
- **Criterio PASS:** Botones destructivos ocultos para roles sin `ROLE_AI_ADMIN`.
- **Criterio FAIL:** Botones visibles o solo deshabilitados (`:disabled` en vez de `v-if`).

**CA-09: Sudo Mode (Re-Autenticación Destructiva)**
- **Acción:** Busca una acción protegida por Sudo Mode (acciones destructivas en administración). Intenta ejecutarla.
- **Validación UX:** Debe aparecer un modal de re-autenticación pidiendo la contraseña antes de ejecutar la acción.
- **Nota:** El test `SudoModal.spec.ts.disabled` está deshabilitado. Verifica que la funcionalidad real funcione aunque el test esté apagado. Si el componente SudoModal no se invoca en ninguna acción real, reporta como **Parcial** con nota: "Componente existe pero sin integración activa".
- **Criterio PASS:** Modal aparece, bloquea acción hasta re-autenticación.
- **Criterio FAIL:** Acción destructiva se ejecuta sin confirmación de identidad.

**CA-10: Ojo de Sauron (Auditoría de Secretos)**
- **Acción:** Busca una vista donde se haya integrado `useAuditReveal` (ConnectorBuilder o similar). Localiza un campo de secreto ofuscado (`***`).
- **Acción:** Haz clic en el botón "Revelar con Auditoría".
- **Validación Red:** Debe dispararse un `POST /api/v1/audit/events` con payload: `{ eventType: 'SECRETS_VIEWED', resourceId: '<fieldId>', reason: 'user_requested' }`.
- **Validación Backend:** El endpoint debe responder `200 OK`.
- **Validación Fail-Closed:** Simula un error: desconecta el backend (docker-compose stop ibpms-core). Intenta revelar de nuevo. El dato debe permanecer oculto y mostrar alerta "No se pudo registrar la visualización".
- **Nota:** Si `useAuditReveal` no está integrado en ninguna vista (ningún `.vue` lo importa), reporta como **NO CUMPLE** con nota: "Composable existe pero sin consumidor en la UI".
- **Criterio PASS:** POST auditado + dato revelado tras éxito + dato oculto tras fallo.
- **Criterio FAIL:** Dato se revela sin POST de auditoría, o se revela incluso cuando el POST falla.

---

### ═══════════════════════════════════════════════════
### LOTE 3: Seguridad SSE y Revocación (CAs 11-15)
### ═══════════════════════════════════════════════════

| # | Scenario | Qué probar | Capa Principal |
|---|---|---|---|
| 11 | Revocación en Caliente + Botón de Pánico | SSE [ROLE_REVOKED] dispara logout | Red + Seguridad |
| 12 | Reconexión Agresiva SSE (CA-33) | Backoff exponencial + overlay | Red + UX |
| 13 | Soft-Refresh Quirúrgico (CA-34) | [ROLES_UPDATED] rehidrata sin logout | Red + Seguridad |
| 14 | Multi-Pestaña LocalStorage (CA-35) | storage event sincroniza pestañas | Seguridad |
| 15 | Contrato JSON Menú (CA-36) | Reconciliar DTO Backend/Frontend | Red + Backend |

#### Instrucciones detalladas por CA:

**CA-11: Revocación en Caliente + Botón de Pánico**
- **Acción:** Inicia sesión. En Network, verifica que se establezca una conexión SSE a `GET /api/v1/security/stream` (tipo `EventStream`).
- **Simulación:** Si el Backend puede emitir `[ROLE_REVOKED]` por ese stream, verifica que al recibirlo aparezca alerta "⚠️ Sus privilegios han sido erradicados" y el usuario sea deslogueado.
- **Botón de Pánico:** Verifica que el botón flotante de logout (`App.vue` L52-55) esté visible siempre que haya token activo, y que al pulsarlo ejecute `authStore.logout()` correctamente.
- **Criterio PASS:** SSE conectado + revocación desloguea + botón de pánico funcional.

**CA-12 (CA-33): Reconexión Agresiva SSE**
- **Preparación:** Con sesión activa y SSE establecido.
- **Acción:** Detén el backend: `docker-compose stop ibpms-core`. Espera.
- **Validación UX:** Tras ~3 fallos de reconexión, debe aparecer un overlay opaco oscuro con spinner y texto "Reconectando con el servidor de seguridad..." (`App.vue` L41-44).
- **Validación Red:** En la consola, deben verse intentos de reconexión con backoff: 1s, 2s, 4s, 8s...
- **Acción de recuperación:** Levanta el backend: `docker-compose start ibpms-core`. El overlay debe desaparecer automáticamente al reconectar.
- **Criterio PASS:** Overlay aparece tras 3 fallos + backoff visible + auto-recuperación.
- **Criterio FAIL:** La app se queda bloqueada, o no intenta reconectar, o no muestra feedback.

**CA-13 (CA-34): Soft-Refresh Quirúrgico**
- **Acción:** Verifica en `authStore.ts` (L46-51) que existe el branch `[ROLES_UPDATED]`.
- **Nota para QA:** El Backend **aún no emite** este evento en producción. Prueba de caja blanca: inyecta manualmente desde la consola F12:
```javascript
// Simular evento SSE de soft-refresh
window.dispatchEvent(new MessageEvent('message', { data: '[ROLES_UPDATED]' }));
```
- **Si no es posible inyectar SSE así**, marca como **Parcial** con nota: "Frontend preparado, Backend pendiente de emisión".
- **Criterio PASS:** Al recibir `[ROLES_UPDATED]`, se ejecuta `hydrateAuth() + purgeTopology() + fetchMenuLayout()` SIN hacer logout.
- **Criterio FAIL:** El usuario es deslogueado al recibir `[ROLES_UPDATED]`, o el evento es ignorado.

**CA-14 (CA-35): Multi-Pestaña LocalStorage**
- **Preparación:** Inicia sesión en Pestaña A. Abre Pestaña B con el mismo sitio.
- **Acción:** En Pestaña A, cierra sesión (click en botón Logout o `authStore.logout()`).
- **Validación en Pestaña B:** Debe detectar que `ibpms_token` fue removido del localStorage y redirigir automáticamente a `/login` sin intervención del usuario.
- **Archivo:** `App.vue` L20-25 — `window.addEventListener('storage', ...)`.
- **Criterio PASS:** Logout en Pestaña A desloguea automáticamente Pestaña B.
- **Criterio FAIL:** Pestaña B permanece activa con la sesión antigua.

**CA-15 (CA-36): Contrato JSON Menú**
- **Acción:** En Network, intercepta el response de `GET /api/v1/ui/menu-layout`.
- **Validación:** Verifica que las propiedades del JSON coincidan con lo que espera el Frontend (`useMenuStore`). Busca discrepancias de naming: ¿El Backend envía `children` y el Frontend espera `items`? ¿El Backend envía `title` y el Frontend espera `label`?
- **Si hay discrepancia:** Reporta exactamente qué campo difiere y cuál es el valor esperado.
- **Criterio PASS:** Contrato alineado, menú se renderiza sin errores de consola.
- **Criterio FAIL:** `undefined` en labels del menú, errores de consola como `Cannot read property 'label' of undefined`.

---

### ═══════════════════════════════════════════════════
### LOTE 4: Telemetría y Certificación Final (CAs 16-18)
### ═══════════════════════════════════════════════════

| # | Scenario | Qué probar | Capa Principal |
|---|---|---|---|
| 16 | Dashboards Híbridos (CA-37) | Igual que #7 — validar Portal.vue | UX |
| 17 | Micro-Ping Hidratación (CA-38) | Verificar `hydrateAuth()` funcional | Red + Backend |
| 18 | Fail-Closed Telemetría (CA-39) | Si auditoría falla, dato oculto | Seguridad |

**CA-16 (CA-37): Dashboards Híbridos**
- **Nota:** Este escenario es funcionalmente idéntico a CA-07 (Dashboard Bifurcado). Si el CA-07 pasó, CA-37 pasa automáticamente. Reporta el mismo resultado con nota: "Validado en conjunto con CA-07".

**CA-17 (CA-38): Micro-Ping Hidratación**
- **Acción:** Al cargar la app, verifica en Network que se dispare una petición de hidratación (`GET /auth/me` o similar) que resuelva el perfil del usuario.
- **Validación:** El `authStore.user` debe poblarse con `username`, `roles`, y `email` reales del backend.
- **Criterio PASS:** Hidratación resuelve datos reales del JWT. Sin mocks.
- **Criterio FAIL:** `authStore.user` queda vacío o con datos hardcodeados.

**CA-18 (CA-39): Fail-Closed Telemetría**
- **Nota:** Validado conjuntamente con CA-10 (Ojo de Sauron). Si la prueba Fail-Closed del CA-10 pasó, este CA pasa automáticamente.

---

## Formato de Entrega: Reporte Autopsia Forense

Para cada Lote, entrega una tabla con este formato:

| CA | Scenario | UX | Red | Backend | Seguridad | Veredicto |
|:---|:---|:---|:---|:---|:---|:---|
| 01 | Hidratación Anti-Amnesia | ✅/❌ + detalle | ✅/❌ + detalle | ✅/❌ | ✅/❌ | PASS / FAIL / PARCIAL |

Si el veredicto es FAIL o PARCIAL, incluye:
- **Evidencia forense:** Captura del error en la consola F12, payload del Network, o screenshot del DOM.
- **Origen de la Deuda Técnica:** ¿Es un bug de Frontend, Backend, contrato, o test faltante?
- **Severidad:** Bloqueante / Mayor / Menor / Cosmético.

---

## Reglas Inquebrantables

1. 🚫 **PROHIBIDO usar mocks.** Si ves datos que no vienen de un endpoint real, repórtalo como FAIL inmediato.
2. 🚫 **PROHIBIDO aprobar más de 5 CAs por iteración.** Entrega lote, espera confirmación, continúa.
3. 🚫 **PROHIBIDO ejecutar `mvn spring-boot:run` en el host.** Solo Docker.
4. ✅ **OBLIGATORIO evidenciar tráfico de red** para cada CA que involucre datos del Backend.
5. ✅ **OBLIGATORIO probar caminos tristes** (sad paths) además de los felices.

---

## Instrucción de Invocación (Copy-Paste para el Humano)

```
Asume tu rol como Ingeniero Principal de QA y Auditor Hacker E2E. 
Lee y ejecuta el plan de pruebas detallado en:
`.agentic-sync/handoff_us051_qa_uat.md`

La US a certificar es US-051 (Seguridad Perimetral). 
Procede con la Fase 0 (Setup DevOps) y luego ataca el Lote 1.
Entrega el Reporte Autopsia Forense al terminar cada lote.
```
