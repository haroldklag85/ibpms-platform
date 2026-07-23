# Handoff Report: Backend Refactoring Analysis

This report is produced by Explorer 3 and provides a self-contained, verified analysis and refactoring plan for requirements R1, R2, R3, and R4 of the `ibpms-platform` project.

---

## 1. Observation

During our investigation of the `ibpms-platform` codebase, we observed the following:

### A. Domain Models with JPA Annotations (R1)
We inspected the domain models under `com.ibpms.poc.domain.model` and confirmed that the following 8 models contain direct jakarta persistence (JPA) and Hibernate annotations:
1.  **`AllowedDomain.java`**
    *   Path: `src/main/java/com/ibpms/poc/domain/model/AllowedDomain.java`
    *   Annotations: `@Entity`, `@Table(name = "ibpms_webhook_allowed_domains")` (line 11-12), `@Id` (line 19), `@Column` (lines 20, 23, 26, 29, 32, 35, 38).
2.  **`OrphanPayload.java`**
    *   Path: `src/main/java/com/ibpms/poc/domain/model/OrphanPayload.java`
    *   Annotations: `@Entity`, `@Table(name = "ibpms_orphan_payloads")` (line 13-14), `@Id` (line 21), `@JdbcTypeCode(SqlTypes.JSON)` (line 25), `@Column` (lines 22, 26, 29, 32, 35, 38, 41, 44).
3.  **`TriageTask.java`**
    *   Path: `src/main/java/com/ibpms/poc/domain/model/TriageTask.java`
    *   Annotations: `@Entity`, `@Table(name = "ibpms_triage_tasks")` (line 8-9), `@Id` (line 17), `@GeneratedValue` (line 18), `@Column` (lines 21, 24, 27, 30, 33, 36, 39, 42, 45, 48, 51, 55), `@PrePersist` (line 57), `@PreUpdate` (line 66).
4.  **`WebhookTransaction.java`**
    *   Path: `src/main/java/com/ibpms/poc/domain/model/WebhookTransaction.java`
    *   Annotations: `@Entity`, `@Table(name = "ibpms_webhook_transactions")` (line 12-13), `@Id` (line 20), `@Column` (lines 21, 24, 27, 30, 33, 36, 39, 42, 45, 48).
5.  **`agile/AgileProject.java`**
    *   Path: `src/main/java/com/ibpms/poc/domain/model/agile/AgileProject.java`
    *   Annotations: `@Entity`, `@Table(name = "ibpms_agile_projects")` (line 8-9), `@Id` (line 17), `@GeneratedValue` (line 18), `@Column` (lines 21, 24, 27, 30, 33, 36, 39, 42, 45), `@PrePersist` (line 48).
6.  **`agile/AgileSlaChangelog.java`**
    *   Path: `src/main/java/com/ibpms/poc/domain/model/agile/AgileSlaChangelog.java`
    *   Annotations: `@Entity`, `@Table(name = "ibpms_agile_sla_changelog")` (line 9-10), `@Id` (line 18), `@GeneratedValue` (line 19), `@Column` (lines 22, 25, 28, 31, 34), `@PrePersist` (line 37).
7.  **`agile/AgileTimebox.java`**
    *   Path: `src/main/java/com/ibpms/poc/domain/model/agile/AgileTimebox.java`
    *   Annotations: `@Entity`, `@Table(name = "ibpms_agile_timeboxes")` (line 13-14), `@Id` (line 22), `@GeneratedValue` (line 23), `@Column` (lines 26, 29, 32, 35, 38, 41, 44, 47), `@PrePersist` (line 50).
8.  **`agile/AgileTask.java`**
    *   Path: `src/main/java/com/ibpms/poc/domain/model/agile/AgileTask.java`
    *   Annotations: `@Entity`, `@Table(name = "ibpms_agile_tasks")` (line 10-11), `@Id` (line 19), `@GeneratedValue` (line 20), `@Column` (lines 23, 26, 29, 32, 35, 38, 41, 44, 47, 50, 53, 56, 59, 62, 65, 68, 71, 74), `@ElementCollection` (lines 77, 82), `@CollectionTable` (lines 78, 83), `@PrePersist` (line 87), `@PreUpdate` (line 96).

