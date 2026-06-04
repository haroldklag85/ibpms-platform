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
## Follow-up — 2026-05-25T20:25:15-05:00

# Teamwork Project Prompt

> Status: Launched
> Goal: Craft prompt → get user approval → delegate to teamwork_preview

Resolver la deuda técnica CA-07 (Gobernanza Estricta de Despliegue) mediante TDD. Se debe construir una prueba automatizada que demuestre la falta de barreras lógicas al desplegar un proceso BPMN ambiguo, y posteriormente implementar las reglas duras en el backend para bloquear dicho despliegue.

Working directory: `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform`
Integrity mode: development

## Requirements

### R1. Creación de Prueba TDD (Fase Roja)
Desarrollar una prueba de integración en el backend Java (ej. `DeploymentGovernanceIntegrationTest.java` o testeando `PreFlightAnalyzerService`) que simule la validación de un XML BPMN sintácticamente correcto, pero lógicamente ambiguo (por ejemplo: compuertas lógicas sin un flujo por defecto definido, o flujos divergentes sin convergencia clara). La prueba debe exigir que el motor rechace el despliegue con un error.

### R2. Refuerzo de Reglas de Negocio (Fase Verde)
Modificar el validador (ej. `PreFlightAnalyzerService` o el adaptador de Camunda) para detectar estas fallas topológicas y catalogarlas obligatoriamente como Errores Bloqueantes (`Hard-Stop`) y no como simples advertencias, impidiendo su paso al entorno productivo.

### R3. Aplicación de LEY GLOBAL 3
Asegurar la amnesia institucional inversa agregando la etiqueta obligatoria de trazabilidad en cada clase modificada: `// @Traceability: US-005, CA-07 Gobernanza Estricta de Despliegue`.

## Acceptance Criteria

## Follow-up — 2026-05-25T20:25:15-05:00

# Teamwork Project Prompt

> Status: Launched
> Goal: Craft prompt → get user approval → delegate to teamwork_preview

Resolver la deuda técnica CA-07 (Gobernanza Estricta de Despliegue) mediante TDD. Se debe construir una prueba automatizada que demuestre la falta de barreras lógicas al desplegar un proceso BPMN ambiguo, y posteriormente implementar las reglas duras en el backend para bloquear dicho despliegue.

Working directory: `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform`
Integrity mode: development

## Requirements

### R1. Creación de Prueba TDD (Fase Roja)
Desarrollar una prueba de integración en el backend Java (ej. `DeploymentGovernanceIntegrationTest.java` o testeando `PreFlightAnalyzerService`) que simule la validación de un XML BPMN sintácticamente correcto, pero lógicamente ambiguo (por ejemplo: compuertas lógicas sin un flujo por defecto definido, o flujos divergentes sin convergencia clara). La prueba debe exigir que el motor rechace el despliegue con un error.

### R2. Refuerzo de Reglas de Negocio (Fase Verde)
Modificar el validador (ej. `PreFlightAnalyzerService` o el adaptador de Camunda) para detectar estas fallas topológicas y catalogarlas obligatoriamente como Errores Bloqueantes (`Hard-Stop`) y no como simples advertencias, impidiendo su paso al entorno productivo.

### R3. Aplicación de LEY GLOBAL 3
Asegurar la amnesia institucional inversa agregando la etiqueta obligatoria de trazabilidad en cada clase modificada: `// @Traceability: US-005, CA-07 Gobernanza Estricta de Despliegue`.

## Acceptance Criteria

### Verificación Automatizada (TDD)
- [ ] La compilación de pruebas finaliza sin errores (`cd backend/ibpms-core && mvn test-compile`).
- [ ] La ejecución de la prueba específica de CA-07 pasa de forma nativa demostrando que el despliegue ambiguo es rechazado y no lanzado a base de datos.

### Integridad de Código
- [ ] Los comentarios de trazabilidad de la US-005 están presentes en el código fuente de producción modificado.
## Follow-up — 2026-05-26T01:36:15Z

