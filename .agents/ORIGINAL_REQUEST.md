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

## Follow-up — 2026-06-06T19:18:24Z

El proyecto consiste en rediseñar y reconstruir la barra de herramientas (Toolbar) y la interfaz visual de la funcionalidad "Validar y simular" (US-005) en la aplicación iBPMS, cambiando de un modal emergente (popup) a un panel lateral (canvas lateral) resizable y organizando los botones superiores en un flujo secuencial continuo de 6 pasos (estilo Stepper). Adicionalmente, se debe corregir el bug en el panel de Historial de Versiones (donde procesos nuevos en borrador muestran versiones ficticias inexistentes v2/v3) y estabilizar los hallazgos de backend (OBS-1 y OBS-2).

Working directory: /home/haroltandrsgmezagu/proyectos/ibpms-platform
Integrity mode: development

## Requirements

### R1. Barra de Herramientas en Stepper Secuencial Glassmorphic
La barra de herramientas superior del modelador se reorganizará visualmente en 6 tarjetas secuenciales ordenadas de izquierda a derecha (estilo Stepper):
- Paso 1: Biblioteca (Explorador de procesos, Importar, Exportar).
- Paso 2: Modelado (Canvas + Consultar Copiloto IA).
- Paso 3: Simulación (Validar y Simular, Limpiar Trayectoria).
- Paso 4: Trazabilidad (Auditoría, Versiones).
- Paso 5: Despliegue (Solicitar Despliegue, Solicitudes en solo lectura para diseñadores, [VALIDAR Y DESPLEGAR]).
- Paso 6: Operación (Gestor de Instancias).

Reglas de UI del Stepper:
- Estética Glassmorphism: Fondo translúcido con desenfoque de fondo (backdrop-filter: blur(8px)) y bordes extra-finos semi-transparentes para temas claro/oscuro.
- Resaltado Dinámico: Se destacará visualmente la tarjeta de la fase actual del ciclo de vida en base al estado del proceso (currentVersion === 0 destaca diseño/simulación; currentVersion > 0 destaca fases avanzadas).
- Paso 6 Deshabilitado en v0: El botón "Gestor de Instancias" aparecerá deshabilitado con un tooltip de ayuda contextual: "Esta opción estará disponible al realizar el primer despliegue activo".
- Permisos en Paso 5: Los diseñadores (BPMN_Designer) solo podrán consultar el panel de solicitudes en modo de Solo Lectura (los controles de aprobación/rechazo estarán bloqueados o inactivos).
- Responsividad: Colapsar los grupos en menús desplegables (Dropdowns) o deslizador horizontal en pantallas pequeñas.

### R2. Layout de Panel Lateral Derecho No Bloqueante (Push Layout)
El panel de validación y simulación del Paso 3 debe deslizarse desde la derecha de la pantalla y empujar (encoger) el canvas del modelador BPMN en lugar de superponerse. El lienzo y sus elementos permanecen editables e interactivos para el usuario con el panel abierto.
- La lógica de arrastre (resizing) del panel lateral debe implementarse utilizando eventos nativos de ratón (mousedown, mousemove, mouseup) en Vue 3 para evitar conflictos con los componentes de bpmn-js.
- El panel de propiedades nativo de bpmn-js se ocultará automáticamente al abrirse y se restaurará al cerrarse.

### R3. Organización en Acordeón Vertical en Simulación
Las tres fases de validación (Linter Local, Pre-Flight Analyzer y Sandbox Simulator) se presentarán en secciones colapsables (acordeón vertical) dentro del panel lateral de simulación.

### R4. Simulación Interactiva con Trazado en Caliente
La simulación interactiva del sandbox debe ejecutarse en caliente, dibujando halos verdes animados (highlight-executed) sobre el canvas del modelador en tiempo real (nodo por nodo) a medida que avanza. Al cerrar el panel o hacer clic en "Limpiar trayectoria", los halos deben removerse.
- Se incluirá una grilla interactiva en la sección del simulador para que el usuario pueda ver, editar y eliminar valores de variables acumuladas en localStorage antes de lanzar el test.

