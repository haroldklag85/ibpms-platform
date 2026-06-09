# Analysis Report — Purificación de Modelos, Desacoplamiento de Puertos y Consolidación de Adaptadores

## Core Summary
This report analyzes the technical debt and architectural refactoring requirements for pure domain models (ADR-001) in `ibpms-platform`. It details the JPA mappings of the 8 domain models, the decoupling strategy for `TriageTaskRepository`'s pagination, the package consolidation for infrastructure adapters, the cleanup of the duplicate drafts API, and provides a precise list of files to modify, create, or delete.

---

## 1. Domain Models & JPA Mappings Analysis

The following 8 models under `com.ibpms.poc.domain.model` currently mix domain logic with infrastructure persistence annotations (`jakarta.persistence.*`, Hibernate-specific `@JdbcTypeCode`, `@CollectionTable`, `@ElementCollection`, `@PrePersist`, `@PreUpdate` hooks). Under the target design, these models will become pure Java POJOs, and their mappings will be moved to new corresponding `*JpaEntity` classes in `com.ibpms.poc.infrastructure.jpa.entity`.

### Table & Field Mappings

#### 1. AllowedDomain (`AllowedDomain.java`)
- **Table Name**: `ibpms_webhook_allowed_domains`
- **Fields**:
  - `id` (UUID): `@Id`, `@Column(name = "id", updatable = false, nullable = false)`
  - `domain` (String): `@Column(name = "domain", nullable = false)`
  - `tenantId` (String): `@Column(name = "tenant_id", nullable = false)`
  - `description` (String): `@Column(name = "description")`
  - `createdBy` (String): `@Column(name = "created_by", nullable = false)`
  - `createdAt` (ZonedDateTime): `@Column(name = "created_at", nullable = false, updatable = false)`
  - `isActive` (Boolean): `@Column(name = "is_active", nullable = false)`

#### 2. OrphanPayload (`OrphanPayload.java`)
- **Table Name**: `ibpms_orphan_payloads`
- **Fields**:
  - `id` (UUID): `@Id`, `@Column(name = "id", updatable = false, nullable = false)`
  - `rawPayload` (String): `@Column(name = "raw_payload", columnDefinition = "jsonb")`, `@JdbcTypeCode(SqlTypes.JSON)`
  - `errorType` (String): `@Column(name = "error_type", nullable = false)`
  - `fileHashSha256` (String): `@Column(name = "file_hash_sha256")`
  - `fileName` (String): `@Column(name = "file_name")`
  - `fileSizeBytes` (Long): `@Column(name = "file_size_bytes")`
  - `senderEmail` (String): `@Column(name = "sender_email")`
  - `createdAt` (ZonedDateTime): `@Column(name = "created_at", nullable = false, updatable = false)`

#### 3. TriageTask (`TriageTask.java`)
- **Table Name**: `ibpms_triage_tasks`
- **Fields**:
  - `id` (UUID): `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`
  - `camundaProcessInstanceId` (String): `@Column(name = "camunda_process_instance_id", nullable = false)`
  - `messageId` (String): `@Column(name = "message_id", nullable = false)`
  - `senderEmail` (String): `@Column(name = "sender_email", nullable = false)`
  - `subject` (String): `@Column(name = "subject")`
  - `attachmentCount` (Integer): `@Column(name = "attachment_count")`
  - `status` (String): `@Column(name = "status", nullable = false)`
  - `rejectionReason` (String): `@Column(name = "rejection_reason")`
  - `slaDeadline` (ZonedDateTime): `@Column(name = "sla_deadline", nullable = false)`
  - `scanStatus` (String): `@Column(name = "scan_status")`
  - `fileSha256Hash` (String): `@Column(name = "file_sha256_hash")`
  - `createdAt` (ZonedDateTime): `@Column(name = "created_at", nullable = false)`
  - `updatedAt` (ZonedDateTime): `@Column(name = "updated_at", nullable = false)`
- **Hooks to Move**: `@PrePersist` (`onCreate`) and `@PreUpdate` (`onUpdate`) lifecycle methods must be moved to `TriageTaskJpaEntity`.

