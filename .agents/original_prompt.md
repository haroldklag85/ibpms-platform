## 2026-05-30T00:47:14Z

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

## 2026-05-30T02:37:48Z

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

## 2026-06-01T00:14:49Z

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

## 2026-06-01T19:58:55Z

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

## 2026-06-02T05:04:10Z

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

## 2026-06-02T05:51:01Z

Implement the Glosario de Datos Unificado (Propuesta 2) for the nomenclature rule input field in BpmnDesigner.vue to improve the UX/UI of CA-5 under US-005.

Working directory: c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform
Integrity mode: development

## Requirements

### R1. Glosario de Variables Section & State
Add a new collapsible card section in the BpmnDesigner.vue properties panel (visible when process is selected, alongside Nomenclature Rule) titled "Glosario de Variables de Negocio".
- Allow architects to declare variables manually (e.g. key: `nit_cliente`, type: `Text`), which are persisted to the BPMN process definition XML metadata (custom extension elements).
- Dynamically merge into this list any variables coming from:
  - Linked start forms/user task forms (Form Catalog fields loaded via `fetchForms()`).
  - Webhooks/Connectors active in the process (extracted from topics and mapper variables).
  - Session context pre-defines (`session.user_name`, `session.email`).

### R2. Token Autocomplete using Glosario
Replace the simple nomenclature text input with an interactive autocomplete pill editor.
- When typing `{`, display a popover suggestion list populated dynamically from the unified Glosario de Variables.
- Selecting a variable inserts it as `{glosario.<variable_key>}` (or `{session.user_name}` for system context) in the nomenclature rule structure.
- Tokens should be rendered visually as interactive, color-coded tags (pills) inside the input container.

### R3. Dummies-Tone Explanatory Tooltip
Add a premium explanatory tooltip next to the "Regla de Nomenclatura (CA-5)" label. The tooltip content must be styled beautifully and written in an extremely friendly "dummies-tone" explaining the concept of a shared glossary, how it works in a bidirectional way (whether screens or process is created first), and examples of execution.

### R4. TDD and Verification
- Write new component unit tests in BpmnDesigner.spec.ts under the CA-5 scope to verify:
  - The Glosario de Variables section is rendered and allows adding manual variables.
  - Typing `{` in the nomenclature rule input shows variables from both the manual Glosario, active forms, and session context.
  - The dummies-tone tooltip is present with the correct text.
- Ensure that the entire frontend test suite continues to pass (npx vitest run).
- Ensure that npm run build compiles with zero warnings or errors.

## Acceptance Criteria

### Unified Glossary & Autocomplete
- [ ] The "Glosario de Variables de Negocio" section is visible in the process properties sidebar.
- [ ] Users can manually add variable keys to the glossary, which are successfully saved to the BPMN XML.
- [ ] The autocomplete popover triggers on typing `{` in the nomenclature rule field, displaying the unified variables.
- [ ] The explanatory tooltip button `❓` next to the nomenclature rule label presents a friendly, dummies-tone description of the glossary and examples.
- [ ] The entire Vitest suite passes and the production bundle build succeeds.

## 2026-06-06T19:18:24Z

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

