# Sprint 6 - Bug Tracker & Incident Log

## Registro de Incidencias QA - Ejecución Zero-Mock

**Bug ID:** BUG-S6-001
**Fecha:** 2026-04-22
**Componente:** `ConnectionToast` / `us017-connection-toast.e2e.spec.ts`
**Reportado por:** Agente QA Especialista en E2E
**Contexto:** Ejecución bajo perfil `Zero-Mock-E2E` (US-017 CA-19 a CA-26).
**Estado:** ✅ CERRADO (Retest OK)

### Descripción del Error (Solucionado)
Las pruebas automatizadas de resiliencia de conexión E2E en Playwright fallaban consistentemente en el entorno sin mocks debido a un Mismatch de Eventos y selectores faltantes. 
* Se han identificado fallos (Timeouts) tanto en `CA-19 & CA-25` como en `CA-26`.
* Al ejecutar contra el backend real estabilizado, los locators del `ConnectionToast` (`.connection-toast`) nunca logran ser visibles tras activar la desconexión simulada en el `browserContext`, o no transicionan los textos esperados (`Trabajando sin conexión`).

### Detalle Técnico (Playwright Trace)
* **Exit code**: 1 (2 failed tests)
* Las aserciones `expect(toast).toBeVisible()` exceden el límite de espera nativo. La inyección de eventos globales (`window.dispatchEvent(new CustomEvent('http-error-500'))`) tampoco gatilla la superposición esperada del `ErrorStateGlobal` bloqueando el comportamiento reactivo de Pinia/Componentes.

### Acción Requerida
Dado que la política Zero-Mock prohíbe explícitamente el uso de `page.route()`, el equipo de Arquitectura o Frontend debe revisar:
1. Si el componente `ConnectionToast.vue` está debidamente instanciado de manera global en `App.vue`.
2. Si el interceptor Axios del frontend está propagando correctamente los Custom Events (`http-error-500`) hacia los Stores sin depender de respuestas simuladas del framework de E2E.

---

## Registro de Incidencias QA - Ejecución Masiva Zero-Mock (J-04 Suite)

**Bug ID:** BUG-S6-002
**Fecha:** 2026-04-22
**Componente:** Suite Completa J-04 (53 Escenarios)
**Reportado por:** Agente QA Especialista en UAT y Playwright
**Contexto:** Ejecución Masiva E2E bajo perfil `Zero-Mock-E2E`.
**Estado:** 🔴 ABIERTO

### Descripción del Error
Durante la ejecución masiva y simultánea de los 53 escenarios del Journey J-04, se registraron múltiples caídas por `Timeout (10000ms / 30000ms exceeded)` bajo el entorno real Zero-Mock.

* **Resultado Global:** 26 Passed, 10 Skipped, 17 Failed.
* **Exit code:** 1.
* **Tiempo Total de Ejecución:** ~8.0 minutos.

### Detalle Técnico (Playwright Trace)
Las áreas principales de fallo sistémico o Timeouts reportados son:
1. **Workdesk & Bandeja de Ejecución (J-04 F1-F2):** Timeouts en carga de DataGrid (CU-J04-01), facetas con debounce, y heartbeats reactivos.
2. **Delegación y Force Route (J-04 F4-F6):** Timeouts masivos (~1.5 minutos) en tareas de reclamo y firma por parte del Director (CU-J04-20 a CU-J04-22).
3. **Múltiples Instancias (J-04 F3):** Fallo crítico al operar con 2 navegadores simultáneos.
4. **Degradación y Validaciones de Seguridad (US-039):** Timeouts y fallos de aserción en validaciones Zod de botones de Pánico y recuperación de Drafts.
5. **Kanban View (OBS-1):** Timeouts en la carga de la API real y el Modal de bloqueo.

### Acción Requerida
La infraestructura de Backend o Frontend no está soportando la carga o carece de la Data Seed esperada en tiempo de ejecución. 
* El equipo de Desarrollo debe investigar las latencias en las interacciones E2E y confirmar la disponibilidad de los flujos para Delegación y multi-instancia en el Backend Dockerizado.

