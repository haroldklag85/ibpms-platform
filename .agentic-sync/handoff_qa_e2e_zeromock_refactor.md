# 🏗️ Handoff QA — Refactorización Arquitectónica: E2E Zero-Mock Compliance

## 1. Metadatos y Contexto
- **Iteración:** Sprint 6.2 - Cierre de Auditoría V1
- **Rama Git:** `sprint-6/us-051-zero-mock-e2e` (Crear o usar si existe)
- **Objetivo:** Refactorizar masivamente 31 archivos de pruebas E2E en Playwright (`frontend/e2e/*.spec.ts`) para eliminar la dependencia de mocks de red (`page.route`) y obligarlos a operar contra el Backend real (Local CQRS/PostgreSQL).
- **Asignado a:** Especialista de Calidad (Agente QA)

## 2. Descripción de la Deuda Técnica
La reciente auditoría de arquitectura dictaminó la prohibición de simulaciones de red (*Mocking*) para el frontend. Sin embargo, un análisis exhaustivo revela que 31 archivos en `frontend/e2e/` siguen conteniendo instrucciones como:
```typescript
await page.route('**/api/v1/...', async route => {
  await route.fulfill({ status: 200, json: { ... } });
});
```
Esto viola la política **Zero-Mock** y oculta errores genuinos de Sagas, RBAC y CQRS.

## 3. Directivas de Refactorización (Plan de Acción)

### 3.1. Purga Masiva de Interceptores
Debes abrir las suites de pruebas (Ej: `agile-hub-assign.spec.ts`, `us029-saga-compliance.spec.ts`, `us002-force-unclaim.spec.ts`, etc.) y **borrar** todas las llamadas de inyección HTTP mockeada (`page.route`). 

### 3.2. Data Seeding y Fixtures
Puesto que ya no falsificaremos respuestas, el Backend debe tener los datos reales listos. 
* Si la prueba requiere que exista un "Expediente" o una "Tarea", debes utilizar un `setup` global, invocar APIs de Backdoor (`POST /api/v1/test-setup`), o hacer que el test genere la entidad vía UI antes de asertar la bandeja.
* Los tests deben loguearse genuinamente o usar un JWT válido emitido contra el backend, no un objeto estático de StorageState que burle la seguridad perimetral.

### 3.3. Estabilización de Tiempos y TTI
Al remover los mocks, las peticiones HTTP sufrirán latencia real (I/O contra PostgreSQL). 
* Asegúrate de reemplazar aserciones rígidas o estáticas por verificaciones con polling dinámico: `await expect(locator).toBeAttached({ timeout: 15000 });`.
* Ajusta los *Timeouts* de Playwright en caso de ser necesario para el entorno local.

## 4. Escenarios Específicos Prioritarios

1.  **Workdesk & CQRS (Local):** `workdesk.spec.ts` y las variaciones `us002-*.spec.ts`. Valida que las Sagas de *Claim/Unclaim* de tareas envíen el Command al backend y la posterior recarga del Grid recupere el dato correctamente de la BD.
2.  **Agile Hub:** Suite `agile-hub-*.spec.ts`. Las operaciones de Drag & Drop y Multi-Asignación deben impactar la API en vivo y esperar el 200 OK del Servidor, no de `route.fulfill`.
3.  **Identidad (RBAC):** `identity-governance.spec.ts`. Asegura que la tabla de matrices de roles se construya con datos provenientes del `RoleService` verdadero en Spring Boot.

## 5. Instrucciones Operativas para el Agente QA

1. Inicia tu labor analizando detalladamente los 31 archivos (`findstr /M /C:"page.route" frontend\e2e\*.spec.ts`).
2. Comienza la refactorización iterativa por módulo (por ejemplo, limpia primero Agile Hub, luego Intake, luego Workdesk).
3. Corre las pruebas localmente (`npx playwright test --project=authenticated`).
4. **TIENES ESTRICTAMENTE PROHIBIDO:** Dejar un solo `page.route` que intercepte `/api/v1/` dentro de la carpeta `e2e/`. La única excepción es para simular llamadas de red que provengan de 3rd-party services no controlados (como Stripe, o Graph API externa), pero NUNCA nuestro propio iBPMS Backend.
5. Una vez termines, notifica al Humano y solicita al Arquitecto Líder la revisión.