### R5. Detección y Corrección de Bug del Historial de Versiones (Backend y Frontend)
- Backend: Modificar la ruta /api/v1/design/processes/{processDefinitionKey}/versions in BpmnDesignController.java para capturar IllegalArgumentException (que ocurre cuando un proceso en borrador no existe aún en la base de datos) y retornar HTTP 200 con una lista vacía List.of().
- Alineación de Contrato: Enriquecer el DTO de respuesta del endpoint de versiones del backend con las llaves que el frontend renderiza: version, date (o updatedAt), author (o createdBy), y status, además de las claves existentes.
- Frontend: Modificar la función fetchVersions() en BpmnDesigner.vue para que, en caso de error, limpie la variable versionHistory asignándole un array vacío [] en lugar de cargar el fallback mock de Ana/Carlos. Mapear las propiedades del JSON del backend correspondientemente.

### R6. Estabilización de Backend (OBS-1 y OBS-2)
- Modificar DataMappingIntegrityTest.java para que herede de AbstractIntegrationTest (que utiliza ddl-auto=validate junto con Liquibase), eliminando cualquier DDL espurio inyectado manualmente en la prueba.
- Completar las anotaciones OpenAPI (Swagger) en BpmnDesignController.java para los endpoints /deploy y /validate, asegurando la correcta documentación del Response Body (e.g. 201 Created con ids y timestamps) y sus parámetros de entrada.
- Alinear la llamada de validateProcess en useIntegrationStore.ts del frontend para que envíe un objeto FormData con formato multipart/form-data conteniendo el XML en el parámetro file.

## Acceptance Criteria

### Rediseño de Barra de Herramientas Stepper (UI)
- [ ] La barra de herramientas superior se organiza en un Stepper de 6 grupos secuenciales con diseño Glassmorphic.
- [ ] La tarjeta de la fase actual del proceso se resalta visualmente según el estado.
- [ ] En un proceso nuevo en borrador (v0), el Paso 6 (Gestor de Instancias) está deshabilitado en gris con un tooltip explicativo.
- [ ] Un usuario con rol BPMN_Designer puede ver el panel de solicitudes del Paso 5 en modo solo lectura, con los botones de aprobación/rechazo bloqueados.
- [ ] La barra es responsiva y colapsa los grupos en dropdowns colapsables en pantallas pequeñas.

### Corrección del Historial de Versiones (Bug-Fix)
- [ ] El endpoint de versiones de backend retorna 200 OK con un array vacío [] para procesos no persistidos.
- [ ] El endpoint de versiones de backend retorna las llaves mapeadas version, date, author y status.
- [ ] Al hacer clic en el botón de Versiones para un proceso nuevo en borrador, el panel muestra un estado vacío ("No hay versiones publicadas aún") y ya no despliega las versiones mock v2/v3 de ejemplo.

### Interfaz del Panel Lateral de Simulación (Frontend)
- [ ] El botón superior "Validar y simular" abre/cierra (comportamiento toggle) el panel lateral derecho.
- [ ] El panel se puede redimensionar arrastrando su borde izquierdo en un rango de 400px a 700px.
- [ ] Las tres secciones colapsables (Linter, Pre-Flight, Simulator) funcionan como acordeón vertical.
- [ ] El panel de propiedades nativo de bpmn-js se oculta de forma limpia al abrirse el panel de simulación y se restaura al cerrarse.
- [ ] Los halos verdes brillantes se van dibujando dinámicamente en el canvas nodo por nodo en caliente durante la simulación y se limpian al presionar "Limpiar trayectoria".

### Estabilización y Calidad
- [ ] Las llamadas de validación del frontend al backend no fallan con HTTP 400 y transmiten el payload como multipart/form-data.
- [ ] El test de persistencia del backend corre con éxito bajo el esquema Liquibase real (ddl-auto=validate).
- [ ] El backend cuenta con OpenAPI Swagger docs completos para los endpoints de despliegue y validación BPMN.
- [ ] Todos los tests de regresión del backend (mvn clean test -Dtest=DataMappingIntegrityTest,BpmnDeployContractTest,SandboxGovernanceTest) y el build del frontend (npm run build) compilan y finalizan con éxito.

## Follow-up — 2026-06-07T05:43:00Z

