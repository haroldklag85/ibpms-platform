# Original User Request

## Initial Request — 2026-05-23T23:40:00Z

# Teamwork Project Prompt — Draft

> Status: Launched
> Goal: Craft prompt → get user approval → delegate to teamwork_preview

Resolver la regresión HTTP 403 y 415 en las pruebas E2E de despliegue Sandbox (US-005) modificando el control de roles en el backend Java y el formato del payload (MultipartFile) en el test de Playwright del frontend.

Working directory: `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform`
Integrity mode: development

## Requirements

### R1. Backend Role Bypass para Sandbox
En el archivo `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java`, modifica el método `deployBpmnProcess`. El endpoint `/deploy` debe seguir exigiendo el rol `BPMN_Release_Manager` a menos que se envíe el header `X-Sandbox-Mode: true`. Si el modo sandbox está activo, se debe permitir la operación incluso si el rol no está presente. Evita alterar la lógica general y NO utilices `@PreAuthorize` en este endpoint. Preserva el código de validación existente y agrega la marca de trazabilidad `// @Traceability: US-005, CA-63 Aislamiento de Sandbox`. 

### R2. Corrección del Test QA E2E (Payload Multipart)
En el archivo `frontend/e2e/certification/us005-bpmn-modeler-persistence.e2e.spec.ts`, el test de la CA-63 envía un `fetch` hacia `/deploy` con `application/json`. Modifícalo para que utilice la API `FormData` nativa del navegador, adjuntando el XML como un `Blob` (text/xml) bajo el campo `file` y enviando `deploy_comment`. Remueve el header `Content-Type` manual para que `fetch` calcule el boundary de `multipart/form-data` automáticamente.

## Acceptance Criteria

### Compilación y Ejecución
- [ ] La compilación en el backend termina sin errores (`cd backend/ibpms-core && mvn clean compile test-compile`).
- [ ] La suite de pruebas E2E de Playwright se ejecuta exitosamente y valida el despliegue del Sandbox (`cd frontend && npx playwright test`).

### Estándares y Trazabilidad
- [ ] El código de ambas modificaciones contiene el comentario de trazabilidad obligatorio `// @Traceability: US-005, CA-63`.
- [ ] Los commits generados cumplen con el estándar de Clean Code especificado en los Handoffs (e.g. `fix(security): ...` y `test(e2e): ...`).

## Follow-up — 2026-05-25T19:51:11Z

# Teamwork Project Prompt — Draft

> Status: Launched.
> Goal: Craft prompt → get user approval → delegate to teamwork_preview

Implement the backend and frontend technical handoffs for US-004: add an async RabbitMQ consumer and refactor hexagonal architecture adapters in the backend; create a Pinia store and Dumb Component for the Intake Triage View in the frontend.

Working directory: c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform
Integrity mode: development

## Requirements

### R1. Backend: Async RabbitMQ Consumer & Refactor Hexagonal
- Mover los archivos de adaptadores (`SharePointAdapterService.java` y `MsGraphWebClientAdapter.java`) hacia la infraestructura correcta (external adapters).
- Crear `WebhookIntakeConsumer.java` utilizando `@RabbitListener` que consuma `RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK`.
- Aplicar la anotación `@Traceability` como se indica en la LEY GLOBAL 3 (US-004, CA-6, CA-8).
- Asegurarse de seguir la arquitectura hexagonal y la directiva `ADR-001-Hexagonal.md`.

### R2. Frontend: Pantalla de Triaje Humano (Dumb Components)
- Implementar el store en Pinia `useIntakeTriageStore.ts` que se comunique con la API para listar y procesar items de triaje.
- Construir `IntakeTriageView.vue` como Dumb Component, sin realizar peticiones HTTP directas desde el componente (delegando en el store).
- Aplicar clases de diseño base usando TailwindCSS, ya que está presente en la plataforma.
- Añadir la vista al Router.
- Inyectar `@Traceability` en el store y la vista.

## Acceptance Criteria

### Backend Verification
- [ ] La capa `application/service/sgdea/` ya no contiene el adaptador `SharePointAdapterService.java`.
- [ ] La compilación se ejecuta de manera exitosa: `mvn clean package -DskipTests` en el directorio del backend (`ibpms-core`).

### Frontend Verification
- [ ] El store implementa la lógica de red de Pinia (cero mock v2) y la vista redirige las acciones al store.
- [ ] La vista utiliza TailwindCSS para el estilo.
- [ ] El build de frontend completa sin errores de TypeScript: `npm run build` en el directorio de `frontend`.

### Funcional Verification
- [ ] Se ejecuta con éxito la suite de pruebas E2E correspondiente a esta US: `npx playwright test us004-webhook-intake-pipeline.e2e.spec.ts` (asumiendo backend/frontend/servicios corriendo) o se documenta/verifica la viabilidad y cobertura en caso de que un ambiente E2E completo no esté disponible al momento.