El usuario acaba de modificar el archivo `CamundaBpmnValidationAdapter.java` agregando la lógica de validación (Hard-Stop) para CA-07 y la etiqueta de trazabilidad requerida. Solo necesitas enfocarte en construir y ejecutar la prueba TDD (`DeploymentGovernanceIntegrationTest.java`) para asegurar que el despliegue es rechazado. Debería pasar directamente (Verde) gracias a los cambios del usuario.

## Follow-up — 2026-05-30T00:47:14Z

Resolve the recurring login bug in the iBPMS authentication and Break-Glass flow, ensuring that incorrect credentials, disabled user accounts, and connection issues provide proper and styled feedback to the user, and ensure the Playwright E2E tests pass successfully.

Working directory: C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform
Integrity mode: development

## Requirements

### R1. Fix Promise Hanging on 401 Auth Errors
Modify the Axios response interceptor in `frontend/src/services/apiClient.ts` to bypass 401 interception/suspension for credential-checking endpoints (such as `/auth/login`, `/auth/emergency-login`, `/auth/break-glass`, `/auth/change-password`). These should return `Promise.reject(error)` so that caller `catch` blocks can execute.

### R2. Handle Justification Field in E2E Tests and Form
Add `data-testid="justification-input"` to the justification textarea in `frontend/src/components/auth/BreakGlassLogin.vue`. Update the Playwright E2E tests in `frontend/e2e/emergency-login-feedback.spec.ts` (ESC-01, ESC-02, ESC-03, ESC-04, ESC-05, ESC-06, ESC-07) to fill this field with a valid non-empty string before submitting the form.

### R3. Dynamic Error Banner Styling
Implement dynamic styling/colors on the error banner in `BreakGlassLogin.vue` based on the error code or type to match the visual expectations of the test suite:
- Amber (`bg-amber-50 border-amber-500 text-amber-800`) when user does not exist (`USER_NOT_FOUND`).
- Red (`bg-red-50 border-red-600 text-red-800`) when password is incorrect (`INVALID_PASSWORD`).
- Gray (`bg-gray-100 border-gray-400 text-gray-700`) when account is disabled (`ACCOUNT_DISABLED`).
- Dark Red (`bg-red-900 border-red-700 text-red-50`) for network connection failures.

## Acceptance Criteria

### E2E Test Suite Pass
- [ ] Running `npx playwright test e2e/emergency-login-feedback.spec.ts` from `ibpms-platform/frontend` succeeds with 7/7 tests passing.
- [ ] No manual browser intervention is required for the tests to pass.

## Follow-up — 2026-05-30T02:37:48Z

Cerrar la deuda técnica y las desviaciones arquitectónicas de la Arquitectura Hexagonal y DDD (ADR-001) identificadas en el backend de la plataforma `ibpms-platform`, garantizando que la capa de dominio sea totalmente pura y desacoplada de la infraestructura de persistencia (JPA/Hibernate y Spring Data).

Working directory: c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform
Integrity mode: development

## Requirements

### R1. Purificación de Modelos de Dominio (Pure POJOs)
- Remover todas las anotaciones de persistencia (`jakarta.persistence.*`, `@Entity`, `@Table`, `@Column`, `@Id`, etc.) de los modelos de dominio en `com.ibpms.poc.domain.model` (incluyendo `AllowedDomain`, `OrphanPayload`, `TriageTask`, `WebhookTransaction` y el subpaquete `agile`: `AgileProject`, `AgileTask`, `AgileTimebox`, `AgileSlaChangelog`).
- Crear las clases de entidad JPA equivalentes con el sufijo `JpaEntity` (ej. `AllowedDomainJpaEntity.java`) ubicadas en `com.ibpms.poc.infrastructure.jpa.entity`, mapeadas a las mismas tablas PostgreSQL y columnas originales.
- Implementar los mapeadores bidireccionales en infraestructura utilizando **MapStruct** para convertir entre las entidades JPA y los POJOs de dominio.