El proyecto consiste en completar el desarrollo y estabilización de la historia **US-005 (BPMN Modeler - Rediseño de Barra de Herramientas, Paneles Laterales y Corrección de Bugs)**, y resolver dos bugs de estabilidad críticos detectados en la plataforma iBPMS (V1), aplicando de forma estricta el patrón TDD (Test-Driven Development) y directrices de inmutabilidad de regresiones.

**Working directory:** Y:\home\haroltandrsgmezagu\proyectos\ibpms-platform
**Integrity mode:** development

---

## 📋 Contexto y Reglas de Contexto de Sesión
- **Exclusividad de Alcance**: Durante esta sesión queda establecido de forma estricta que solo se trabajará sobre el alcance de la **US-005** y los bugs asociados. Queda prohibido desviar la atención a otras historias o introducir alucinaciones de código fuera del alcance directo.
- **Leyes de Gobernanza**: Cumplir rigurosamente las directrices de `.cursorrules`, en especial:
    - *Ley Global 1*: Iniciar siempre los mensajes con el Collar de Identidad correspondiente.
    - *Ley Global 2*: Prohibición absoluta de `git stash` (usar `git commit` y `git push` en ramas de sprint).
    - *Ley Global 3*: Trazabilidad Inversa en código mediante anotaciones `@Traceability` o comentarios `// @Traceability: US-005, CA-XX`.
- **Handoff Quality Standard (HQS)**: Todo handoff técnico generado entre subagentes debe seguir estrictamente la plantilla de 7 secciones del archivo `.agents/skills/handoff_quality_standard/SKILL.md`. Cada subagente debe definir explícitamente las habilidades (`skills`) a emplear para su entrega.
- **Skills de Subagentes Mandatarios**: En los handoffs y ejecuciones técnicas de los subagentes, es obligatorio invocar y utilizar las siguientes habilidades:
    - `addyosmani_sre_discipline` (Disciplina SRE, Zero-Mock y validación estricta)
    - `addyosmani_planning` (Estructura de planificación fina)
    - `addyosmani_code_review` (Auditoría de código de doble ojo)
    - `yudhi_architecture_compliance` (Cumplimiento de arquitectura hexagonal y ADRs)
    - `yudhi_database_migrations` (Validaciones transaccionales y de Liquibase sin DDLs espurios)

---

## 🔬 Diagnóstico de Bugs a Resolver

### Bug 1: El auto-guardado no funciona y el lienzo se pierde tras inactividad
- **Causa Raíz**: 
    1. En `BpmnDesigner.vue` (`onMounted`), no se inicia el motor de tiempo `timeStore.startEngine()`, manteniendo congelada la variable `timeStore.currentTick` y paralizando el temporizador de auto-guardado y los heartbeats del candado.
    2. Al crear o cargar un proceso, no se sincroniza reactivamente el parámetro `processId` en la URL de navegación (`router.replace`), provocando que cualquier remontaje del componente Vue redirija erróneamente al Welcome Modal.
    3. Al expirar el candado pesimista en el servidor por falta de latidos, no existe un mecanismo visual claro para renovarlo.
- **Solución Requerida**: 
    - Iniciar y detener el motor de tiempo con `timeStore.startEngine()` and `timeStore.stopEngine()` en los ganchos correspondientes.
    - Sincronizar reactivamente el query parameter `processId` en la URL.
    - Mostrar un banner no bloqueante de advertencia cuando el candado expire por inactividad, con un botón interactivo para renovarlo en un solo clic.

### Bug 2: Error "Error cargando el XML del proceso" al seleccionar un Draft en recientes
- **Causa Raíz**:
    1. Cuando el frontend solicita el XML de un proceso draft recién creado (cuyo XML no ha sido persistido o es nulo en base de datos), el servicio en backend lanza un `IllegalArgumentException` no controlado en el controlador.
    2. Esto devuelve un error HTTP 500/400 que rompe la carga del lienzo y dispara un toast rojo.
- **Solución Requerida**:
    - Modificar `getProcessXml` en `BpmnDesignController.java` para capturar `IllegalArgumentException` y retornar un estado `200 OK` con un XML BPMN vacío con estructura básica por defecto.
    - Si el borrador seleccionado no tiene XML en base de datos, el frontend debe inicializar el lienzo con dicha plantilla vacía (evento de inicio básico) asociada a su ID técnico.