#### 4. WebhookTransaction (`WebhookTransaction.java`)
- **Table Name**: `ibpms_webhook_transactions`
- **Fields**:
  - `id` (UUID): `@Id`, `@Column(name = "id", updatable = false, nullable = false)`
  - `messageId` (String): `@Column(name = "message_id", nullable = false, unique = true)`
  - `senderEmail` (String): `@Column(name = "sender_email", nullable = false)`
  - `senderDomain` (String): `@Column(name = "sender_domain", nullable = false)`
  - `subject` (String): `@Column(name = "subject")`
  - `payloadHash` (String): `@Column(name = "payload_hash")`
  - `status` (String): `@Column(name = "status", nullable = false)`
  - `rejectionReason` (String): `@Column(name = "rejection_reason")`
  - `camundaProcessInstanceId` (String): `@Column(name = "camunda_process_instance_id")`
  - `createdAt` (ZonedDateTime): `@Column(name = "created_at", nullable = false, updatable = false)`

#### 5. AgileProject (`AgileProject.java`)
- **Table Name**: `ibpms_agile_projects`
- **Fields**:
  - `id` (UUID): `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`
  - `name` (String): `@Column(name = "name", nullable = false)`
  - `description` (String): `@Column(name = "description")`
  - `methodology` (String): `@Column(name = "methodology", nullable = false)`
  - `status` (String): `@Column(name = "status", nullable = false)`
  - `createdBy` (String): `@Column(name = "created_by", nullable = false)`
  - `createdAt` (ZonedDateTime): `@Column(name = "created_at", nullable = false)`
  - `closedAt` (ZonedDateTime): `@Column(name = "closed_at")`
  - `closedBy` (String): `@Column(name = "closed_by")`
  - `maxActiveTasks` (Integer): `@Column(name = "max_active_tasks", nullable = false)`
- **Hooks to Move**: `@PrePersist` (`onCreate`) lifecycle method must be moved to `AgileProjectJpaEntity`.

#### 6. AgileTask (`AgileTask.java`)
- **Table Name**: `ibpms_agile_tasks`
- **Fields**:
  - `id` (UUID): `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`
  - `projectId` (UUID): `@Column(name = "project_id", nullable = false)`
  - `teamId` (String): `@Column(name = "team_id")`
  - `title` (String): `@Column(name = "title", nullable = false)`
  - `description` (String): `@Column(name = "description")`
  - `effortEstimated` (BigDecimal): `@Column(name = "effort_estimated")`
  - `effortActual` (BigDecimal): `@Column(name = "effort_actual")`
  - `notes` (String): `@Column(name = "notes")`
  - `status` (String): `@Column(name = "status", nullable = false)`
  - `position` (Integer): `@Column(name = "position", nullable = false)`
  - `draftPayload` (String): `@Column(name = "draft_payload", columnDefinition = "TEXT")`
  - `draftPayloadHash` (String): `@Column(name = "draft_payload_hash", length = 64)`
  - `draftExpiresAt` (ZonedDateTime): `@Column(name = "draft_expires_at")`
  - `slaDeadline` (ZonedDateTime): `@Column(name = "sla_deadline")`
  - `lastActivityAt` (ZonedDateTime): `@Column(name = "last_activity_at", nullable = false)`
  - `timeoutExtensions` (Integer): `@Column(name = "timeout_extensions")`
  - `createdBy` (String): `@Column(name = "created_by", nullable = false)`
  - `createdAt` (ZonedDateTime): `@Column(name = "created_at", nullable = false)`
  - `updatedAt` (ZonedDateTime): `@Column(name = "updated_at", nullable = false)`
  - `assigneeIds` (Set<String>): `@ElementCollection(fetch = FetchType.EAGER)`, `@CollectionTable(name = "ibpms_agile_task_assignees", joinColumns = @JoinColumn(name = "task_id"))`, `@Column(name = "user_id")`
  - `tags` (Set<String>): `@ElementCollection(fetch = FetchType.EAGER)`, `@CollectionTable(name = "ibpms_agile_task_tags", joinColumns = @JoinColumn(name = "task_id"))`, `@Column(name = "tag")`
- **Hooks to Move**: `@PrePersist` (`onCreate`) and `@PreUpdate` (`onUpdate`) lifecycle methods must be moved to `AgileTaskJpaEntity`.

