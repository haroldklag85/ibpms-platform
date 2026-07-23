# Handoff Report — Backend Refactoring Analysis (Explorer Refac 2)

## 1. Observation
- Inspected the 8 domain models under `com.ibpms.poc.domain.model` (and `agile` subpackage):
  - `AllowedDomain.java` (table: `ibpms_webhook_allowed_domains`)
  - `OrphanPayload.java` (table: `ibpms_orphan_payloads`)
  - `TriageTask.java` (table: `ibpms_triage_tasks`)
  - `WebhookTransaction.java` (table: `ibpms_webhook_transactions`)
  - `AgileProject.java` (table: `ibpms_agile_projects`)
  - `AgileTask.java` (table: `ibpms_agile_tasks`)
  - `AgileTimebox.java` (table: `ibpms_agile_timeboxes`)
  - `AgileSlaChangelog.java` (table: `ibpms_agile_sla_changelog`)
  All of these files contain JPA and Hibernate annotations (e.g. `@Entity`, `@Table`, `@Column`, etc.) directly on the domain definitions.
- Inspected `TriageTaskRepository.java` (domain port) and found it uses Spring Data's `org.springframework.data.domain.Page` and `Pageable` in:
  `Page<TriageTask> findByStatus(String status, Pageable pageable);`
- Inspected package layout and found adapters are placed in both `com.ibpms.poc.infrastructure.adapters` (plural, 24 adapter classes) and `com.ibpms.poc.infrastructure.jpa.adapter` (singular, 1 adapter class).
- Inspected controllers for task drafts:
  - `TaskDraftController.java` (GET/PUT/DELETE `/api/v1/workbox/tasks/{taskId}/draft`) uses `TaskDraftRepository` to store drafts in the `task_drafts` table and implements a Bucket4j rate limiter.
  - `TaskDraftApiController.java` (GET/POST/DELETE `/api/v1/drafts/{taskId}`) uses `TaskDraftService` to store drafts inside `AgileTask` fields and lacks rate limiting.
- Located duplicate entities: `TaskDraftEntity` and `TaskDraftJpaEntity` in `com.ibpms.poc.infrastructure.jpa.entity`. Only `TaskDraftJpaEntity` is currently mapped and used via `TaskDraftRepositoryJpa`.

## 2. Logic Chain
- To achieve R1 (Domain Model Purification), JPA annotations must be removed from domain classes to make them pure POJOs. This necessitates creating corresponding JpaEntity classes (e.g., `AllowedDomainJpaEntity`, `OrphanPayloadJpaEntity`, etc.) in the infrastructure layer to hold the JPA annotations, and MapStruct mappers (e.g., `AllowedDomainMapper`, etc.) to map bidirectionally between the purified domain models and their JpaEntity counterparts.
- To achieve R2 (Port Decoupling), the domain port `TriageTaskRepository` must not import any Spring Data classes. Introducing a custom domain pagination record `DomainPage` allows the application/domain layers to define pagination cleanly, while the database-specific implementation adapter `TriageTaskRepositoryJpa` converts integers to `PageRequest.of(page, size)` and maps the results back.
- To achieve R3 (Adapter Package Consolidation), the 24 classes in `com.ibpms.poc.infrastructure.adapters` must be moved to `com.ibpms.poc.infrastructure.adapter` to unify the namespaces, and their respective imports/packages updated.
- To achieve R4 (Controller Consolidation), `TaskDraftController` must be deleted, and its rate-limiting logic added to `TaskDraftApiController` for `/api/v1/drafts/{taskId}` endpoints.

## 3. Caveats
- Since this is a read-only investigation, no code modifications have been applied.
- The compile/test command `mvnw verify` must be run by the implementer after applying the proposed changes to ensure MapStruct annotation processors generate the mapping source files and all tests pass.

## 4. Conclusion
The detailed refactoring strategy has been documented in `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/analysis.md` (physically located in `.agents/explorer_refac_2/analysis.md`). Requirements R1, R2, R3, and R4 can be cleanly addressed by:
1. Purifying the 8 domain models, creating corresponding JpaEntities and MapStruct mappers, and updating their JPA repositories.
2. Decoupling pagination in `TriageTaskRepository` via a custom `DomainPage` record.
3. Consolidating the package naming of the adapters to `com.ibpms.poc.infrastructure.adapter`.
4. Merging the draft controllers under `TaskDraftApiController` with rate limiting.

## 5. Verification Method
1. Inspect the generated `analysis.md` file located at `c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\explorer_refac_2\analysis.md`.
2. Review the detailed file listings to modify, create, and delete.
3. After the implementer applies the changes:
   - Run compilation and tests using `mvnw clean test` (or `mvnw verify`) in `backend/ibpms-core` to verify code correctness.
   - Run the integration test `test_us017.js` (and any rate-limiting tests) to verify drafts work as expected.