---

## 🎯 Requerimientos y Decisiones de Rediseño (Alineados en `/grill-me`)

### R1. Barra de Herramientas en Stepper Secuencial
La barra superior se organiza en un Stepper de 6 pasos de izquierda a derecha:
- **Paso 1: Inicio** (antiguo Biblioteca). Contiene el explorador de procesos, importar, exportar y el botón manual **"Guardar"** como control secundario.
- **Paso 2: Modelado** (Canvas + Consultar Copiloto IA).
- **Paso 3: Simulación** (Validar y Simular, Limpiar Trayectoria).
- **Paso 4: Trazabilidad** (Historial de Versiones y Auditoría de Cambios unificados).
- **Paso 5: Despliegue** (Solicitar Despliegue, lista de solicitudes en modo Solo Lectura para roles `BPMN_Designer`).
- **Paso 6: Operación** (Gestor de Instancias).
- **Detalle Estético**: Fondo translúcido con desenfoque de fondo (Glassmorphism: `backdrop-filter: blur(8px)`) y bordes semi-transparentes finos.
- **Etiquetas de Estado**: Los elementos visuales `"BORRADOR"` y `"SANDBOX"` deben rediseñarse como badges planos, redondeados, de solo lectura, con **tonos grises suaves** y tooltip explicativo al pasar el cursor (sin efectos hover de botón ni invitaciones a cliquear).
- **Control de Versión**: La cabecera mostrará la versión de iBPMS (`v0`, `v1`, `v2`...) vinculada al `Version Tag` de Camunda (inicializado en `"1"` para procesos nuevos).
- **Paso 6 Deshabilitado**: En procesos en borrador (`v0`), el Paso 6 debe mostrarse grisáceo y deshabilitado con un tooltip aclarativo.

### R2. Cajón Lateral Derecho Unificado (Trazabilidad Drawer)
- El historial de versiones y la bitácora de auditoría se unificarán en un **panel lateral derecho deslizable (Drawer) no bloqueante** que empuje y reduzca el lienzo de forma adaptativa.
- El panel se compone de dos pestañas: "Historial de Versiones" y "Auditoría de Cambios", detallando metadatos (versión, fecha/hora, autor, estado).
- Al abrirse, debe ocultar de forma limpia el panel de propiedades nativo de bpmn-js y restaurarlo al cerrarse.
- En procesos en borrador (`v0`), el historial de versiones mostrará un estado vacío descriptivo: *"No hay versiones publicadas aún"*.
- Las acciones de despliegue o rechazo en la auditoría requerirán un comentario de justificación mínimo obligatorio de 20 caracteres.

### R3. Notificaciones y Sincronización
- En caso de fallas de red o errores de persistencia en el auto-guardado en segundo plano, se notificará al usuario mediante un **Toast temporal de 5 segundos** que se desvanece solo (evitando banners persistentes intrusivos).
- El estado del guardado se reflejará discretamente en la barra de estado superior ("Guardado hace x segundos", "Validado").

---

## 📋 Estrategia de Pruebas y TDD (Gobernanza de Regresión)

Se debe seguir estrictamente la estrategia de **Test-Driven Development (TDD)**:
1. **Paso Inicial**: Antes de escribir o modificar código, localiza o crea las pruebas unitarias/integración correspondientes a los cambios.
2. **Ejecutar Pruebas (Fallo Inicial)**: Ejecuta las pruebas para confirmar que fallan (luz roja).
3. **Desarrollar**: Implementa el código mínimo necesario para solucionar los bugs y cumplir el diseño.
4. **Ejecutar Pruebas (Éxito final)**: Ejecuta las pruebas nuevamente para validar el paso a verde.
5. **Inmutabilidad de Regresión**: Está prohibido alterar aserciones o casos de prueba preexistentes de sprints anteriores con el fin de forzar luz verde. Si fallan, se corrige el código del negocio, no los tests.

### Comandos de Ejecución de Pruebas