#### 7. AgileTimebox (`AgileTimebox.java`)
- **Table Name**: `ibpms_agile_timeboxes`
- **Fields**:
  - `id` (UUID): `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`
  - `projectId` (UUID): `@Column(name = "project_id", nullable = false)`
  - `name` (String): `@Column(name = "name", nullable = false, length = 150)`
  - `goal` (String): `@Column(name = "goal", length = 500)`
  - `startDate` (LocalDate): `@Column(name = "start_date", nullable = false)`
  - `endDate` (LocalDate): `@Column(name = "end_date", nullable = false)`
  - `status` (String): `@Column(name = "status", nullable = false, length = 30)`
  - `createdBy` (String): `@Column(name = "created_by", nullable = false, length = 100)`
  - `createdAt` (ZonedDateTime): `@Column(name = "created_at", nullable = false, updatable = false)`
- **Hooks to Move**: `@PrePersist` (`onCreate`) lifecycle method must be moved to `AgileTimeboxJpaEntity`.

#### 8. AgileSlaChangelog (`AgileSlaChangelog.java`)
- **Table Name**: `ibpms_agile_sla_changelog`
- **Fields**:
  - `id` (UUID): `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`
  - `taskId` (UUID): `@Column(name = "task_id", nullable = false)`
  - `previousValue` (ZonedDateTime): `@Column(name = "previous_value")`
  - `newValue` (ZonedDateTime): `@Column(name = "new_value")`
  - `changedBy` (String): `@Column(name = "changed_by", nullable = false)`
  - `changedAt` (ZonedDateTime): `@Column(name = "changed_at", nullable = false)`
- **Hooks to Move**: `@PrePersist` (`onCreate`) lifecycle method must be moved to `AgileSlaChangelogJpaEntity`.

---

## 2. Decoupling of TriageTaskRepository & Pagination

### The Dependency Coupling
The port `TriageTaskRepository` (defined in the domain layer) imports and uses:
- `org.springframework.data.domain.Page`
- `org.springframework.data.domain.Pageable`

This violates the purity of the Domain Layer (Hexagonal Core).

### Refactoring Strategy
1. **Define a Pure Domain Pagination Wrapper**:
   Create a generic domain pagination wrapper under `com.ibpms.poc.domain.model.DomainPage`:
   ```java
   package com.ibpms.poc.domain.model;

   import java.util.List;

   public class DomainPage<T> {
       private final List<T> content;
       private final long totalElements;
       private final int pageNumber;
       private final int pageSize;

       public DomainPage(List<T> content, long totalElements, int pageNumber, int pageSize) {
           this.content = content;
           this.totalElements = totalElements;
           this.pageNumber = pageNumber;
           this.pageSize = pageSize;
       }

       public List<T> getContent() { return content; }
       public long getTotalElements() { return totalElements; }
       public int getPageNumber() { return pageNumber; }
       public int getPageSize() { return pageSize; }
       public int getTotalPages() {
           return pageSize == 0 ? 1 : (int) Math.ceil((double) totalElements / (double) pageSize);
       }
   }
   ```

2. **Modify the Port Interface Signature**:
   In `TriageTaskRepository.java`, remove Spring Data imports and redefine the pagination method:
   ```java
   // Before
   Page<TriageTask> findByStatus(String status, Pageable pageable);

   // After
   DomainPage<TriageTask> findByStatus(String status, int page, int size);
   ```

3. **Convert Page/Pageable inside the Adapter**:
   In the infrastructure class `TriageTaskRepositoryJpa.java`, convert the integer parameters into Spring Data's `Pageable`, query the Spring Data repository (which manages `TriageTaskJpaEntity`), and map the returned `Page<TriageTaskJpaEntity>` back to the pure domain `DomainPage<TriageTask>`:
   ```java
   @Override
   public DomainPage<TriageTask> findByStatus(String status, int page, int size) {
       Pageable pageable = PageRequest.of(page, size);
       Page<TriageTaskJpaEntity> jpaPage = springDataRepo.findByStatus(status, pageable);
       List<TriageTask> content = jpaPage.getContent().stream()
               .map(mapper::toDomain)
               .toList();
       return new DomainPage<>(content, jpaPage.getTotalElements(), page, size);
   }
   ```

