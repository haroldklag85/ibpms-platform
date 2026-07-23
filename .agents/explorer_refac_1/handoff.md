# Handoff Report — Hexagonal Architecture & DDD Refactoring Exploration (ADR-001)

## 1. Observation
We observed the following state and mappings in the codebase of `ibpms-platform` (backend module `ibpms-core`):

1. **Domain Models**:
   - The 8 target models are located under `com.ibpms.poc.domain.model` (`AllowedDomain`, `OrphanPayload`, `TriageTask`, `WebhookTransaction`) and subpackage `agile` (`AgileProject`, `AgileTask`, `AgileTimebox`, `AgileSlaChangelog`).
   - All 8 models are annotated with JPA annotations (e.g., `@Entity`, `@Table`, `@Column`, `@Id`). For example, in `com.ibpms.poc.domain.model.AllowedDomain`:
     ```java
     @Entity
     @Table(name = "ibpms_webhook_allowed_domains")
     ```
     And in `com.ibpms.poc.domain.model.agile.AgileTask`:
     ```java
     @Entity
     @Table(name = "ibpms_agile_tasks")
     ```
     Including collection table mappings:
     ```java
     @ElementCollection(fetch = FetchType.EAGER)
     @CollectionTable(name = "ibpms_agile_task_assignees", joinColumns = @JoinColumn(name = "task_id"))
     ```
     And lifecycle hooks:
     ```java
     @PrePersist
     protected void onCreate() { ... }
     ```

2. **TriageTaskRepository and Pagination**:
   - `com.ibpms.poc.domain.port.TriageTaskRepository` (lines 4-5, 16):
     ```java
     import org.springframework.data.domain.Page;
     import org.springframework.data.domain.Pageable;
     ...
     Page<TriageTask> findByStatus(String status, Pageable pageable);
     ```
   - Only `com.ibpms.poc.infrastructure.persistence.TriageTaskRepositoryJpa` implements `TriageTaskRepository`.
   - Classes invoking this method: `com.ibpms.poc.application.service.TriageTaskService` (line 28):
     ```java
     return triageTaskRepository.findByStatus(status != null ? status : "PENDING", pageable);
     ```

3. **Adapters & Package Structure**:
   - There are 24 adapter classes located under the package `com.ibpms.poc.infrastructure.adapters` (and its subpackages `external`, `inbound`, `security`, `ui`).
   - A singular package `com.ibpms.poc.infrastructure.adapter` already exists and contains 4 adapter files.
   - Searches across `src/main` confirm that no other classes (services, configuration, etc.) import `com.ibpms.poc.infrastructure.adapters.*` directly; they are only imported in test classes:
     - `src/test/java/com/ibpms/poc/application/service/security/DeploymentGovernanceIntegrationAdversarialTest.java`
     - `src/test/java/com/ibpms/poc/application/service/security/DeploymentGovernanceIntegrationTest.java`
     and the unit/integration tests for the adapters themselves under `src/test/java/com/ibpms/poc/infrastructure/adapters/`.

4. **Task Draft Controllers & Legacy Duplication**:
   - **`TaskDraftController.java`**: Located in `com.ibpms.poc.api.controller`, mapped to:
     ```java
     @RequestMapping("/api/v1/workbox/tasks/{taskId}/draft")
     ```
     and uses `com.ibpms.poc.domain.port.TaskDraftRepository` (interacting with `task_drafts` table).
   - **`TaskDraftApiController.java`**: Located in `com.ibpms.poc.infrastructure.web`, mapped to:
     ```java
     @PostMapping("/drafts/{taskId}")
     ```
     and uses `com.ibpms.poc.application.service.TaskDraftService`, which saves drafts into the `draftPayload` field of `AgileTask` via `AgileTaskPort`.
   - **Duplicate Entity**: `com.ibpms.poc.infrastructure.jpa.entity.TaskDraftEntity` maps to `ibpms_task_draft` table and is queried by `com.ibpms.poc.infrastructure.jpa.repository.TaskDraftRepository`. This entity and repository are completely unused in the main codebase.
   - **Active Entity**: `com.ibpms.poc.infrastructure.jpa.entity.TaskDraftJpaEntity` maps to `task_drafts` table and is used by `FormCompletionService.java` to clean up legacy drafts.