---

## Registro de Incidencias QA - Retest Ejecución Masiva Zero-Mock (J-04 Suite)

**Bug ID:** BUG-S6-003
**Fecha:** 2026-04-22
**Componente:** Suite Completa J-04 (55 Escenarios)
**Reportado por:** Agente QA Especialista en UAT y Playwright
**Contexto:** Retest Masivo E2E tras parche "Backend Estabilizado y Data Seed".
**Estado:** 🔴 ABIERTO (Fallido de nuevo)

### Descripción del Error
A pesar de la remediación reportada en el backend (Data Seed en el arranque, Skipeo, Force Routing y Kanban State Machine), la ejecución E2E continúa sufriendo caídas críticas por `Timeout`.

* **Resultado Global:** 25 Passed, 10 Skipped, 1 Flaky, 19 Failed.
* **Exit code:** 1.
* **Tiempo Total de Ejecución:** ~9.3 minutos.

### Detalle Técnico (Playwright Trace)
Las áreas clave de falla siguen siendo idénticas a BUG-S6-002, indicando que el parche no resolvió los Timeouts:
1. **Workdesk & Bandeja de Ejecución (J-04 F1-F2):** Falla `CU-J04-01 | Workdesk carga en <=2s`. (DataGrid vacío o lento).
2. **Delegación y Force Route (J-04 F4-F6):** Siguen superando 1.5 minutos de Timeout en las vistas del Director.
3. **Múltiples Instancias (J-04 F3):** Falla en Multi-Browser claim and execution.
4. **Validaciones US-039:** Los botones de Pánico y Zod Validation siguen fallando silenciosamente en UI causando timeout.
5. **Kanban View:** Timeouts en el board real.

### Acción Requerida
El parche de estabilización no está reflejándose en el entorno de Pruebas `Zero-Mock-E2E`. Se solicita a Arquitectura verificar si la base de datos realmente está reteniendo la Data Seed o si el contenedor Playwright no alcanza la red del Backend a tiempo.

---

## Registro de Incidencias QA - Ejecución Forzada sin Skips (J-04 Suite)

**Bug ID:** BUG-S6-004
**Fecha:** 2026-04-22
**Componente:** Suite Completa J-04 (55 Escenarios, Sin Skips)
**Reportado por:** Agente QA Especialista en UAT y Playwright
**Contexto:** Ejecución Masiva Forzada habilitando todos los escenarios (Deudas Técnicas omitidas).
**Estado:** 🔴 ABIERTO (Mandato Directivo: Resolutorio Obligatorio)

### Descripción del Error
Bajo instrucción directa, se removieron los 10 comandos `test.skip()` de las pruebas que correspondían a funcionalidades V2 (Uploads, Autoguardados) y automatizaciones dependientes de Docker/Red (Timeouts provocados). 

* **Resultado Global:** 36 Passed, 0 Skipped, 19 Failed.
* **Exit code:** 1.
* **Tiempo Total de Ejecución:** ~11.1 minutos.

### Detalle Técnico (Playwright Trace)
Es de suma importancia notar que la ejecución incrementó los test pasados a 36, confirmando que algunas de las funcionalidades supuestamente "no automatizables" pasaron con éxito bajo el Backend actual. Sin embargo, persisten 19 fallos:
1. Las fallas reportadas en BUG-S6-003 siguen presentes (Workdesk, Timeouts de Director, Kanban Modal y Multi-instancia).
2. Forzar los tests bloqueados por "Falta de implementación V2" no revirtió el problema de fondo del entorno E2E.

### Directiva de Remediación (Mandato de Jefatura)
La Jefatura ha rechazado explícitamente asumir la caída por infraestructura local. Se exige que el 100% de los 55 escenarios pasen exitosamente para dar cierre a la Iteración 6.2. 
**Acción Requerida:** 
Generar Handoffs de optimización cruzada (Infra/BD, Backend, Frontend, QA) para erradicar los cuellos de botella de concurrencia, latencia del DataGrid y multi-instancia en el entorno local actual.