4. **Service & Controller Layers**:
   - `TriageTaskService.java` (application layer): Call the port using `pageable.getPageNumber()` and `pageable.getPageSize()`. Map the returned `DomainPage<TriageTask>` back to Spring Data's `PageImpl<TriageTask>` before returning, so that the REST contract/controller signatures do not break:
     ```java
     public Page<TriageTask> listTasks(String status, Pageable pageable) {
         DomainPage<TriageTask> domainPage = triageTaskRepository.findByStatus(
                 status != null ? status : "PENDING",
                 pageable.getPageNumber(),
                 pageable.getPageSize()
         );
         return new PageImpl<>(domainPage.getContent(), pageable, domainPage.getTotalElements());
     }
     ```
   - `TriageTaskController.java` (web layer) remains completely untouched since it uses the standard Spring Data pagination serialization.

---

## 3. Consolidation of Adapters and Package Renaming

All adapters inside `com.ibpms.poc.infrastructure.adapters` must be merged into the singular namespace `com.ibpms.poc.infrastructure.adapter`.

### Target Files to Move and Re-package
The following files must have their package declaration updated to `com.ibpms.poc.infrastructure.adapter` (plus subpackages) and moved:
1. `BpmnAuditJpaAdapter.java`
2. `BpmnDesignJpaAdapter.java`
3. `CamundaBpmnValidationAdapter.java`
4. `CamundaEngineAdapter.java`
5. `CamundaGenericTaskAdapter.java`
6. `CamundaTaskQueryAdapter.java`
7. `DataMappingJpaAdapter.java`
8. `DeployRequestJpaAdapter.java`
9. `ExternalTaskTopicJpaAdapter.java`
10. `FeatureToggleJpaAdapter.java`
11. `FormDefinitionFullAdapter.java`
12. `FormDesignJpaAdapter.java`
13. `ProcessDesignJpaAdapter.java`
14. `ProcessLockJpaAdapter.java`
15. `SecurityRoleJpaAdapter.java`
16. `TaskSkipJpaAdapter.java`
17. `WorkdeskProjectionJpaAdapter.java`
18. `external/MsGraphWebClientAdapter.java` (to `com.ibpms.poc.infrastructure.adapter.external`)
19. `external/SharePointAdapterService.java` (to `com.ibpms.poc.infrastructure.adapter.external`)
20. `inbound/messaging/WebhookIntakeConsumer.java` (to `com.ibpms.poc.infrastructure.adapter.inbound.messaging`)
21. `security/ImpersonationJpaAdapter.java` (to `com.ibpms.poc.infrastructure.adapter.security`)
22. `security/RoleHierarchyJpaAdapter.java` (to `com.ibpms.poc.infrastructure.adapter.security`)
23. `security/UserJpaAdapter.java` (to `com.ibpms.poc.infrastructure.adapter.security`)
24. `ui/MenuTopologyJpaAdapter.java` (to `com.ibpms.poc.infrastructure.adapter.ui`)

### Affected Test Files (Package Renaming & Move)
1. `src/test/java/com/ibpms/poc/infrastructure/adapters/BpmnAuditJpaAdapterTest.java`
2. `src/test/java/com/ibpms/poc/infrastructure/adapters/BpmnGatewayConvergenceGovernanceCA27Test.java`
3. `src/test/java/com/ibpms/poc/infrastructure/adapters/BpmnInfiniteLoopGovernanceCA23Test.java`
4. `src/test/java/com/ibpms/poc/infrastructure/adapters/BpmnStructuralGovernanceCA09Test.java`
5. `src/test/java/com/ibpms/poc/infrastructure/adapters/BpmnZombieNodeGovernanceCA22Test.java`
6. `src/test/java/com/ibpms/poc/infrastructure/adapters/DataMappingJpaAdapterTest.java`
7. `src/test/java/com/ibpms/poc/infrastructure/adapters/DmnBindingValidationTest.java`
8. `src/test/java/com/ibpms/poc/infrastructure/adapters/ProcessLockJpaAdapterTest.java`
9. `src/test/java/com/ibpms/poc/infrastructure/adapters/SecurityRoleJpaAdapterTest.java`

### Files Requiring Import Updates
The following integration test classes import `com.ibpms.poc.infrastructure.adapters.CamundaBpmnValidationAdapter` and will need to update their imports:
1. `src/test/java/com/ibpms/poc/application/service/security/DeploymentGovernanceIntegrationAdversarialTest.java`
2. `src/test/java/com/ibpms/poc/application/service/security/DeploymentGovernanceIntegrationTest.java`

