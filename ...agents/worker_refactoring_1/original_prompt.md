## 2026-05-29T21:45:42Z
You are the Worker. Your working directory is c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_refactoring_1.
Your task is to implement the backend refactoring for requirements R1, R2, R3, and R4 as analyzed and described in:
- `c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\explorer_refac_2\analysis.md`
- `c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\explorer_refac_1\analysis.md`

Please follow these instructions:

### MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

### R1. Domain Model Purification (Pure POJOs)
1. Remove all JPA and Hibernate persistence annotations (including `jakarta.persistence.*`, `@JdbcTypeCode`, `@CollectionTable`, `@ElementCollection`, `@PrePersist`, `@PreUpdate` hooks, etc.) from these domain models under `com.ibpms.poc.domain.model`:
   - `AllowedDomain.java`
   - `OrphanPayload.java`
   - `TriageTask.java`
   - `WebhookTransaction.java`
   - `agile/AgileProject.java`
   - `agile/AgileTask.java`
   - `agile/AgileTimebox.java`
   - `agile/AgileSlaChangelog.java`
   Maintain only clean Lombok decorators (like `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Data`).
2. Create corresponding JPA Entities with the suffix `JpaEntity` in `com.ibpms.poc.infrastructure.jpa.entity` (and subpackages if necessary) that map to the exact same tables, columns, constraints, element collections, and hooks as the original domain entities.
3. Create MapStruct mappers in `com.ibpms.poc.infrastructure.jpa.mapper` annotated with `@Mapper(componentModel = "spring")` to map bidirectionally between the Domain POJOs and JPA Entities.
4. Refactor all repositories (e.g. `AllowedDomainRepositoryJpa.java`, `OrphanPayloadRepositoryJpa.java`, `WebhookTransactionRepositoryJpa.java`, `AgileProjectRepositoryJpa.java`, `AgileTaskRepositoryJpa.java`, `AgileTimeboxRepositoryJpa.java`, `AgileSlaChangelogRepositoryJpa.java`) to interact with Spring Data using the new JPA Entities, translating them to/from Domain models using the MapStruct mappers.

### R2. Port Decoupling
1. Define a pure domain pagination record `DomainPage<T>` in `com.ibpms.poc.domain.model` to wrap paginated query results without Spring Data dependencies.
2. Modify the port `TriageTaskRepository.java` signature for `findByStatus` to remove Spring Data `Page` and `Pageable`, replacing them with primitives:
   `DomainPage<TriageTask> findByStatus(String status, int page, int size);`
3. Refactor the implementation `TriageTaskRepositoryJpa.java` to map `page` and `size` to Spring Data `PageRequest.of(page, size)` and convert the returned `Page<TriageTaskJpaEntity>` to `DomainPage<TriageTask>` using the MapStruct mapper.
4. Update `TriageTaskService.java` to use the updated port signature, and convert the returned `DomainPage<TriageTask>` back to Spring Data `PageImpl<TriageTask>` before returning (to avoid breaking the web layer REST contract).

### R3. Adapter Package Consolidation
1. Consolidate infrastructure adapters by moving them from the plural `com.ibpms.poc.infrastructure.adapters` package to the singular `com.ibpms.poc.infrastructure.adapter` package namespace.
2. Update the package declarations and imports across the entire project (including both main classes and test files) to reflect this renaming.

### R4. Controller Consolidation
1. Delete `TaskDraftController.java` to eliminate the duplicate `/api/v1/workbox/tasks/{taskId}/draft` endpoint.
2. In `TaskDraftApiController.java`, autowire `Bucket draftRateLimiterBucket` and add rate-limiting checks (using `.tryConsume(1)`) on the `/api/v1/drafts/{taskId}` endpoints.
3. Clean up legacy unused draft entities/repositories (`TaskDraftEntity.java`, `TaskDraftRepository.java` (in `com.ibpms.poc.infrastructure.jpa.repository`), etc.) as identified in the analysis reports.

### Traceability
Add `// @Traceability: US-003 - ADR-001` to all created or modified files.

### Verification
1. Run `mvn clean compile` in the backend directory (`backend/ibpms-core`) to verify compilation.
2. Run `mvn test` in the backend directory to ensure all tests pass.
3. Document your changes and verification results in `changes.md` and write a structured `handoff.md`.
4. When complete, send a message to the orchestrator (b340978d-141d-4e11-a85f-c47b7d945b0a) indicating your completion.
