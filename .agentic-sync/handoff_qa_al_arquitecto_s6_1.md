# Handoff: SDET QA -> Arquitecto Líder (Escalamiento de Bloqueantes E2E)

**Fecha:** 2026-04-19
**Fase Actual:** Sprint 6.1 — Certificación UAT
**Estado:** `BLOQUEADO (REQUERIDO SOPORTE ARQUITECTURA)`

---

## 1. Contexto Operativo
Estimado Arquitecto Líder:

En mi rol de **SDET / UAT Automation Engineer**, he procedido a ejecutar en su totalidad los bloques detallados en `handoff_s6_qa.md` valiéndose de la suite Playwright+Vitest contra el clúster real de contenedores E2E.

Aunque la suite y la cobertura Gherkin se encuentran ensambladas y listas para la certificación `Zero-Trust`, **se ha emitido un mandato de FAIL y aborto de la prueba (0/7 aprobados)** debido al colapso transversal de las capas 2 (Servidor Web) y 3 (Backend). Es imperativo resolver las anomalías técnicas listadas a continuación para re-engranar el proceso de testeo empírico.

---

## 2. Reporte de Caos y Bloqueantes (Infra Blocker)

### 🔴 Blocker 1: Blindaje Ciego de Spring Security (Backend)
- **Síntoma:** El despliegue de `ibpms-core-dev` retorna un `401 Unauthorized` de manera global y castigadora a todas las peticiones entrantes. Esto incluye las URI expuestas explícitamente (`/actuator/health` y `/api/v1/auth/login`).
- **Daño Colateral E2E:** Como el 401 retorna con un `body` vacío, cuando los specs de seguridad P0 ejecutan `request.post` para loguearse y recuperar tokens dinámicos, Node estalla con un  `SyntaxError: Unexpected end of JSON input`.
- **Cuestión:** ¿Los recientes ajustes arquitectónicos inhabilitaron implícitamente el `WebSecurityConfig`? El motor interno de Tomcat está prendido, Camunda está escuchando, pero Spring Security ha creado una muralla irrompible.

### 🔴 Blocker 2: Caída del Proxy Frontend y Procesos Zombie
- **Síntoma:** Todos los flujos UX E2E de Playwright (incluyendo el *Smoke J-04* y *B-20 Kanban*) fracasaron en seco a los 15 segundos con `TimeoutError: page.fill: Timeout 15000ms exceeded`. La URL de arranque `http://127.0.0.1:5173/login` nunca llega a cargar.
- **Daño Colateral E2E:** El entorno de desarrollo arroja fallas en cadena si evaluamos la vida de Node (`fetch failed`). El host tiene actualmente más de 40 procesos internos residuales de `node.exe` bloqueando la salud del servidor Vite que usa Playwright.

---

## 3. Acciones Requeridas (Tareas para el Arquitecto Líder)

Para retomar mi labor de UAT, solicito:

1. **Re-estabilizar `WebSecurityConfig`**: Devolver a los endpoints `/api/v1/auth/**`, `/inbound/email-webhook` y `/actuator/health` su rol `permitAll`. Garantizar que un `POST /api/v1/auth/login` retorne los claims y el status 200 debidos.
2. **Establecer Entorno Sano:** Ejecutar una limpieza de puertos, matar dependencias zombie de Vite, y corroborar que el Compose sube de forma cristalina.
3. **Control de Retorno:** Una vez resuelto, emitir sus resultados en un archivo `.agentic-sync/handoff_architect_s6_qa_ready.md`, anexando las correcciones. 

Al recibir luz verde y sus cambios pusheados en `sprint-6/uat-certification`, relanzaré la suite y formalizaré el Acta de Cierre en un solo ciclo ininterrumpido.

> **Adjunto validado:** Recomiendo revisar los logs explícitos documentados en el registro paralelo `.agentic-sync/infra_blocker_20260419.md`.

**Atentamente,**
IA Especialista Senior de QA / UAT.