*Note: Production services do not import these adapters directly because they consume them via domain/application port interfaces (e.g. `BpmnDesignPort`, `TaskQueryPort`), keeping services decoupled from the adapter implementations.*

---

## 4. Task Draft Controllers & Redundancy Analysis

### Naming Collisions & Duplicate Logic
The codebase contains a major naming collision and redundant code in draft persistence:
1. **Unused Table & Entity**:
   - `TaskDraftEntity.java` maps to table `ibpms_task_draft` and repository `com.ibpms.poc.infrastructure.jpa.repository.TaskDraftRepository`.
   - **Crucial Discovery**: This entity and repository are completely unused by both the production backend and test code.
2. **Active Table & Entity**:
   - `TaskDraftJpaEntity.java` maps to table `task_drafts` and repository `com.ibpms.poc.domain.port.TaskDraftRepository`.
   - Used by `FormCompletionService.java` to clean up form drafts upon submission.
3. **Duplicate Controllers**:
   - `TaskDraftController.java` (to be deleted): Exposes `/api/v1/workbox/tasks/{taskId}/draft`. It rate-limits and persists drafts into the `task_drafts` table using `TaskDraftRepository`.
   - `TaskDraftApiController.java` (to be kept/consolidated): Exposes `/api/v1/drafts/{taskId}`. It persists drafts inside the fields of the `AgileTask` model (e.g., `draftPayload` and `draftExpiresAt`) via `TaskDraftService` and `AgileTaskPort`.

### Consolidating Endpoint to `/api/v1/drafts/{taskId}`
1. Delete `TaskDraftController.java` completely, which removes `/api/v1/workbox/tasks/{taskId}/draft`.
2. Move the rate limiting configuration (using `draftRateLimiterBucket`) into `TaskDraftApiController.java` if strict API throttling is needed for draft autosaves:
   - Inject the `Bucket` dependency in `TaskDraftApiController.java`.
   - Add the `.tryConsume(1)` check in the controller endpoints.
3. Since `FormCompletionService` is the service that cleans up drafts from the `task_drafts` table, we can keep `TaskDraftRepository`, `TaskDraftRepositoryJpa` and `TaskDraftJpaEntity` as internal persistence mechanisms to clear legacy wizard-state drafts, but the REST API for interactive draft saving/retrieval will now be unified under the interactive `/api/v1/drafts/{taskId}` (managed by `TaskDraftService` inside `AgileTask`).

---

## 5. Refactoring Plan (Proposed Mapping & Classes)

### MapStruct Mappers
Create a mapper package `com.ibpms.poc.infrastructure.jpa.mapper` with the following MapStruct interfaces:
- `AllowedDomainMapper.java`
- `OrphanPayloadMapper.java`
- `TriageTaskMapper.java`
- `WebhookTransactionMapper.java`
- `AgileProjectMapper.java`
- `AgileTaskMapper.java`
- `AgileTimeboxMapper.java`
- `AgileSlaChangelogMapper.java`

*Example configuration for MapStruct mapping (AllowedDomain):*
```java
package com.ibpms.poc.infrastructure.jpa.mapper;

import com.ibpms.poc.domain.model.AllowedDomain;
import com.ibpms.poc.infrastructure.jpa.entity.AllowedDomainJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AllowedDomainMapper {
    AllowedDomainMapper INSTANCE = Mappers.getMapper(AllowedDomainMapper.class);

    AllowedDomain toDomain(AllowedDomainJpaEntity entity);
    AllowedDomainJpaEntity toEntity(AllowedDomain domain);
}
```

### Exact List of File Changes

#### 📂 Files to CREATE:
1. `com.ibpms.poc.domain.model.DomainPage.java` — pure pagination abstraction.
2. `com.ibpms.poc.infrastructure.jpa.entity.AllowedDomainJpaEntity.java`
3. `com.ibpms.poc.infrastructure.jpa.entity.OrphanPayloadJpaEntity.java`
4. `com.ibpms.poc.infrastructure.jpa.entity.TriageTaskJpaEntity.java` (including `@PrePersist` and `@PreUpdate` hooks)
5. `com.ibpms.poc.infrastructure.jpa.entity.WebhookTransactionJpaEntity.java`
6. `com.ibpms.poc.infrastructure.jpa.entity.AgileProjectJpaEntity.java` (including `@PrePersist` hook)
7. `com.ibpms.poc.infrastructure.jpa.entity.AgileTaskJpaEntity.java` (including `@PrePersist` and `@PreUpdate` hooks, collections mapping)
8. `com.ibpms.poc.infrastructure.jpa.entity.AgileTimeboxJpaEntity.java` (including `@PrePersist` hook)
9. `com.ibpms.poc.infrastructure.jpa.entity.AgileSlaChangelogJpaEntity.java` (including `@PrePersist` hook)
10. `com.ibpms.poc.infrastructure.jpa.mapper.*` — 8 new MapStruct mapper interfaces.

