# 🟢 Handoff QA — US-025 Fase 4: Certificación E2E

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** QA  
> **Prioridad:** 🟢 P2  
> **Rama Git:** sprint-6  
> **Pre-requisito:** Fases 0, 1A, 1B, 2 y 3A completadas y compilando  
> **Gate de Salida:** Playwright E2E suite 100% green + Zero-Mock compliance

---

## 1. Contexto

La US-025 (Experiencia de Cards Dinámicas por Rol) abarca 34 CAs del App Shell. Tras las fases de desarrollo (F0-F3A), se espera tener 24/34 CAs implementados. Esta fase QA valida end-to-end todos los CAs implementados usando Playwright contra backend real.

---

## 2. Estrategia QA

| Aspecto | Definición |
|---------|-----------|
| **Framework** | Playwright (`@playwright/test`) |
| **Protocolo** | Zero-Mock — Todas las pruebas contra backend Docker real |
| **Seeding** | Usar `task-seeder.ts` para crear usuarios con diferentes roles |
| **Base de datos** | PostgreSQL real (Testcontainers o Docker Compose) |
| **Navegadores** | Chromium (mínimo), opcionalmente Firefox |

---

## 3. Suites E2E

### Suite 4.1 — RBAC Sidebar Visibility

**Crear:** `frontend/e2e/us025-rbac-sidebar.spec.ts`

**CAs Validados:** CA-1, CA-2, CA-3, CA-4, CA-5

**Escenarios:**

```gherkin
Scenario: CA-1 — SuperAdmin ve todos los módulos del sidebar
  Given un usuario con rol "ROLE_SUPER_ADMIN" autenticado
  When la página carga completamente
  Then el sidebar contiene los grupos: "Mi Workdesk", "Administración y Gobernanza", "Service Delivery", "Project Builder", "Analytics & BAM", "Integration Hub", "SGDEA", "Gobernanza"

Scenario: CA-2 — Operario Base ve solo módulos operativos
  Given un usuario con rol "ROLE_OPERADOR" autenticado
  When la página carga completamente
  Then el sidebar contiene solo: "Inicio", "Mi Workdesk"
  And el sidebar NO contiene: "Administración y Gobernanza", "Service Delivery"

Scenario: CA-3 — SAC Líder ve módulos SAC
  Given un usuario con rol "ROLE_SAC_LIDER" autenticado
  When la página carga completamente
  Then el sidebar contiene: "Inicio", "Mi Workdesk"
  And el sidebar contiene acceso a funcionalidades SAC

Scenario: CA-4 — PM/Scrum Master ve Project Builder
  Given un usuario con rol "ROLE_PM" autenticado
  When la página carga completamente
  Then el sidebar contiene: "Inicio", "Mi Workdesk", "Project Builder"
  And el sidebar NO contiene: "Administración y Gobernanza"

Scenario: CA-5 — Acceso directo por URL prohibido sin rol
  Given un usuario con rol "ROLE_OPERADOR" autenticado
  When navega directamente a "/admin/analytics/bam"
  Then es redirigido a la página 404
```

**Validación técnica:**
- Usar `page.locator('[data-testid="sidebar-group"]')` para contar grupos visibles
- Verificar con `toBeHidden()` que los módulos prohibidos no aparecen
- Para CA-5: interceptar la redirección y verificar URL final

---

### Suite 4.2 — Role Switching

**Crear:** `frontend/e2e/us025-role-switching.spec.ts`

**CAs Validados:** CA-6, CA-7, CA-10

**Escenarios:**

```gherkin
Scenario: CA-6 — Usuario multi-rol puede cambiar perfil activo
  Given un usuario con roles ["ROLE_OPERADOR", "ROLE_SUPER_ADMIN"] autenticado
  When hace clic en el RoleSelectorDropdown en el Header
  And selecciona "ROLE_OPERADOR"
  Then el sidebar muestra solo módulos de operador
  And el dropdown muestra "ROLE_OPERADOR" como activo

Scenario: CA-6b — Cambio inverso restaura módulos admin
  Given el usuario está en perfil "ROLE_OPERADOR"
  When cambia a "ROLE_SUPER_ADMIN" via dropdown
  Then el sidebar muestra TODOS los módulos admin

Scenario: CA-10 — Módulos ocultos no existen en DOM
  Given un usuario con rol "ROLE_OPERADOR" autenticado
  When inspecciona el DOM del sidebar
  Then los nodos de módulos admin NO existen en el DOM (v-if, no v-show)
```

**Validación técnica CA-10:**
```typescript
// Verificar que el nodo NO existe en DOM (v-if), no solo hidden (v-show)
const adminModule = page.locator('[data-testid="sidebar-admin-governance"]');
await expect(adminModule).toHaveCount(0); // DOM ausente, no hidden
```

---

### Suite 4.3 — UX Patterns

**Crear:** `frontend/e2e/us025-ux-patterns.spec.ts`

**CAs Validados:** CA-13, CA-15, CA-17, CA-26

**Escenarios:**

