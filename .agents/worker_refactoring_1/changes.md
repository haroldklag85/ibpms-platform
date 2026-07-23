# Changes

// @Traceability: US-003 - ADR-001

## R1. Domain Model Purification (Pure POJOs)
- **Purification of Domain Models**: Purified `AgileSlaChangelog.java` under `com.ibpms.poc.domain.model.agile` of all JPA/Hibernate annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@PrePersist`).
- **Creation of JpaEntity**: Created `AgileSlaChangelogJpaEntity.java` in `com.ibpms.poc.infrastructure.jpa.entity.agile` containing the exact JPA/Hibernate annotations and hooks from the original model.
- **Creation of MapStruct Mapper**: Created MapStruct mapper interface `AgileSlaChangelogMapper.java` in `com.ibpms.poc.infrastructure.jpa.mapper.agile` to translate bidirectionally.
- **Refactoring of Repository**: Updated repository adapter `AgileSlaChangelogRepositoryJpa` in `com.ibpms.poc.infrastructure.persistence` to interact with Spring Data using the new JPA Entity, converting records to and from Domain POJOs using the mapper.

## R2. Port Decoupling
- **DomainPage record**: Defined `DomainPage<T>` in `com.ibpms.poc.domain.model` as a pure POJO representation of paginated data.
- **Port decoupled**: Modified the signature of `TriageTaskRepository.findByStatus` to receive primitives `page` and `size` and return `DomainPage<TriageTask>`, eliminating Spring Data Page/Pageable dependencies.
- **Adapters adjusted**:
  - Refactored `TriageTaskRepositoryJpa` to map `page` and `size` inputs to Spring Data `PageRequest.of(page, size)` and convert the returned `Page<TriageTaskJpaEntity>` to `DomainPage<TriageTask>`.
  - Refactored `TriageTaskService` to wrap the returned `DomainPage<TriageTask>` back into Spring Data `PageImpl<TriageTask>` before returning to preserve the REST web contracts.

## R3. Adapter Package Consolidation
- **Consolidation**: Relocated 24 adapter classes from `com.ibpms.poc.infrastructure.adapters` (and subpackages) to the singular `com.ibpms.poc.infrastructure.adapter` package namespace.
- **Consolidation of Tests**: Relocated 9 test adapter classes to the corresponding singular package.
- **Import/Package Updates**: Updated package declarations and imports across the entire project codebase.
- **Clean up**: Deleted the empty plural `adapters` folders.

## R4. Controller Consolidation
- **Redundancy removal**: Deleted duplicate REST controller `TaskDraftController.java` to prevent routing conflicts.
- **Rate limiting added**: Wired `Bucket draftRateLimiterBucket` into `TaskDraftApiController.java` and applied a `.tryConsume(1)` limit check on POST, GET, and DELETE draft endpoints.
- **Legacy files cleanup**: Removed legacy unused files `TaskDraftEntity.java` and `TaskDraftRepository.java`.

## Traceability Header
- Prepended `// @Traceability: US-003 - ADR-001` to all created or modified files.