---

## 2. Logic Chain
1. **R1: Domain Purification**: Since domain model classes must not depend on any specific infrastructure frameworks, stripping `@Entity` and all related annotations from the 8 domain models under `com.ibpms.poc.domain.model` is required. To maintain persistence capabilities, equivalent JPA entities (with `JpaEntity` suffix) mapping to the same tables must be created under `com.ibpms.poc.infrastructure.jpa.entity`. Bidirectional MapStruct mappers will reconcile these layers within the persistence adapters.
2. **R2: Decoupling TriageTaskRepository**: The import of Spring Data's `Page` and `Pageable` in the domain interface `TriageTaskRepository` violates hexagonal design principles. By replacing these with a custom pure domain pagination wrapper `DomainPage` and passing page/size integer arguments, we clean up the domain port. The adapter class `TriageTaskRepositoryJpa` will manage the transition from integers to Spring Data `Pageable` and convert the results back.
3. **R3: Adapters Renaming**: Moving all adapters under `com.ibpms.poc.infrastructure.adapters` to the singular `com.ibpms.poc.infrastructure.adapter` package name is straightforward because no production services import adapter classes directly (they inject using ports). The refactoring is limited to changing package headers in the 24 adapter source files, moving them into the target directory, and updating imports in 11 test files.
4. **R4: Drafts API Cleanup**: `TaskDraftController.java` exposes a redundant `/api/v1/workbox/tasks/{taskId}/draft` path. Since `TaskDraftApiController.java` under `/api/v1/drafts/{taskId}` is the primary interactive CQRS API for drafts, deleting `TaskDraftController.java` clears the duplication. We will relocate its `draftRateLimiterBucket` configuration into `TaskDraftApiController.java` to retain throttling features.

---

## 3. Caveats
- **Legacy Draft Table**: `FormCompletionService` still relies on `TaskDraftRepository` to purge drafts from the `task_drafts` table upon submission. Therefore, while `TaskDraftController` (the REST endpoint) is deleted, the underlying repository and JpaEntity (`TaskDraftJpaEntity`) should remain inside the codebase as an internal mechanism.
- **Unused Code Removal**: The unused `TaskDraftEntity` (table `ibpms_task_draft`) and repository `com.ibpms.poc.infrastructure.jpa.repository.TaskDraftRepository` should be deleted to prevent future developer confusion, as they are not part of any active execution path.

---

## 4. Conclusion
The proposed refactoring is highly feasible, clean, and has zero impact on core domain logic. It purifies all domain models, decouples ports from Spring Data framework code, unifies infrastructure package namespaces, and eliminates a redundant REST API. The detailed migration plan is documented in `analysis.md`.

---

## 5. Verification Method
After implementing the proposed refactoring, the following verification process must be conducted:

1. **Clean Compile**:
   Execute the Maven compilation to ensure all classes compile and MapStruct processors generate the required mapper implementations:
   ```cmd
   mvn clean compile
   ```
2. **Tests Execution**:
   Run the unit and integration tests to verify there are no package mismatch regressions, import issues, or mapping failures:
   ```cmd
   mvn test
   ```
3. **Paging Integration Verification**:
   Verify `TriageTaskController` and `TriageTaskService` integrate correctly with the newly refactored `TriageTaskRepository` port by checking that the integration tests run and pass without modification.
4. **Redundant Endpoint Removal**:
   Verify that endpoint `GET /api/v1/workbox/tasks/{taskId}/draft` returns `404 Not Found` or `405 Method Not Allowed`, and `GET /api/v1/drafts/{taskId}` works correctly.
5. **No Database Annotations in Domain**:
   Verify that files in `com.ibpms.poc.domain.model` (and subpackages) do not contain any `jakarta.persistence` imports or Spring Data annotations.
