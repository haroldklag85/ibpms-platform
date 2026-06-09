# 📋 Criterios de Aceptación y Gobernanza Arquitectónica (Cierre de Gaps US-051)

**Para:** Product Owner (Ingeniero de Contexto)
**De:** Arquitecto Líder (Tech Lead)
**Contexto:** Resolución de ambigüedades técnicas y definición del *Definition of Done* (DoD) para la US-051 (Identity Governance & RBAC).

---

## 📌 Contexto Estratégico

Tras una auditoría exhaustiva del código actual (`authStore.ts`, `useMenuStore.ts`, `useSudo.ts`), la Arquitectura ha seleccionado las soluciones que **maximizan el ROI** (alta seguridad y UX) **reutilizando la infraestructura existente**. 

Solicito formalmente que los siguientes puntos sean traducidos a **Acceptance Criteria (Gherkin)** dentro de la US-051 para autorizar el desarrollo por parte de los agentes Frontend y Backend.

---

## 🛡️ GAP-01: Resiliencia Perimetral y Sincronización SSE

### 1.1. Fallback de Desconexión (Reconexión Agresiva)
*   **Decisión Técnica:** Si la conexión SSE (`EventSource`) se pierde, el Frontend debe reintentar la conexión. Tras 3 intentos fallidos, se debe bloquear la pantalla.
*   **Directriz para AC (Criterio de Aceptación):** *Dado que* la red se vuelve inestable, *cuando* el SSE de seguridad falle consecutivamente, *entonces* el sistema debe desplegar un *Overlay* opaco de "Reconectando..." que impida clics fantasma hasta que la red regrese o el token expire pasivamente.

### 1.2. Revocación Quirúrgica (Soft-Refresh)
*   **Decisión Técnica:** El backend emitirá un nuevo evento `[ROLES_UPDATED]` en lugar del destructivo `[ROLE_REVOKED]`.
*   **Directriz para AC:** *Dado que* un administrador remueve o agrega un rol parcial a un usuario activo, *cuando* el SSE recibe la notificación, *entonces* la pantalla no debe hacer *Logout* forzoso, sino ejecutar una re-hidratación silenciosa (`hydrateAuth`) que actualice el Menú lateral en tiempo real.

### 1.3. Persistencia Multi-Pestaña (LocalStorage Listener)
*   **Decisión Técnica:** Uso del evento nativo `storage` de JS.
*   **Directriz para AC:** *Dado que* el usuario tiene múltiples pestañas abiertas, *cuando* cierra sesión o es expulsado en UNA de ellas, *entonces* todas las demás pestañas deben reaccionar en milisegundos y redirigirse inmediatamente a la pantalla de `/login`.

---

## 🗺️ GAP-02: Contrato de Datos (El JSON del Menú)

### 2.1. Estructura Jerárquica y Responsabilidad Estética
*   **Decisión Técnica:** El Backend entrega el árbol anidado (Nested Tree) para evitar lógica recursiva compleja en Vue. La estética (iconos, path) se mapea localmente en Frontend.
*   **Directriz para AC:** *Dado que* el Frontend solicita `/menu-layout`, *cuando* el Backend responde, *entonces* el JSON debe venir pre-anidado (Padres e Hijos). *Y* el Frontend será el único responsable de renderizar los iconos y rutas basadas en el `menu_id`, eximiendo a la Base de Datos de guardar metadatos de UI.

### 2.2. Manejo de Dashboards Híbridos
*   **Decisión Técnica:** Deducción pura en Frontend mediante Token.
*   **Directriz para AC:** *Dado que* un usuario tiene roles múltiples, *cuando* entra al Home (Workdesk), *entonces* el Frontend usará el chequeo local `hasAnyRole()` para mostrar u ocultar widgets específicos, sin necesidad de que el Backend calcule la matriz de widgets en la API.

---

## 🔒 GAP-03: Almacenamiento, Fricción y Protección

### 3.1. Vector de Ataque Local y F5 (Amnesia)
*   **Decisión Técnica:** Se mantiene el JWT en LocalStorage (riesgo aceptado visualmente, pero blindado transaccionalmente) y se mantiene el *Micro-Ping* en `hydrateAuth()`.
*   **Directriz para AC:** *Dado que* el usuario presiona F5 o reabre el navegador, *cuando* el sistema hidrata el estado, *entonces* debe obligatoriamente ejecutar un chequeo síncrono (`/auth/effective-roles`) contra el Backend. Si la validación falla (401/403), el caché local debe ser ignorado y redirigir a Login.

### 3.2. Modalidad Sudo-Mode y Telemetría
*   **Decisión Técnica:** Sudo-Mode gestionado de forma interna (`/auth/sudo`) y política "Fail-Closed" para trazabilidad.
*   **Directriz para AC (Sudo):** *Dado que* se requiere una acción destructiva, *cuando* se invoca el Sudo-Mode, *entonces* se validará la contraseña localmente sin salir a EntraID.
*   **Directriz para AC (Telemetría):** *Dado que* un usuario intenta revelar un dato sensible, *cuando* el API de auditoría falla (Error 500), *entonces* el sistema DEBE ocultar el dato (Fail-Closed) y advertir que "No se pudo registrar la visualización por fallos de red". NUNCA mostrar información sin registro de auditoría.

---
**Firma:** *Arquitecto Líder (Tech Lead)*
**Estado:** Aprobado para su transmutación a Gherkin.