### B. Ports Leaking Spring Data Classes (R2)
We inspected the `TriageTaskRepository.java` port and verified it references Spring Data pagination classes:
*   Path: `src/main/java/com/ibpms/poc/domain/port/TriageTaskRepository.java`
*   Imports (lines 4-5):
    `import org.springframework.data.domain.Page;`
    `import org.springframework.data.domain.Pageable;`
*   Signature (line 15):
    `Page<TriageTask> findByStatus(String status, Pageable pageable);`
*   Implementations:
    *   `src/main/java/com/ibpms/poc/infrastructure/persistence/TriageTaskRepositoryJpa.java` (lines 21-53)
*   Callers:
    *   `src/main/java/com/ibpms/poc/application/service/TriageTaskService.java` (lines 27-29)
    *   `src/main/java/com/ibpms/poc/infrastructure/web/TriageTaskController.java` (lines 29-33)

### C. Adapters Package to Rename (R3)
We confirmed the package directory `src/main/java/com/ibpms/poc/infrastructure/adapters` contains exactly 24 files:
*   `BpmnAuditJpaAdapter.java`, `BpmnDesignJpaAdapter.java`, `CamundaBpmnValidationAdapter.java`, `CamundaEngineAdapter.java`, `CamundaGenericTaskAdapter.java`, `CamundaTaskQueryAdapter.java`, `DataMappingJpaAdapter.java`, `DeployRequestJpaAdapter.java`, `ExternalTaskTopicJpaAdapter.java`, `FeatureToggleJpaAdapter.java`, `FormDefinitionFullAdapter.java`, `FormDesignJpaAdapter.java`, `ProcessDesignJpaAdapter.java`, `ProcessLockJpaAdapter.java`, `SecurityRoleJpaAdapter.java`, `TaskSkipJpaAdapter.java`, `WorkdeskProjectionJpaAdapter.java`.
*   Subfolders: `external/` (2 files), `inbound/messaging/` (1 file), `security/` (3 files), `ui/` (1 file).
*   Test folder: `src/test/java/com/ibpms/poc/infrastructure/adapters` containing 9 test files.
*   Backup test folder: `src/test/java.bak/com/ibpms/poc/infrastructure/adapters` containing 8 test files.
*   External references importing from this package:
    *   `src/test/java/com/ibpms/poc/application/service/security/DeploymentGovernanceIntegrationAdversarialTest.java` (line 4)
    *   `src/test/java/com/ibpms/poc/application/service/security/DeploymentGovernanceIntegrationTest.java` (line 4)

### D. Redundant Controllers & Legacy Files (R4)
We observed two controllers defining routes for task drafts, clashing on `PUT /api/v1/workbox/tasks/{taskId}/draft`:
*   `TaskDraftController.java` (lines 40-64): `@RequestMapping("/api/v1/workbox/tasks/{taskId}/draft")` on class, `@PutMapping` on save method.
*   `WorkboxTaskController.java` (lines 202-221): `@RequestMapping("/api/v1/workbox/tasks")` on class, `@PutMapping("/{id}/draft")` on save method.
*   The active CQRS REST endpoint for drafts is `/api/v1/drafts/{taskId}` (mapped in `TaskDraftApiController.java`), routing to `TaskDraftService.java` which stores drafts inside `AgileTask`.
*   We verified the following files are legacy code and not referenced by the active production code:
    *   `TaskDraft.java` (domain model)
    *   `TaskDraftRepository.java` (domain port)
    *   `TaskDraftEntity.java` (infra entity)
    *   `TaskDraftJpaEntity.java` (infra entity)
    *   `TaskDraftRepository.java` (infra spring data repo)
    *   `TaskDraftRepositoryJpa.java` (infra persistence adapter)
    *   `TaskDraftCrudTest.java` (unit test in `src/test` and `src/test.bak`)
    *   `TaskDraftController.java` (infra controller)
*   Frontend apiClient (`frontend/src/services/apiClient.ts`) on line 281 defines `saveTaskDraft` calling `/workbox/tasks/${taskId}/draft` using a PUT request.
*   `frontend/src/stores/useFormStore.ts` calls `api.saveTaskDraft(taskId, formData.value)` on line 76.
*   `frontend/src/tests/stores/useFormStore.spec.ts` mocks `saveTaskDraft` on lines 12, 70, 76.