#### A. Backend (JUnit e Integración)
Asegurar que la base de datos estática esté activa en los puertos indicados por `application-test.yml`.
```bash
mvn -f backend/pom.xml test -pl ibpms-core -Dtest=BpmnDeployContractTest,DataMappingIntegrityTest,SandboxGovernanceTest
```

#### B. Frontend (Playwright E2E y CT con GPU Acelerada en WSL2)
Correr las pruebas forzando el uso de la GPU local (NVIDIA RTX 5080) a través de `PLAYWRIGHT_USE_GPU=true`:
- **Certificación E2E**:
  ```bash
  PLAYWRIGHT_USE_GPU=true npx playwright test --config=playwright.e2e.config.ts
  ```
- **Component Testing (CT)**:
  ```bash
  PLAYWRIGHT_USE_GPU=true npm run test:ct
  ```
- **Build de Verificación**:
  ```bash
  npm run --prefix frontend build
  ```

---

## 🏁 Acceptance Criteria (DoD)

### UI Barra de Herramientas & Stepper
- [ ] El Paso 1 se llama visualmente "Inicio" y contiene el botón "Guardar" manual como control secundario.
- [ ] Las etiquetas "BORRADOR" y "SANDBOX" son badges planos grises suaves de solo lectura con tooltips informativos.
- [ ] El Paso 6 ("Operación") se muestra grisáceo y deshabilitado para procesos en `v0`.
- [ ] La cabecera muestra la versión del historial de iBPMS (secuencial v0, v1, v2...) y se sincroniza con el Tag de Camunda inicializado en "1".
- [ ] La barra de herramientas aplica efectos de Glassmorphic (fondo translúcido con blur y bordes finos).

### Cajón Deslizable de Trazabilidad
- [ ] Al pulsar "Versiones" o "Auditoría" se abre un cajón lateral derecho no bloqueante que empuje/reduce el canvas BPMN.
- [ ] El cajón oculta el panel de propiedades nativo de Camunda al abrirse y lo restaura al cerrarse.
- [ ] El cajón tiene pestañas para "Historial de Versiones" y "Auditoría de Cambios".
- [ ] Para un proceso nuevo en borrador (v0), el historial de versiones muestra el estado vacío "No hay versiones publicadas aún".

### Estabilidad y Calidad de Código
- [ ] El backend maneja `IllegalArgumentException` en el endpoint `/xml` y devuelve el XML por defecto con HTTP 200.
- [ ] El motor de ticks de `timeStore` se inicia y detiene adecuadamente en `BpmnDesigner.vue` (`startEngine` y `stopEngine`).
- [ ] La URL se mantiene sincronizada con el query parameter `processId` al crear o cargar procesos.
- [ ] Existe un banner no bloqueante en caso de pérdida/expiración de candado para re-adquirir.
- [ ] Los errores de red en el auto-guardado se reportan con un Toast temporal de 5 segundos.
- [ ] Los tests de regresión del backend compilan y finalizan con éxito.
- [ ] El build de frontend (`npm run build`) y las pruebas CT/E2E compilan y pasan en verde con la aceleración por GPU.

## Follow-up — 2026-06-09T18:55:35Z

El usuario ha otorgado la autorización explícita de desarrollo para la US-005. Revive el flujo de trabajo, levanta los subagentes necesarios y finaliza la implementación completa del Stepper superior, el panel deslizable derecho de trazabilidad (drawer con pestañas de versiones y auditoría) y las correcciones asociadas al Bug 1 y Bug 2. Asegúrate de compilar y correr todas las pruebas (Vitest, JUnit y Playwright E2E con GPU) confirmando que pasan en verde.

## Follow-up — 2026-06-10T00:34:28Z

Fix welcome modal and tech ID misalignment in BPMN Modeler (US-005) when loading process with typographic name-key mismatch.

Working directory: z:/home/haroltandrsgmezagu/proyectos/ibpms-platform
Integrity mode: development

Note: The regression tests have already been written in BpmnDesigner.spec.ts and their initial failure has been verified. The detailed handoff with architectural rules and surgical instructions is located at:
.agentic-sync/handoff_US005_bug_guardado.md

Please follow the handoff instructions strictly to apply the fix, run the tests to verify they pass (green light), run the production build, and commit the changes conventions-style to the sprint branch.

