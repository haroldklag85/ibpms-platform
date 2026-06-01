# Implementation Plan - DMN Governance Refactoring (ADR-001 Validation)

This plan details the steps required to refactor the DMN governance module of US-007 to comply with ADR-001 (Hexagonal Architecture / DDD), ensuring the domain layer is completely decoupled from the JPA persistence layer using ports and adapters.

## Milestones

### Milestone 1: Implement Failing Architecture Test (TDD Phase 1)
- **Scope**: Ensure that any infrastructure or persistence leaks in the domain layer are caught early.
- **Task**: Modify/create `DmnArchitectureComplianceTest.java` under `src/test/java` in `com.ibpms.poc.application.usecase.dmn` package.
- **Verification**: Run `mvn test -Dtest=DmnArchitectureComplianceTest -pl ibpms-core` and verify it fails due to imports in `DmnGovernanceUseCase.java`.
- **Traceability**: Add `// @Traceability: US-007 - ADR-001` in the header of the test class.

### Milestone 2: Create Domain Models & Ports
- **Scope**: Purify the domain by defining the domain model and its corresponding database repository port.
- **Tasks**:
  - Create `DmnModel.java` as a pure POJO inside `com.ibpms.poc.domain.model` package (without any JPA or Hibernate annotations).
  - Create `DmnModelRepositoryPort.java` inside `com.ibpms.poc.domain.port` package defining required persistence operations.
- **Traceability**: Add `// @Traceability: US-007 - ADR-001` in headers of all created files.

### Milestone 3: Refactor Use Case to Decouple Persistence
- **Scope**: Decouple the `DmnGovernanceUseCase` from direct infrastructure references.
- **Tasks**:
  - Refactor `DmnGovernanceUseCase.java` to use `DmnModelRepositoryPort` instead of `DmnModelRepository`.
  - Replace occurrences of `DmnModelEntity` with the domain model `DmnModel`.
  - Remove all infrastructure/JPA package imports.
- **Traceability**: Add `// @Traceability: US-007 - ADR-001` in headers of modified files.

### Milestone 4: Consolidate Infrastructure Adapters and Mappers
- **Scope**: Implement persistence adapters and MapStruct mappers to bridge the domain-infrastructure gap.
- **Tasks**:
  - Rename `DmnModelEntity.java` to `DmnModelJpaEntity.java` under `com.ibpms.poc.infrastructure.jpa.entity.dmn`.
  - Create `DmnModelMapper.java` under `com.ibpms.poc.infrastructure.jpa.mapper` using MapStruct.
  - Create `DmnModelJpaAdapter.java` under `com.ibpms.poc.infrastructure.adapter` implementing `DmnModelRepositoryPort` and delegating to `DmnModelRepository`.
  - Update `DmnModelRepository` to extend `JpaRepository<DmnModelJpaEntity, String>`.
- **Traceability**: Add `// @Traceability: US-007 - ADR-001` in headers of all created/modified files.

### Milestone 5: Web & Test Integration
- **Scope**: Update web controllers and tests to match the new hexagonal layout.
- **Tasks**:
  - Update `DmnGovernanceController.java` to handle the new return/parameter types from the use case.
  - Update other use cases or controllers referencing `DmnModelEntity` (like `AiDmnGeneratorUseCase`, `DmnSimulatorUseCase`, etc., if applicable).
  - Refactor unit/integration tests (`DmnGovernanceControllerTest.java`, `DmnSimulationIntegrationTest.java`, etc.) to use domain models and correct adapters/mocks.
- **Traceability**: Add `// @Traceability: US-007 - ADR-001` in headers of all modified files.

### Milestone 6: Verification & Compliance (Green Phase)
- **Scope**: Run the test suite and verification tools to ensure that all requirements and constraints are met.
- **Verification**:
  - Run `mvn test -pl ibpms-core` to verify all tests pass.
  - Verify that `DmnArchitectureComplianceTest` passes.
  - Run `teamwork_preview_auditor` to check for integrity and traceability violations.
