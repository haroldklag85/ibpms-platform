# Implementation Plan - Backend Refactoring (ADR-001 Validation)

This plan details the steps required to eliminate technical debt in the backend code of `ibpms-platform` by purifying the domain models, decoupling the repository ports from Spring Data, unifying the infrastructure adapters namespace, and removing duplicate endpoints.

## Milestones

### Milestone 1: Domain Model Purification
- **Scope**: Cleanse `com.ibpms.poc.domain.model` package (and its `agile` subpackage) from all JPA annotations (`@Entity`, `@Table`, `@Id`, `@Column`, etc.).
- **Entities to Purify**:
  - `AllowedDomain`
  - `OrphanPayload`
  - `TriageTask`
  - `WebhookTransaction`
  - `agile/AgileProject`
  - `agile/AgileTask`
  - `agile/AgileTimebox`
  - `agile/AgileSlaChangelog`
- **Infrastructure Equivalent**: Create corresponding `JpaEntity` classes under `com.ibpms.poc.infrastructure.jpa.entity` mapping to same database schema.
- **Mapping Layer**: Implement MapStruct mappers under `com.ibpms.poc.infrastructure.jpa.mapper` to translate back and forth between JPA entities and Domain POJOs.
- **Traceability**: Add `// @Traceability: US-003 - ADR-001` in all created and modified source files.

### Milestone 2: Port Decoupling
- **Scope**: Remove Spring Data dependency (`Page` and `Pageable`) from `TriageTaskRepository.java`.
- **Modifications**:
  - Update `TriageTaskRepository.java` signature to return `List<TriageTask>` using simple pagination parameters or domain abstractions.
  - Implement conversion to and from Spring Data pagination classes inside the JPA repository implementation.
  - Fix calling services (e.g. `TriageTaskService.java`) to adapt to the new clean signature.
- **Traceability**: Add `// @Traceability: US-003 - ADR-001`.

### Milestone 3: Infrastructure Adapters Package Consolidation
- **Scope**: Rename package `com.ibpms.poc.infrastructure.adapters` to `com.ibpms.poc.infrastructure.adapter`.
- **Modifications**:
  - Move files from `infrastructure/adapters` directory to `infrastructure/adapter` directory.
  - Global import updates across the entire application and tests.
- **Traceability**: Add `// @Traceability: US-003 - ADR-001`.

### Milestone 4: Controller Duplication Removal
- **Scope**: Delete `TaskDraftController.java` from `com.ibpms.poc.api.controller`.
- **Modifications**:
  - Ensure all drafts API paths are fully handled by `TaskDraftApiController.java` under `/api/v1/drafts/{taskId}` (which maps to the exact same logic or is consolidated).
  - Verify that no remaining references to the old endpoint or class exist.
- **Traceability**: Add `// @Traceability: US-003 - ADR-001`.

### Milestone 5: Verification & Testing
- **Scope**: Run complete maven build and test suite, verify compilation and runtime behavior.
- **Commands**:
  - `mvn clean compile`
  - `mvn test`
- **Traceability Verification**: Ensure all modified files contain the required comment block.