### R2. Desacoplamiento de Puertos de Dominio
- Eliminar el acoplamiento de Spring Data (`org.springframework.data.domain.Page` y `Pageable`) del puerto de dominio [TriageTaskRepository.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/domain/port/TriageTaskRepository.java).
- Definir firmas que utilicen una lista simple `List<TriageTask>` con parámetros `int page` y `int size` o una abstracción de paginación pura de dominio.
- Realizar la conversión al tipo `Page`/`Pageable` de Spring Data dentro del adaptador de infraestructura correspondiente.

### R3. Consolidación de Adaptadores e Infraestructura
- Unificar todos los paquetes de adaptadores de infraestructura (`com.ibpms.poc.infrastructure.adapters`) renombrándolos y moviéndolos hacia el namespace singular `com.ibpms.poc.infrastructure.adapter`.
- Actualizar todas las importaciones y dependencias del backend para reflejar la unificación singular de adaptadores.

### R4. Eliminación de Redundancia de APIs
- Borrar por completo el archivo `TaskDraftController.java` ubicado en `com.ibpms.poc.api.controller` para eliminar el endpoint duplicado de borradores `/api/v1/workbox/tasks/{taskId}/draft`.
- Consolidar la API REST de borradores en la ruta única e interactiva `/api/v1/drafts/{taskId}` del controlador [TaskDraftApiController.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/TaskDraftApiController.java).

## Acceptance Criteria

### [Quality & Clean Code]
- [ ] No existen referencias ni importaciones de `jakarta.persistence.*`, `@Entity`, `@Table` o dependencias de Spring Data en la capa de dominio `com.ibpms.poc.domain.model` o `com.ibpms.poc.domain.port`.
- [ ] Los adaptadores de infraestructura están consolidados bajo el namespace singular `com.ibpms.poc.infrastructure.adapter`.
- [ ] Se eliminó `TaskDraftController.java` y no existen mapeos duplicados para `/draft` en el backend.
- [ ] Todo el código modificado cuenta con los comentarios reglamentarios de trazabilidad en su cabecera: `// @Traceability: US-003 - ADR-001`.

### [Verification]
- [ ] El backend compila con éxito mediante Maven (`mvn clean compile`).
- [ ] La suite de pruebas de integración y unitarias pasa exitosamente (`mvn test`) sin regresiones.

## Follow-up — 2026-06-01T00:14:49Z

Refactor the DMN governance module of US-007 to comply with ADR-001 (Hexagonal Architecture / DDD). Ensure the domain layer is completely decoupled from the JPA persistence layer using ports and adapters.

Working directory: C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform
Integrity mode: development

## Requirements

### R1. Implement Failing Architecture Test (TDD Phase 1)
Create a new JUnit test class `DmnArchitectureComplianceTest.java` in `com.ibpms.poc.application.usecase.dmn` package under `src/test/java`.
The test must statically check `DmnGovernanceUseCase.java` (using file reading or class inspection) to assert it contains **no imports or usage** of:
- `com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity`
- `com.ibpms.poc.infrastructure.jpa.repository.dmn.DmnModelRepository`
- Any persistence/JPA annotations (e.g. `jakarta.persistence.*`, `org.springframework.data.jpa.*`).
This test must initially fail to compile or fail to execute (observing the red phase of TDD).

### R2. Purify Use Cases and Domain Layers
- Create a pure POJO `DmnModel.java` in `com.ibpms.poc.domain.model` package. It should have the properties: `id`, `xmlContent`, `status`, `name`, `createdAt`, `updatedAt`, `authorJwtHash`, `tenantId`, `chatHistoryJson`, and `isManual`. It must contain no JPA, Hibernate, or Spring persistence annotations.
- Create a domain port interface `DmnModelRepositoryPort.java` in `com.ibpms.poc.domain.port` package defining methods:
  - `Optional<DmnModel> findById(String id)`
  - `DmnModel save(DmnModel dmnModel)`
  - `void delete(DmnModel dmnModel)`
  - `List<DmnModel> findByTenantId(String tenantId)`
  - `List<DmnModel> findByStatusAndUpdatedAtBefore(String status, LocalDateTime cutoff)`
