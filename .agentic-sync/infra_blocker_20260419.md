# Reporte de Estado y Bloqueo de Certificación - Sprint 6.1

La ejecución E2E basada en el handoff `handoff_s6_qa.md` ha concluido en un **FAIL GENERAL** (0/7 specs aprobados). El proceso fue abortado por fallas críticas de infraestructura y red en la capa del contenedor, impidiendo que Playwright validara los casos de uso exitosamente.

A continuación, la trazabilidad del fallo por cada bloque mandatado:

| Bloque | Objetivo original | Estado Final de Certificación | Causa de la Falla / Excepción Encontrada |
| :---: | :--- | :--- | :--- |
| **B1** | Verificar Docker Compose levantado (healthchecks) | ❌ **FAIL** (Bloqueante Infraestructura) | Múltiples fallas. 1) Un error tipográfico pre-existente en `application.yml` (sección `servlet`) colapsó Spring Boot (`ConverterNotFoundException`). Tras corregirlo, ahora Spring Security está bloqueando transversalmente el servidor, retornando HTTP `401 Unauthorized` a todas las peticiones (incluyendo `/actuator/health` y `/api/v1/auth/login`). |
| **B2** | Crear fixtures E2E (`e2e-data.ts`) | ⚠️ **OMITIDO** | Los scripts preparatorios de Playwright (`auth.setup.ts`) no fueron ejecutados por discordancia en la ruta del `testMatch`. Adicionalmente, forzar una sesión global choca con el diseño Zero-Trust de los specs que requieren re-logueos dinámicos. |
| **B3** | Specs verificación P0 (IDOR Copilot + Webhook Legacy) | ❌ **FAIL** (Bloqueante Backend / Auth) | Las llamadas nativas vía `request.post` en Playwright chocaron con el bloqueo de Spring Security (`401 Unauthorized`). Esto arrojó un cuerpo de respuesta vacío, provocando una colisión de JS en tests: `SyntaxError: Unexpected end of JSON input` al intentar parsear el JWT. El spec del Webhook esperaba un HTTP `410 Gone` pero fracasó por el mismo motivo. |
| **B4** | Smoke Test J-04 Operario — happy path | ❌ **FAIL** (Bloqueante Frontend Server) | Caída del servidor de desarrollo de Vite. El comando `fetch("http://127.0.0.1:5173/login")` falló. Playwright arrojó `TimeoutError: page.fill: Timeout 15000ms exceeded` al intentar buscar `email-input` debido a que la página Web jamás cargó en el navegador Chrome simulado. |
| **B5** | Specs B-20 DMN dropdown + KanbanView real | ❌ **FAIL** (Bloqueante Frontend Server) | Igual que B4. Todas las aserciones de UI fallaron por Timeout al no existir el servidor Frontend para renderizar los menús ni el Canvas BPMN. |

## Diagnóstico y Reconciliación Restante

Para retomar la certificación y proceder al Cierre Formal (Fase 5+6), debemos resolver **dos bloqueadores paralelos**:
1. **[BACKEND] Ceguera de Spring Security:** Resolver por qué el WebSecurityConfig está arrojando `401` en `/api/v1/auth/login` y en el `/actuator/health`, dejándonos ciegos respecto al estado interno del motor Camunda.
2. **[FRONTEND] Caída del Servidor Vite:** El servidor está inyectando 40+ procesos NodeJS zombies que bloquean el puerto `5173`, lo que impide a Playwright levantar el UI Web. Se requiere limpieza de puertos e iniciar Vite explícitamente en IPv4 (`127.0.0.1`).

**Veredicto Emisor:** SDET AI. Esperando resolución de Dev/Arquitectura para re-engranar.
