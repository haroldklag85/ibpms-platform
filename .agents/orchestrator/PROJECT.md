# Project: Hexagonal Architecture and DDD Refactoring (US-003 - ADR-001)

## Architecture
- **Domain Layer**: Contains pure POJOs (`com.ibpms.poc.domain.model`) and interfaces/ports (`com.ibpms.poc.domain.port`). It is completely clean of any infrastructure-specific concepts such as Spring Data pagination, JPA/Hibernate mapping annotations, and Web controller decorators.
- **Application Layer**: Contains business logic, orchestrators, and services. Uses domain models and ports.
- **Infrastructure Layer**: Contains persistence implementations (Spring Data repositories, MapStruct mappers, JPA Entities under `com.ibpms.poc.infrastructure.jpa`) and Web controllers. Adapters are consolidated under `com.ibpms.poc.infrastructure.adapter`.

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Domain Model Purification | Remove JPA annotations from models under `com.ibpms.poc.domain.model` (and subpackages); recreate JPA Entities and MapStruct Mappers in infrastructure. | none | DONE |
| 2 | Port Decoupling | Decouple `TriageTaskRepository` from Spring Data's `Page` and `Pageable` interfaces. | M1 | DONE |
| 3 | Adapter Package Consolidation | Consolidate packages from `infrastructure.adapters` to `infrastructure.adapter` namespace. | M1, M2 | DONE |
| 4 | Controller Consolidation | Remove `TaskDraftController.java` and consolidate drafts logic under `TaskDraftApiController.java`. | M3 | DONE |
| 5 | Compile & Test Verification | Compile the application and run unit and integration tests. | M4 | IN_PROGRESS |

## Interface Contracts
### MapStruct Mappers
- Mappers must map bidirectionally between domain model POJOs and JPA Entities (with `JpaEntity` suffix) inside `com.ibpms.poc.infrastructure.jpa.mapper`.
- Mappers should use the component model `spring` (`@Mapper(componentModel = "spring")`).

### TriageTaskRepository
- Domain Port signature:
  `List<TriageTask> findPaginated(int page, int size);` (or similar domain-specific pagination parameters).
- Infrastructure Adapter:
  Converts primitive `page` and `size` parameters into Spring Data `PageRequest.of(page, size)` and performs query, mapping results back from `TriageTaskJpaEntity` to `TriageTask`.

### Drafts API Endpoint
- Base URI: `/api/v1/drafts/{taskId}` (mapped to `TaskDraftApiController`).
- Old endpoint `/api/v1/workbox/tasks/{taskId}/draft` mapped to `TaskDraftController` must be deleted completely.

## Code Layout
- Domain: `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model`
- Ports: `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/port`
- JPA Entities: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity`
- MapStruct Mappers: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper`
- Adapters: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter`
- Web Controllers: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web`