## Requirements

### R1. Technical ID Immutability (processId)
The technical ID of an existing process must remain unchanged after loading. Modify the watcher of `currentProcessName` in `BpmnDesigner.vue` to only generate the ID technical slug if the process is completely new (v0). Prevent overwriting the technical ID during process load.

### R2. Test-Driven Development (TDD)
Write unit test cases in `BpmnDesigner.spec.ts` that verify:
1. Changing the business name on a new process (v0) generates the slug ID.
2. Loading an existing process (v1+) keeps the technical ID unchanged, even if it does not match the name.
Verify that tests fail initially, then pass after implementation.

## Acceptance Criteria

### TDD & Stability
- [ ] Test cases verifying technical ID immutability are written in BpmnDesigner.spec.ts.
- [ ] Tests fail when executed before fixing the code.
- [ ] Tests pass (green light) after the fix is implemented.
- [ ] Vitest test suite runs successfully with `npm run test:unit` inside WSL.
- [ ] Production build compiles cleanly with `npm run build` inside WSL.
- [ ] Conventional Git commit is pushed to Sprint branch.

Required Skills to apply:
- addyosmani_sre_discipline
- addyosmani_planning
- addyosmani_code_review
- yudhi_architecture_compliance
- yudhi_database_migrations

## Follow-up — 2026-06-10T04:06:06Z

Implement process version tag auto-suggestion, SemVer validation in Pre-Flight, and timeline version fallback correction (US-005).

Working directory: z:/home/haroltandrsgmezagu/proyectos/ibpms-platform
Integrity mode: development

## Requirements

### R1. Timeline Version Fallback Correction
In `BpmnDesigner.vue` timeline log rendering, replace the logical OR fallback (`|| 1`) with a nullish/exact check so that version `0` (drafts) is correctly rendered as `v0` instead of being coerced to `v1`. This applies to the version badge and the click-to-restore action.

### R2. Version Tag Auto-Suggestion (Frontend)
In `BpmnDesigner.vue`, when a new process is created or loaded (where `currentVersion` is `0` or no `camunda:versionTag` exists yet), the field `processVersionTag` must be automatically suggested and set to `"1.0.0"`, and the XML root property updated.

### R3. SemVer Validation in Pre-Flight Analyzer (Backend)
In `CamundaBpmnValidationAdapter.java`, extract `camunda:versionTag` from the process definition during both draft XML validation (`validateDraftXml`) and deployment stream validation (`validateBpmnStream`). Validate that the tag is not empty and complies with SemVer format (`^[0-9]+\.[0-9]+\.[0-9]+.*$`). Reject the validation/deployment with an HTTP 422 error if it is invalid.

### R4. Test-Driven Development (TDD)
- Frontend: Write unit tests in `BpmnDesigner.spec.ts` verifying that `v0` is correctly rendered in the timeline log when `version = 0`, and that `"1.0.0"` is auto-suggested for new processes.
- Backend: Write unit tests in `BpmnVersionTagValidationTest.java` verifying valid, invalid, and missing version tags.
- Verify tests fail before implementing logic and pass after.

## Acceptance Criteria

### Modeler UI
- [ ] Timeline log renders `v0` if log version is `0`.
- [ ] Timeline log click-to-restore restores version `0` if log version is `0`.
- [ ] Version tag is auto-suggested to `"1.0.0"` when creating or loading a new process (v0).
- [ ] Vitest unit tests in `BpmnDesigner.spec.ts` pass in WSL.

### Backend Validation
- [ ] Pre-flight analyzer rejects XML with invalid version tags (e.g. `"v1.0"`, `"abc"`, empty/blank).
- [ ] Pre-flight analyzer accepts XML with valid SemVer version tags (e.g. `"1.0.0"`, `"2.0.1-SNAPSHOT"`).
- [ ] JUnit tests in `BpmnVersionTagValidationTest.java` pass.

### Build & Delivery
- [ ] Maven test completes successfully with `-Djacoco.skip=true`.
- [ ] Frontend production build compiles cleanly (`npm run build`) in WSL.
- [ ] Changes are committed and pushed to `sprint-6` branch without using `git stash`.