#### 📝 Files to MODIFY:
1. `com.ibpms.poc.domain.model.AllowedDomain.java` — strip JPA annotations.
2. `com.ibpms.poc.domain.model.OrphanPayload.java` — strip JPA annotations.
3. `com.ibpms.poc.domain.model.TriageTask.java` — strip JPA annotations and hooks.
4. `com.ibpms.poc.domain.model.WebhookTransaction.java` — strip JPA annotations.
5. `com.ibpms.poc.domain.model.agile.AgileProject.java` — strip JPA annotations and hooks.
6. `com.ibpms.poc.domain.model.agile.AgileTask.java` — strip JPA annotations, collections mapping, and hooks.
7. `com.ibpms.poc.domain.model.agile.AgileTimebox.java` — strip JPA annotations and hooks.
8. `com.ibpms.poc.domain.model.agile.AgileSlaChangelog.java` — strip JPA annotations and hooks.
9. `com.ibpms.poc.domain.port.TriageTaskRepository.java` — change signature of `findByStatus` using `DomainPage` and remove Spring Data imports.
10. `com.ibpms.poc.infrastructure.persistence.TriageTaskRepositoryJpa.java` — update to use `TriageTaskJpaEntity`, map queries, and construct `DomainPage`.
11. `com.ibpms.poc.application.service.TriageTaskService.java` — convert pagination request to page/size int and convert `DomainPage` response back to Spring Data `Page`.
12. `com.ibpms.poc.infrastructure.persistence.AllowedDomainRepositoryJpa.java` — update to use `AllowedDomainJpaEntity` and perform mapping.
13. `com.ibpms.poc.infrastructure.persistence.OrphanPayloadRepositoryJpa.java` — update to use `OrphanPayloadJpaEntity` and perform mapping.
14. `com.ibpms.poc.infrastructure.persistence.WebhookTransactionRepositoryJpa.java` — update to use `WebhookTransactionJpaEntity` and perform mapping.
15. `com.ibpms.poc.infrastructure.persistence.AgileProjectRepositoryJpa.java` — update to use `AgileProjectJpaEntity` and perform mapping.
16. `com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa.java` — update to use `AgileTaskJpaEntity` and perform mapping.
17. `com.ibpms.poc.infrastructure.persistence.AgileTimeboxRepositoryJpa.java` — refactor from interface to class wrapping Spring Data interface, using `AgileTimeboxJpaEntity` and performing mapping.
18. `com.ibpms.poc.infrastructure.persistence.AgileSlaChangelogRepositoryJpa.java` — update to use `AgileSlaChangelogJpaEntity` and perform mapping.
19. `com.ibpms.poc.infrastructure.web.TaskDraftApiController.java` — integrate rate limiting using `draftRateLimiterBucket` from deleted `TaskDraftController`.
20. Move the 24 adapter classes from `com.ibpms.poc.infrastructure.adapters` to `com.ibpms.poc.infrastructure.adapter` (with subpackages) and update their package declarations.
21. Move the 9 adapter test classes in `src/test` and update package declarations.
22. Update `DeploymentGovernanceIntegrationTest.java` and `DeploymentGovernanceIntegrationAdversarialTest.java` imports for `CamundaBpmnValidationAdapter`.

#### 🗑️ Files to DELETE:
1. `com.ibpms.poc.api.controller.TaskDraftController.java` — completely redundant REST controller.
2. `com.ibpms.poc.infrastructure.jpa.entity.TaskDraftEntity.java` — unused legacy entity causing collision.
3. `com.ibpms.poc.infrastructure.jpa.repository.TaskDraftRepository.java` — unused legacy repository interface.
4. Old `com.ibpms.poc.infrastructure.adapters` directory after all classes are moved and verified.
