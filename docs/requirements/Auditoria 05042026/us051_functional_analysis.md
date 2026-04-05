# Análisis Funcional Definitivo: US-051 (Matriz de Gobernanza Visual y Enrutamiento RBAC - Frontend)

## 1. Resumen del Entendimiento
La US-051 establece el cerco perimetral en la Capa de Cliente (Vue 3 + Vue Router + Pinia). Garantiza que ninguna ruta técnica sea accesible a usuarios sin los Claims JWT específicos, previendo vectores de ataque por suposición de URI (URL Guessing) y asegurando integridad reactiva sin FOUC (parpadeos no autorizados en el DOM).

## 2. Objetivo Principal
Blindar el SPA (Single Page Application) aislando componentes a nivel ruteo y DOM. Aplicar técnicas SRE (Site Reliability) como Expulsiones Reactivas en Caliente por WebSocket, Modal "Sudo" para borrar DBs, y Gaslighting Hacker (Falso 404).

## 3. Alcance Funcional Definido
**Inicia:** El interceptor global `router.beforeResolve()` y/o la orden de pintado de un Custom Component.
**Termina:** Se expulsa al usuario a `/login`, se exige password (Sudo) o se emite telemetría por ver contraseñas.

## 4. Lista de Funcionalidades Incluidas
- **Promesa de Hidratación Anti-Amnesia F5 (CA-4450):** Bloquea el router hasta que Pinia cargue los Claims al pulsar Refresh.
- **Gaslighting URL y Falso 404 (CA-4468):** Devuelve Vue Component 404 en vez del 403 HTTP para ofuscar que existe un Dashboard Secreto. Redirige a Login si expira la sesión 401.
- **Component Composition vs Bifurcación Rutas (CA-4496):** Todos ven el mismo `/`. Se inyectan Widgets según el RBAC.
- **Auto-Colapso Dinámico JS (CA-4489):** Los JSON del Backend dictaminan el árbol, y caen si no hay accesos hoja.
- **Telemetría de Ojo Ciego (Sauron CA-4516):** Mostrar contraseñas guardadas estáticas obliga emitir un Audit Log POST al backend asíncrono.
- **Expulsión Websocket (CA-4522):** Desloguear reactivamente a quien se le revocan permisos con `[ROLE_REVOKED]`. Y Salidas incondicionales maestras si hay bucles locales en el router.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Ninguna Brecha Severa:** La especificidad arquitectónica es altísima y blinda los Anti-Patterns de frontends modernos como el FOUC Skeleton Mismatch o Memory Leaks de State.
- **Riesgo Desempeño WebSocket `[ROLE_REVOKED]`:** Exige un esfuerzo para acoplar la JWT Store a Broadcasters (Socket.io/SockJS) y lograr que Vue reaccione a notificaciones del SysAdmin externo.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Fingerprinting por Cookie (El estado es LocalStorage dependiente, no HttpOnly Cookies absolutas).

## 7. Observaciones de Alineación o Riesgos
Destaca la arquitectura LCP "Largest Contentful Paint": pinta el Skeleton de Workdesk inmediatamente y delega a la promesa de asincronía las ramas prohibidas (Zero Trust UX). Gran solución a cuellos de botella iniciales de carga.
