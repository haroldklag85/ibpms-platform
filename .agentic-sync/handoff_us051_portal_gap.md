# Handoff: Resolución de Gaps (Portal.vue y Auditoría) - US-051 Sprint 6

**Destinatario:** Agente Frontend
**Emisor:** Arquitecto Líder
**Contexto:** Estamos cerrando la certificación técnica de la US-051. Tras una auditoría exhaustiva, identificamos que la mayoría de los gaps de seguridad (SSE, Soft-Refresh, Fail-Closed) ya están cubiertos en tu código. Sin embargo, persisten 2 gaps críticos de Frontend que bloquean el paso a la fase de pruebas UAT E2E.

**Gobernanza:** Sigue estrictamente la regla **Zero-Mock** y **Zero-Trust**. No quemes datos en código. Todo debe depender del estado de `authStore` y los datos del backend.

---

## 🛠 Tarea 1: Refactorización de `Portal.vue` (Scenarios #7 y #16 - CA-37)

**Problema actual:**
En `frontend/src/views/Portal.vue`, la sección de "Procesos Frecuentes Quick Links" está hardcodeada con botones "🏖️ Vacaciones" y "🛒 Req. Compra". Además, el dashboard no se considera un verdadero "Dashboard Bifurcado" porque la inyección de widgets no es modular ni controlada por un array de roles o permisos (aunque ya tienes un IF para el Panel Administrativo).

**Acciones requeridas:**

1. **Eliminar Hardcodes (Zero-Mock):**
   - Localiza y elimina los botones estáticos "Vacaciones" y "Req. Compra" en la sección de procesos frecuentes (líneas ~5-9).
   - Sustitúyelos por una lista reactiva (`v-for`) basada en los procesos marcados como favoritos o frecuentes del usuario, o simplemente oculta la sección si no hay datos de uso real desde el Backend. (Si el endpoint no existe, renderiza la sección vacía o un esqueleto, pero NO quemes botones).

2. **Bifurcación Híbrida de Dashboards:**
   - Modifica el layout para que los **Widgets se inyecten localmente según el rol del usuario**.
   - Por ejemplo, si el usuario tiene `ROLE_OPERADOR`, muestra un widget de "Mis Tareas Pendientes" (puedes crear un placeholder o consumir un endpoint de tareas si existe).
   - Si tiene `ROLE_SUPER_ADMIN` o `ROLE_SYSTEM_ADMIN`, muestra el widget de "Estado del Sistema" o "Auditoría Rápida".
   - Usa la función `authStore.hasAnyRole(['...'])` para controlar la visibilidad de manera estricta en todo el archivo.

---

## 🛠 Tarea 2: Integración de Ojo de Sauron (`useAuditReveal.ts`)

**Problema actual:**
El composable `frontend/src/composables/useAuditReveal.ts` fue creado excelentemente para cumplir con la auditoría forzosa (CA-39), pero **actualmente no se está utilizando en ningún componente Vue**.

**Acciones requeridas:**

1. **Inyectar el Composable:**
   - Busca una vista donde se manejen credenciales o secretos (por ejemplo, en Integraciones: `ConnectorBuilder.vue`, en la configuración de la Bóveda de Azure KeyVault, o en la gestión de Identidades).
   - Importa `useAuditReveal`.
   - Ofusca (con asteriscos `***` o un tipo `password`) el secreto y coloca un botón "Revelar con Auditoría".
   - Conecta el botón al método `revealWithAudit(fieldId)`.
   - Muestra el valor en texto plano solo si `isRevealed` es `true`.
   - Asegúrate de mostrar retroalimentación visual del loading (`isRevealing`).

---

## 🚦 Criterio de Aceptación (DoD)

1. `Portal.vue` no contiene ningún texto hardcodeado simulando procesos.
2. `Portal.vue` renderiza distintos paneles según los roles del JWT hidratado (Dashboard Bifurcado).
3. `useAuditReveal` está conectado y operativo en al menos una vista de administración (ej. Secrets, Connectors o Usuarios).
4. No hay errores de linting ni de compilación (`npm run build` exitoso).

**Nota Final:** Una vez termines, notifica al humano con la frase *"Handoff Frontend Portal.vue completado"*. No modifiques ni rompas las lógicas de `authStore` o `App.vue` que ya funcionan perfectamente para el SSE y el Multi-Tab.