- Refactor `DmnGovernanceUseCase.java` to use `DmnModelRepositoryPort` instead of `DmnModelRepository`, and return/accept `DmnModel` instead of `DmnModelEntity`.
- Remove all infrastructure imports (like `DmnModelEntity` and `DmnModelRepository`) from `DmnGovernanceUseCase.java`.

### R3. Consolidate Infrastructure Adapters and Mappers
- Rename `DmnModelEntity.java` to `DmnModelJpaEntity.java` under `com.ibpms.poc.infrastructure.jpa.entity.dmn`.
- Create `DmnModelMapper.java` under `com.ibpms.poc.infrastructure.jpa.mapper` using MapStruct (`@Mapper(componentModel = "spring")`) to map between `DmnModel` and `DmnModelJpaEntity`.
- Create `DmnModelJpaAdapter.java` under `com.ibpms.poc.infrastructure.adapter` implementing `DmnModelRepositoryPort` and delegating to `DmnModelRepository` (which remains in the infrastructure layer).
- Update `DmnModelRepository` to extend `JpaRepository<DmnModelJpaEntity, String>`.
- Update any other classes that references `DmnModelEntity` to use the appropriate layers, such as `DmnGovernanceController` and unit/integration tests.

## Acceptance Criteria

### Test Phase
- [ ] Running `mvn test -Dtest=DmnArchitectureComplianceTest -pl ibpms-core` fails initially before code refactoring.
- [ ] Running `mvn test -pl ibpms-core` compiles and passes all tests (including the compliance test and all prior integration/unit tests) after the refactoring is done.
- [ ] The Java project compiles without Lombok or MapStruct annotation processor warnings or errors.

### Decoupling Verification
- [ ] `DmnGovernanceUseCase.java` contains no references or imports to any packages inside `com.ibpms.poc.infrastructure.jpa`.
- [ ] `DmnModel.java` has zero annotations from packages `jakarta.persistence` or `org.hibernate`.
- [ ] All database schemas and transactional operations behave exactly as before.

## Follow-up — 2026-06-01T19:58:55Z

El objetivo es alinear la topología visual del menú lateral izquierdo (Sidebar) de la aplicación iBPMS con los cuatro nuevos macro-módulos temáticos (Grupo A al D) especificados en el diseño de enrutamiento, modificando el controlador del backend (`MenuLayoutController.java`) y validando mediante pruebas de integración/unidad en Spring Boot y Vitest.

Working directory: c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform
Integrity mode: development

## Requirements

### R1. Reestructuración del Layout de Menú en el Backend
Modificar la definición del árbol de menús en `MenuLayoutController.java` para que devuelva exactamente los siguientes grupos y elementos basados en la topología de permisos del usuario:
- **Grupo A: Operación Diaria (Buzón y Triaje)**:
  - Elementos: `Portal` (`/`), `Workdesk` (`/workdesk`), `Tablero Kanban` (`/kanban`), `Customer 360` (`/admin/customer360`), `Gestor Proyectos` (`/admin/projects/manager`), `Hub Ágil` (`/admin/projects/agile-hub/:projectId?`), `Triaje` (`/intake-triage`), `Intake Manual` (`/admin/intake`), `BAM Dashboard` (`/admin/analytics/bam`).
- **Grupo B: Gobierno, Seguridad e Incidentes**:
  - Elementos: `Gobernanza de Identidad` (`/admin/security/identity`), `PMO` (`/admin/pmo/settings`), `Configuración Global` (`/admin`).
