# Handoff Arquitectónico: Iteración 7.1 - Resolución Bloqueante Vite & Observabilidad

**Fecha:** 2026-05-11
**Épica/Sprint:** Sprint 7 / Iteración 7.1
**Objetivo:** Resolver inestabilidad del servidor Vite (`Vite pre-transform error: Failed to load url /src/main.ts`) y aplicar la observabilidad de red dictada en el ADR-014 para desbloquear las pruebas E2E en Playwright.

---

## 🧭 SECUENCIA DE EJECUCIÓN ESTRICTA

Para evitar interbloqueos y falsos positivos, los agentes deberán entrar a ejecutar sus tareas en el siguiente orden:

1.  **FASE 1 - Infraestructura:** Verificación de límites de recursos en `docker-compose.e2e.yml`.
2.  **FASE 2 - Backend:** Inyección de `X-Correlation-ID` en respuestas 500 para cumplir contrato del ADR-014.
3.  **FASE 3 - Frontend:** Resolución del bloqueante de Vite (`vite.config.ts`) y refactorización del interceptor HTTP (`apiClient.ts`).
4.  **FASE 4 - QA E2E:** Refactorización de resiliencia en Playwright para ignorar errores temporales 502/503.

---

## 🛑 REGLAS GLOBALES Y CUMPLIMIENTO DE ADRs

Antes de ejecutar cualquier código, es OBLIGATORIO que lean y apliquen las siguientes políticas:

1.  **LEY GLOBAL 3 (Anti-Amnesia / Trazabilidad):** TODO cambio en el código fuente (clases Java, componentes Vue, scripts TS) **DEBE** incluir su respectiva etiqueta de trazabilidad en los comentarios (Ej: `// @Traceability: US-014 - Resolución de Bloqueante Vite S6`). No se permite código huérfano.
2.  **Cumplimiento ADR-010 (Zero-Mock Policy):** Queda estrictamente prohibido el uso de interceptores estáticos (mocks en memoria) para falsear respuestas de red exitosas. Toda prueba debe apuntar a la infraestructura real.
3.  **Cumplimiento ADR-014 (Observabilidad Frontend):** Se deben separar obligatoriamente los errores HTTP `500`, `502`, `503` y `504` en la UI, exponiendo el ID de correlación y aplicando reintentos para caídas temporales del servidor.
4.  **Cumplimiento ADR-002 (Vue3 y Vite):** Las optimizaciones en Vite deben preservar la reactividad base y el HMR sin degradar el performance del entorno de desarrollo.

---

## 🛠️ Handoffs por Especialidad

### FASE 1: Agente DevOps / Infraestructura
*   **Rol:** `[🛠️ DEVOPS / INFRA]`
*   **Skill Requerido:** Activa tu skill de Arquitectura Docker (`Docker_Compose_Optimization`).
*   **Tareas:**
    1.  Verificar que `docker-compose.e2e.yml` no tenga límites restrictivos de CPU/Memoria (`deploy.resources.limits`) que asfixien a Spring Boot en el arranque inicial. Esto es la principal causa de los timeouts iniciales de Vite.

### FASE 2: Agente BACKEND
*   **Rol:** `[⚙️ BACKEND - JAVA]`
*   **Skill Requerido:** `.agents/skills/backend_sre_compilation_audit/SKILL.md` (SRE Compilation Audit).
*   **Tareas:**
    1.  **Inyección de Correlation ID (ADR-014):** Revisa el filtro global de excepciones (Ej. `GlobalExceptionHandler.java`). Asegúrate de que las respuestas `HTTP 500` expongan un Header `X-Correlation-ID` o un campo `traceId` en el body del JSON.
    2.  **Estabilización de Arranque:** Revisa las variables de entorno de conexión a DB/Rabbit en `application-test.yml` para garantizar que el arranque del contenedor (durante E2E) no genere tiempos muertos excesivos.
    3.  **Ley Global 3:** Inyectar Javadoc de trazabilidad (`// @Traceability: ADR-014 Observabilidad 5xx`).
    4.  **Certificación (Skill SRE):** Levanta el contenedor `docker compose up -d --build ibpms-core` y verifica en los logs que inicie sin fallos.

### FASE 3: Agente FRONTEND
*   **Rol:** `[🎨 FRONTEND - VUE]`
*   **Skill Requerido:** `Vue3_Composition_API` y manipulación de ecosistema de Build (`Vite_Optimization`).
*   **Tareas:**
    1.  **Optimización Vite:** Modifica `vite.config.ts` (en `frontend/`) para incluir `optimizeDeps` asegurando el pre-bundling de dependencias pesadas, y ajusta los *timeouts* del proxy para evitar el bloqueante `Vite pre-transform error`.
    2.  **Refactorización `apiClient.ts` (ADR-014):**
        *   Abre `frontend/src/services/apiClient.ts`.
        *   Reemplaza la agrupación genérica de errores `[500, 502, 503, 504]` por lógica diferenciada descrita en el ADR-014.
        *   Implementa reintento automático para 502/503.
    3.  **Ley Global 3:** Agrega `@Traceability: ADR-014 / Bloqueante S6` en todos los archivos modificados.
    4.  **Certificación:** Ejecuta `npm run build` localmente y certifica que no hay errores de compilación TS.

### FASE 4: Agente QA (PLAYWRIGHT)
*   **Rol:** `[🕵️ QA - E2E]`
*   **Skill Requerido:** `.agents/skills/qa_e2e_validation_audit/SKILL.md` (E2E Validation Audit).
*   **Tareas:**
    1.  **Adaptación de Resiliencia en E2E:** Ajusta los scripts de configuración global de Playwright (o el helper de network) para esperar pacientemente (Polling / Retry) cuando detecte un Toast o evento de `SERVICE_UNAVAILABLE` (502/503), en lugar de declarar el test como `FAILED` instantáneamente.
    2.  **Cumplimiento ADR-010:** Garantizar que las pruebas apuntan a un flujo genuino sin interceptar `mockAdapter`.
    3.  **Ley Global 3:** Añadir comentarios descriptivos en los tests (`// @Traceability: E2E Resiliencia Vite S6`).