```gherkin
Scenario: CA-13 — Toast no bloqueante en error del servidor
  Given un usuario autenticado
  When se produce un error 502 en una llamada API
  Then aparece un toast con mensaje de servicio no disponible
  And el toast desaparece automáticamente tras unos segundos
  And el usuario puede seguir interactuando con la app

Scenario: CA-15 — Breadcrumbs muestran ruta correcta
  Given un usuario autenticado en la ruta "/admin/modeler/bpmn"
  Then los breadcrumbs muestran: "Inicio > Modelador > BPMN"
  When hace clic en "Modelador"
  Then navega a "/admin/modeler"

Scenario: CA-17 — Transición fade entre vistas
  Given un usuario autenticado
  When navega de "/workdesk" a "/home"
  Then se observa una transición fade de 300ms

Scenario: CA-26 — Sidebar se colapsa y expande
  Given un usuario autenticado con sidebar expandido
  When hace clic en el botón de toggle del sidebar
  Then el sidebar se colapsa mostrando solo iconos
  When hace clic nuevamente
  Then el sidebar se expande mostrando iconos + labels
```

---

### Suite 4.4 — Session & Error Handling

**Crear:** `frontend/e2e/us025-session-errors.spec.ts`

**CAs Validados:** CA-19, CA-21, CA-27

**Escenarios:**

```gherkin
Scenario: CA-19 — Offline muestra modal de reconexión
  Given un usuario autenticado
  When la conexión de red se pierde (emulado)
  Then aparece el NetworkRetryModal
  When la conexión se restaura
  Then el modal desaparece

Scenario: CA-21 — Error 500 muestra Toast Fatal imborrable
  Given un usuario autenticado
  When se produce un error HTTP 500
  Then aparece un toast ROJO que muestra el traceId
  And el toast NO tiene botón de cerrar
  And el toast persiste después de 10 segundos

Scenario: CA-27 — Token expirado muestra modal de re-autenticación
  Given un usuario con token a punto de expirar
  When el token expira
  Then aparece el SessionLockModal con glassmorphism
  And el usuario puede ingresar su contraseña para desbloquear
```

**Técnica para simular errores:**
```typescript
// Interceptar requests para simular error 500
await page.route('**/api/v1/**', route => {
  route.fulfill({ status: 500, headers: { 'x-correlation-id': 'test-trace-123' } });
});
```

---

### Suite 4.5 — Density & A11y

**Crear:** `frontend/e2e/us025-density-a11y.spec.ts`

**CAs Validados:** CA-16, CA-23

**Escenarios:**

```gherkin
Scenario: CA-16 — Toggle de densidad cambia el layout
  Given un usuario autenticado con densidad "STANDARD"
  When cambia la densidad a "COMPACT" via el toggle en el Header
  Then el atributo data-density del body es "COMPACT"
  And los elementos de la interfaz se muestran más compactos

Scenario: CA-23 — Focus Ring visible en navegación por Tab
  Given un usuario autenticado
  When presiona Tab repetidamente
  Then cada elemento interactivo que recibe foco muestra un ring azul visible
  And al hacer clic (mouse), el ring NO se muestra (focus-visible only)
```

**Validación A11y técnica:**
```typescript
// Verificar focus ring via Tab
await page.keyboard.press('Tab');
const focusedEl = page.locator(':focus-visible');
const outline = await focusedEl.evaluate(el => getComputedStyle(el).outlineStyle);
expect(outline).not.toBe('none');
```

---

## 4. Seed Data Requerida

Para ejecutar los tests, el `task-seeder.ts` (o la precondición del Docker Compose) debe proveer:

| Usuario | Roles | Propósito |
|---------|-------|-----------|
| `admin@ibpms.local` | `ROLE_SUPER_ADMIN`, `Global Admin` | CA-1, CA-6 multi-rol |
| `operador@ibpms.local` | `ROLE_OPERADOR` | CA-2, CA-5, CA-10 |
| `sac.lider@ibpms.local` | `ROLE_SAC_LIDER` | CA-3 |
| `pm@ibpms.local` | `ROLE_PM` | CA-4 |

---

## 5. Criterios de Aceptación del Gate

- [ ] Suite `us025-rbac-sidebar.spec.ts` — 5 escenarios verdes
- [ ] Suite `us025-role-switching.spec.ts` — 3 escenarios verdes
- [ ] Suite `us025-ux-patterns.spec.ts` — 4 escenarios verdes
- [ ] Suite `us025-session-errors.spec.ts` — 3 escenarios verdes
- [ ] Suite `us025-density-a11y.spec.ts` — 2 escenarios verdes
- [ ] **TOTAL: 17 escenarios E2E** — 100% green
- [ ] Zero-Mock compliance: Ningún `page.route()` con data fake (excepto para simular errores de red)
- [ ] Screenshots de evidencia generados automáticamente por Playwright

## 6. Exclusiones

- NO probar CA-24 (i18n) — Diferido a V2
- NO probar CA-22 (DOM Virtualization) — Diferido a V2
- NO probar CA-9/CA-31 (Impersonación) — Diferido a V2
- CA-18 (Header No-Pegajoso) — Verificación visual menor, no E2E

## 7. Archivos Impactados

| Archivo | Acción |
|---------|--------|
| `e2e/us025-rbac-sidebar.spec.ts` | Crear |
| `e2e/us025-role-switching.spec.ts` | Crear |
| `e2e/us025-ux-patterns.spec.ts` | Crear |
| `e2e/us025-session-errors.spec.ts` | Crear |
| `e2e/us025-density-a11y.spec.ts` | Crear |