- **Grupo C: Diseño y Modelado Low-Code**:
  - Elementos: `Modelador BPMN` (`/admin/modeler/bpmn`), `Catálogo Formularios` (`/admin/modeler/forms`), `Diseñador Formularios` (`/admin/modeler/forms/designer`), `DMN Intelligence` (`/admin/modeler/dmn`), `Librería Prompts` (`/ai/prompts`), `Formulario Genérico` (`/admin/generic-form`), `Visual Mapper` (`/admin/integration/mapper`), `Project Builder` (`/admin/project-builder`).
- **Grupo D: Integraciones y Automatización**:
  - Elementos: `Catálogo` (`/admin/integration/catalog`), `Builder` (`/admin/integration/builder`), `DLQ Dashboard` (`/admin/integration/dlq`), `Buzones SAC` (`/admin/mailboxes`), `SGD Bóveda` (`/sgdea/vault`), `Centro Incidentes` (`/admin/incidents`), `Gestión de Instancias` (`/admin/modeler/instances`).

### R2. Asegurar Sincronización en el Frontend
- Modificar el frontend (`MainLayout.vue`, `useMenuStore.ts` y archivos de idioma/i18n) para asegurar que los títulos de los grupos del acordeón se muestren correctamente como "Grupo A: Operación Diaria", "Grupo B: Gobierno, Seguridad e Incidentes", "Grupo C: Diseño y Modelado Low-Code" y "Grupo D: Integraciones y Automatización".
- Mapear correctamente los iconos del frontend para que los acordeones muestren los iconos MDI o Material design correspondientes.

## Acceptance Criteria

### Verificación de Pruebas y Compilación
- [ ] Crear la prueba de integración en el backend `com.ibpms.poc.infrastructure.web.ui.MenuLayoutControllerTest.java` para verificar que el endpoint `GET /api/v1/users/me/menu-layout` retorne la nueva estructura estructurada con los grupos A, B, C y D.
- [ ] Compilar y verificar que las pruebas del backend pasen exitosamente (`mvn clean test`).
- [ ] Compilar y verificar que las pruebas del frontend pasen exitosamente (`npx vitest run`).
- [ ] Garantizar que no se modifiquen aserciones de pruebas históricas (Ley Global 4).
- [ ] Ejecutar `npm run build` en el frontend y corroborar compilación exitosa sin advertencias ni errores.

## Follow-up — 2026-06-02T05:04:10Z

Decouple the "Explorador de procesos" sidebar from the "Welcome Modal" initial load in the BPMN Modeler.

Working directory: c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform
Integrity mode: development

## Requirements

### R1. Remove showCatalog = true on mount
Do not automatically open the "Explorador de procesos" sidebar drawer when mounting BpmnDesigner.vue if processId is not present in the URL query parameters. Only the WelcomeModal should act as the initial gatekeeper.

### R2. Close showCatalog on process creation
Ensure that when a new process is created or selected (such as when completing the wizard in WelcomeModal), the "Explorador de procesos" drawer is explicitly closed or remains closed.

### R3. TDD and Verification
- Ensure that the sidebar does not automatically slide open on initial page load if no process is loaded.
- Verify that selecting a process or creating a new process from the Welcome Modal leaves the user on a clean canvas without the sidebar open.
- Add or update unit tests in BpmnDesigner.spec.ts to reflect the new decoupled lifecycle behavior.
- Ensure all tests pass and npm run build compiles with zero errors.

## Acceptance Criteria

### Decoupled Modeler Interface
- [ ] On mounting BpmnDesigner without processId, the Welcome Modal is displayed and the Process Explorer sidebar remains closed.
- [ ] Selecting a process from the Welcome Modal closes the Modal and loads the process with the sidebar closed.
- [ ] Creating a new process from the Welcome Modal closes the Modal and initializes the template/canvas with the sidebar closed.
- [ ] Clicking the "Explorador de procesos" top toolbar button successfully toggles the sidebar drawer open.

