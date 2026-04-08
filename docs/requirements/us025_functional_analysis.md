# Análisis Funcional: US-025 (Experiencia de 'Cards' Dinámicas por Rol)

**Fecha de Ejecución:** 2026-04-08
**Rol:** Product Owner / Software Architect
**Workflow Aplicado:** `/analisisEntendimientoUs.md`

## 1. Resumen del entendimiento
La historia de usuario **US-025 (Experiencia de 'Cards' Dinámicas por Rol)** no se trata de una simple pantalla, sino del **Sistema Nervioso Central de la Experiencia de Usuario (UI/UX)** para todo el Front-End (Vue.js). Regula exhaustivamente cómo la arquitectura del ecosistema web interactúa y muta basándose en la identidad del usuario, protegiendo al operador del ruido cognitivo, al mismo tiempo que establece los estándares arquitectónicos del paradigma *Client-Side* (SPA): manejo de estados de carga, fallos de red persistentes (App Shell), virtualización del DOM, animaciones destructivas aplazadas (Soft-Undo), internacionalización e inyecciones de WebSockets. 

## 2. Objetivo principal
**Central:** Empoderar la ergonomía visual y la ciberseguridad a nivel de interfaz de usuario. Busca asegurar que el Front-End actúe proactivamente destruyendo nodos prohibitivos del DOM (aplicando la filosofía Zero-Trust client-side) y ofreciendo un entorno operativo fluido, predecible y estéticamente superior que prioriza la memoria muscular y el bienestar mental (Empty states, Skeletons, Soft-locks).

## 3. Alcance funcional
**Hasta dónde llega:** Dirige la **Capa de Presentación y Montaje** del Master Layout (Header, Sidebar, Router Guards Vue.js), los estándares transversales de componentes (grillas, botones restrictivos, skeletons, Toasts, Breadcrumbs) y la reestructuración de la UI (Multi-tab para formularios vs anexos, ocultamiento responsivo en móviles).
**Dónde termina:** Se detiene en las fronteras de los datos. Esta historia **no** gestiona el API, **no** valida la seguridad estructural en la Base de Datos y **no** define el modelo paramétrico de las identidades (esto le pertenece intrínsecamente a la US-036 RBAC). El Front-end de la US-025 es "obediente" frente al JSON de roles entregado.

## 4. Lista de funcionalidades incluidas
- **Reconfiguración Estructural (Layout):** El menú (Sidebar), la campana de notificaciones (Header) y Action Cards (Main) mutan radicalmente (Se destruyen Nodos DOM enteramente) dependiendo de si el user es `System_Admin`, `Operario`, `PM` o `SAC Leader` (CA-1 al CA-4, CA-10).
- **Control de Navegación del Router (Guards):** Intercepción inmediata y re-ruteo al intentar acceder forzosamente a URLs prohibidas (CA-5).
- **Selector de Multi-Rol:** Disminución del "Frankenstein visual" forzando al operario multi-perfil a seleccionar una "única identidad / sombrero" para la sesión viva (CA-6, CA-7).
- **Responsive Degradation Agresiva (Móviles):** Eliminación completa de constructores pesados (BPMN, Formularios pro-code) en viweports reducidos (CA-8).
- **"Impersonación" Nivel Soporte L1/L2:** Herramienta para visualizar la UI deformada exactamente igual que la del usuario afectado como insumo de debugging (CA-9).
- **Ecosistema de Feedback Carga/Error/Éxito:** Evolución rítmica de Skeleton a Spinners lentos (CA-11), Toasts efímeros para Errores HTTP vs Toasts Imborrables (Nivel 0) para eventos sísmicos (CA-13, CA-21) y animaciones *Fade-Out* orgánicas en aprobaciones (CA-17).
- **Empatia Psicológica UX:** Arte gráfico en *Empty States* cuando no hay pendientes (CA-12), contornos vibrantes de accesibilidad [TAB] (CA-23), y un "Soft-lock" que vela (oscurece) la página flotante sin destruirla al expirar el JWT (CA-27).
- **Optimización Científica de Frontend (Performance):** Renderizado exclusivamente de cliente (CA-20), anclaje Virtual del DOM (destrucción activa al hacer scroll) para mitigar fugas de RAM (CA-22), anulación del *Header "sticky"* abriendo espacio vital de píxeles (CA-18), Lazy Loading masivo de librerías ECharts (CA-28) y abandono del *Split-Screen* en favor de Tabs 100% full-width (CA-29).
- **Modos de Supervivencia de Red:** App Shell que previene la "Pantalla en Blanco" reteniendo el envoltorio global estático vivo en caso de pérdida VPN (CA-19).
- **Pausa de 5 Segundos de Aniquilación:** Mecanismo Soft-Undo retardando llamadas al Backend POST/DELETE en la UI para enmendar clicks accidentales (CA-14).
- **Base i18n Estructural:** Arquitectura idiomática ES/EN en el esqueleto base (CA-24).
- **Soporte Visual de Steppers:** Orquestador inyector que gobierna la "Pestaña 1", decidiendo si esplana el formulario sencillo descendente o envuelve en un componente rastreador tipo Stepper el iForm Maestro (CA-30).