---

## 2. Logic Chain

1.  **DDD Isolation (R1):** Per DDD patterns and hexagonal guidelines, the domain model layer must not reference database framework decorators (jakarta/JPA). Therefore, these 8 domain files must be purified. To maintain database mapping, 8 counterpart `JpaEntity` classes will be created under the infrastructure layer (`com.ibpms.poc.infrastructure.jpa.entity`). MapStruct mappers (`com.ibpms.poc.infrastructure.jpa.mapper`) will translate between the domain models and the infrastructure entities.
2.  **Pagination Decoupling (R2):** Since domain ports represent conceptual interfaces, leaking framework classes (`Page` and `Pageable`) creates tight coupling. By introducing custom `DomainPage` and `DomainPageable` in the domain and translating to/from Spring Data types inside the persistence adapter (`TriageTaskRepositoryJpa`), we satisfy the R2 decoupling requirement while maintaining Spring's MVC auto-binding features in the controller.
3.  **Renaming Package (R3):** The package must be renamed from plural `adapters` to singular `adapter`. Moving directories and changing package declarations in all 24 main files, 17 test files, and updating imports in 2 external integration tests will restore compilation.
4.  **Consolidating Draft API (R4):** Since drafts are saved in the `AgileTask` table's `draft_payload` column via `TaskDraftApiController` / `TaskDraftService`, the legacy custom table `task_drafts` and all its 8 associated classes/tests are dead code and should be deleted. Renaming the API route in `apiClient.ts` to `POST /drafts/{taskId}` directly resolves the endpoint collision and integrates the frontend with the active production draft service.

---

## 3. Caveats

*   **Jackson ObjectMapper:** MapStruct mappers for entities with JSON fields (`OrphanPayload`, `TriageTask`, `WebhookTransaction`) must autowire an `ObjectMapper` to handle JSON mapping.
*   **Spring Data binding:** The controller (`TriageTaskController.java`) will continue using Spring Data's `Pageable` in `@GetMapping` parameters so that Spring MVC can automatically bind URL parameters (e.g. `?page=0&size=10`), but it will immediately map it to a `DomainPageable` before invoking the service layer.

---

## 4. Conclusion

The refactoring plan consists of:
1.  **R1:** Strip JPA annotations from `AllowedDomain`, `OrphanPayload`, `TriageTask`, `WebhookTransaction`, `AgileProject`, `AgileSlaChangelog`, `AgileTimebox`, and `AgileTask`. Create equivalent `JpaEntity` classes and MapStruct mapper interfaces for all 8 models.
2.  **R2:** Define `DomainPage` and `DomainPageable` in the domain. Refactor `TriageTaskRepository`, `TriageTaskRepositoryJpa`, `TriageTaskService`, and `TriageTaskController` to use these custom structures.
3.  **R3:** Move `src/main/.../adapters` and `src/test/.../adapters` folders to `adapter`. Update package statements in all 24 source files and 17 test files, plus imports in the 2 deployment governance integration tests.
4.  **R4:** Delete `TaskDraftController.java`, the legacy `TaskDraft` files (8 files in total), and the duplicate `saveDraft` PUT mapping in `WorkboxTaskController.java`. Update the frontend `apiClient.ts` to POST drafts to `/drafts/{taskId}`.
5.  **Traceability:** Ensure all modified or created files include the tag `// @Traceability: US-003 - ADR-001`.

---

## 5. Verification Method

To verify the refactoring independently:
1.  **Compilation & Build:** Run the command:
    `./gradlew build -x test` or `./mvnw clean compile`
    Verification succeeds if the project compiles without errors.
2.  **Tests Execution:** Run the test suite:
    `./gradlew test` or `./mvnw test`
    Specifically check that `TaskDraftIntegrationTest` passes and all renamed adapter tests run successfully.
3.  **Frontend Test Run:** Run the frontend vitest tests:
    `npm run test` or `vitest run` inside the `frontend` folder.
    Specifically check that `useFormStore.spec.ts` passes against the consolidated draft API.
4.  **Static Inspection:** Verify that no file in `com.ibpms.poc.domain` imports `jakarta.persistence` or `org.springframework.data.domain.Pageable` (with exception of controllers/repositories under infrastructure).
