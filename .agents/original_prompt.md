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
