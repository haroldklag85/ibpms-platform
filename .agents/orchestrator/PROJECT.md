# Project: DMN Governance Hexagonal Architecture and DDD Refactoring (US-007 - ADR-001)

## Architecture
- **Domain Layer**: Contains pure POJOs (`com.ibpms.poc.domain.model`) and interfaces/ports (`com.ibpms.poc.domain.port`). It is completely clean of any infrastructure-specific concepts such as Spring Data pagination, JPA/Hibernate mapping annotations, and Web controller decorators.
- **Application Layer**: Contains business logic, orchestrators, and services. Uses domain models and ports.
- **Infrastructure Layer**: Contains persistence implementations (Spring Data repositories, MapStruct mappers, JPA Entities under `com.ibpms.poc.infrastructure.jpa`) and Web controllers. Adapters are consolidated under `com.ibpms.poc.infrastructure.adapter`.

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Compliance Test (Red Phase) | Implement failing `DmnArchitectureComplianceTest.java` in `com.ibpms.poc.application.usecase.dmn`. Verify it fails with `mvn test -Dtest=DmnArchitectureComplianceTest -pl ibpms-core`. | none | DONE |
| 2 | Domain Layer Purification | Create `DmnModel.java` (pure POJO) and `DmnModelRepositoryPort.java` in domain layers. | M1 | DONE |
| 3 | Use Case Refactoring | Refactor `DmnGovernanceUseCase.java` to use the repository port and pure POJO, removing JPA/infrastructure imports. | M2 | DONE |
| 4 | Infrastructure Adapters & Mappers | Rename `DmnModelEntity.java` to `DmnModelJpaEntity.java`. Create `DmnModelMapper.java` (MapStruct) and `DmnModelJpaAdapter.java`. Update `DmnModelRepository.java`. | M3 | DONE |
| 5 | Web & Test Integration | Update `DmnGovernanceController` and other classes/tests referencing `DmnModelEntity` to use the appropriate layers/entities. | M4 | DONE |
| 6 | Verification & Compliance (Green Phase) | Compile and run all tests (`mvn test -pl ibpms-core`). Verify the compliance test passes. Run Forensic Auditor. | M5 | IN_PROGRESS (Worker 8 verifying) |

## Interface Contracts
### MapStruct Mappers
- Mappers must map bidirectionally between domain model POJOs and JPA Entities (with `JpaEntity` suffix) inside `com.ibpms.poc.infrastructure.jpa.mapper`.
- Mappers should use the component model `spring` (`@Mapper(componentModel = "spring")`).

### DmnModelRepositoryPort
- Domain Port signature:
  - `Optional<DmnModel> findById(String id);`
  - `DmnModel save(DmnModel dmnModel);`
  - `void delete(DmnModel dmnModel);`
  - `List<DmnModel> findByTenantId(String tenantId);`
  - `List<DmnModel> findByStatusAndUpdatedAtBefore(String status, LocalDateTime cutoff);`

### DmnModelJpaAdapter
- Infrastructure Adapter implements `DmnModelRepositoryPort` and delegates to `DmnModelRepository` (which extends `JpaRepository<DmnModelJpaEntity, String>`). Maps entities to domain models using `DmnModelMapper`.

## Code Layout
- Domain Models: `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model`
- Domain Ports: `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/port`
- JPA Entities: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/dmn`
- MapStruct Mappers: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper`
- Adapters: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter`
- Web Controllers: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/dmn`