## 5. Lista de brechas, gaps o ambigüedades detectadas
- **Auditoría encubierta del Modo "Impersonator" (CA-9):** Si un Admin "simula" el perfil de Juan para ver qué pasa en el sistema y durante esa simulación por error acciona "Completar tarea", ¿El log guardará la firma biométrica / JWT del Admin (con el flag de impersonate) o falseará la data haciendo ver a Juan cómo autor? *Gap regulatorio ISO 27001 crítico que debe blindarse en el Backend y BFF.*
- **Efecto Fantasma por Cierre Abrupto (Soft-Undo CA-14):** El sistema promete esperar 5s antes de mandar el DELETE. Si el operario "elimina" la card y **cierra bruscamente la pestaña del navegador al segundo 2**, la petición POST/DELETE se colapsa. El Backend jamás se enterará. Reabrirá la app y verá la tarea viva. *Riesgo UX menor pero técnicamente probable (exige acuse visual o `navigator.sendBeacon()`).*
- **Colisión de Arquitectura: Paginación vs DOM Virtual (CA-22 versus Backend API):** Si bien el CA-22 dictamina DOM Virtualization en el listado para aguantar "5,000 Action Cards", el modelo arquitectónico SRE maduro instruye Paginación estrictamente Server-Side en Backend (ej Limit 25 por petición). ¿Existencia de listados client-side crudos de tamaño 5K no viola las normativas de estrés del motor? 
- **Inyección Websocket en Listas Paginadas (CA-25):** Forzar la aparición viva ("magic row update") en medio de una vista paginada por el Backend podría corromper localmente los límites de la página `page=1, size=20` inflando a 21 resultados discordantes, hasta que exista refresco estructural de la promesa original.

## 6. Lista de exclusiones o aspectos fuera de alcance
- **Validación Criptográfica y Creación de Identidades:** Esta US es pasiva y acata al JWT. No crea ni emite tokens ni firma su seguridad. La seguridad Backend (Spring Security Filters) se halla excluída y vive en US-036.
- **Server-Side Rendering (SSR / SEO):** Excluido por definición de diseño (CA-20). Nada de Next.js ni Nuxt.js pesados en servidor; 100% Vue SPA cliente para un ecosistema B2B cerrado.
- **Micro-Funcionalidad BPMN o Zod:** No detalla lógicas de cómo arrastrar la cuadrícula del form builder ni motor de tablas; eso recae en US-003.

## 7. Observaciones de alineación o riesgos
- **Clasificación MoSCoW:** **MUST** (Requisito fundamental, no tener el Shell maestro invalida el despliegue de las otras 50 pantallas). Confirmado cruzando con `scope_master_v1.md`.
- **Resumen de Dependencias con otras User Stories:**
  - **US-036 (RBAC & Seguridad Perimetral Frontend):** Dependencia estricta en el Payload de roles que inyecta en el Token JWT las cláusulas maestras para esconder / encender los botones del CA-1 al CA-4.
  - **US-001 (Workdesk) y US-002 (Reclamo de Tareas):** Son los beneficiarios / locaciones directas donde actuarán el Soft-Undo (CA-14), los Skeleton Loaders (CA-11) y Websocket Magic Injections (CA-25).
  - **US-009 (Dashboard de Salud):** Los paneles macro consumen y fuerzan los efectos mecánicos paramétricos de Lazy Loading requeridos por el CA-28.
- **Dependencia Bloqueante:** **Ninguna.** La US-025 es libre del nivel de base de datos. Los Arquitectos UX/UI tienen vía libre paramétrica de iniciar de inmediato armando el App Shell y el Mock del VueX Store (Pinia) "falseando" temporalmente roles en memoria de Javascript (sin backend real), configurando el Master Router de manera 100% autónoma y no-bloqueante para el equipo Front.
