# Backend Refactoring Analysis Report — R1, R2, R3, R4

This report details the findings and implementation plan for refactoring the `ibpms-platform` backend. The goals align with satisfying requirements R1 (Domain Model Purification), R2 (Port Decoupling), R3 (Adapter Package Consolidation), and R4 (Controller Consolidation) to enforce Clean Architecture and DDD principles.

---

## 1. Domain Models Inspection & Purification Strategy (R1)

The following 8 models under `com.ibpms.poc.domain.model` (and the `agile` subpackage) are currently polluted with JPA annotations. 

### Inspection Findings (Current Mappings)

#### 1. `com.ibpms.poc.domain.model.AllowedDomain`
- **Table**: `ibpms_webhook_allowed_domains`
- **Annotations**: `@Entity`, `@Table(name = "ibpms_webhook_allowed_domains")`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- **Fields & Columns**:
  - `UUID id` -> `@Id`, `@Column(name = "id", updatable = false, nullable = false)`
  - `String domain` -> `@Column(name = "domain", nullable = false)`
  - `String tenantId` -> `@Column(name = "tenant_id", nullable = false)`
  - `String description` -> `@Column(name = "description")`
  - `String createdBy` -> `@Column(name = "created_by", nullable = false)`
  - `ZonedDateTime createdAt` -> `@Column(name = "created_at", nullable = false, updatable = false)`
  - `Boolean isActive` -> `@Column(name = "is_active", nullable = false)`

#### 2. `com.ibpms.poc.domain.model.OrphanPayload`
- **Table**: `ibpms_orphan_payloads`
- **Annotations**: `@Entity`, `@Table(name = "ibpms_orphan_payloads")`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- **Fields & Columns**:
  - `UUID id` -> `@Id`, `@Column(name = "id", updatable = false, nullable = false)`
  - `String rawPayload` -> `@JdbcTypeCode(SqlTypes.JSON)`, `@Column(name = "raw_payload", columnDefinition = "jsonb")`
  - `String errorType` -> `@Column(name = "error_type", nullable = false)`
  - `String fileHashSha256` -> `@Column(name = "file_hash_sha256")`
  - `String fileName` -> `@Column(name = "file_name")`
  - `Long fileSizeBytes` -> `@Column(name = "file_size_bytes")`
  - `String senderEmail` -> `@Column(name = "sender_email")`
  - `ZonedDateTime createdAt` -> `@Column(name = "created_at", nullable = false, updatable = false)`

#### 3. `com.ibpms.poc.domain.model.TriageTask`
- **Table**: `ibpms_triage_tasks`
- **Annotations**: `@Entity`, `@Table(name = "ibpms_triage_tasks")`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@PrePersist`, `@PreUpdate`
- **Fields & Columns**:
  - `UUID id` -> `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`
  - `String camundaProcessInstanceId` -> `@Column(name = "camunda_process_instance_id", nullable = false)`
  - `String messageId` -> `@Column(name = "message_id", nullable = false)`
  - `String senderEmail` -> `@Column(name = "sender_email", nullable = false)`
  - `String subject` -> `@Column(name = "subject")`
  - `Integer attachmentCount` -> `@Column(name = "attachment_count")`
  - `String status` -> `@Column(name = "status", nullable = false)`
  - `String rejectionReason` -> `@Column(name = "rejection_reason")`
  - `ZonedDateTime slaDeadline` -> `@Column(name = "sla_deadline", nullable = false)`
  - `String scanStatus` -> `@Column(name = "scan_status")`
  - `String fileSha256Hash` -> `@Column(name = "file_sha256_hash")`
  - `ZonedDateTime createdAt` -> `@Column(name = "created_at", nullable = false)`
  - `ZonedDateTime updatedAt` -> `@Column(name = "updated_at", nullable = false)`

#### 4. `com.ibpms.poc.domain.model.WebhookTransaction`
- **Table**: `ibpms_webhook_transactions`
- **Annotations**: `@Entity`, `@Table(name = "ibpms_webhook_transactions")`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- **Fields & Columns**:
  - `UUID id` -> `@Id`, `@Column(name = "id", updatable = false, nullable = false)`
  - `String messageId` -> `@Column(name = "message_id", nullable = false, unique = true)`
  - `String senderEmail` -> `@Column(name = "sender_email", nullable = false)`
  - `String senderDomain` -> `@Column(name = "sender_domain", nullable = false)`
  - `String subject` -> `@Column(name = "subject")`
  - `String payloadHash` -> `@Column(name = "payload_hash")`
  - `String status` -> `@Column(name = "status", nullable = false)`
  - `String rejectionReason` -> `@Column(name = "rejection_reason")`
  - `String camundaProcessInstanceId` -> `@Column(name = "camunda_process_instance_id")`
  - `ZonedDateTime createdAt` -> `@Column(name = "created_at", nullable = false, updatable = false)`

#### 5. `com.ibpms.poc.domain.model.agile.AgileProject`
- **Table**: `ibpms_agile_projects`
- **Annotations**: `@Entity`, `@Table(name = "ibpms_agile_projects")`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@PrePersist`
- **Fields & Columns**:
  - `UUID id` -> `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`
  - `String name` -> `@Column(name = "name", nullable = false)`
  - `String description` -> `@Column(name = "description")`
  - `String methodology` -> `@Column(name = "methodology", nullable = false)`
  - `String status` -> `@Column(name = "status", nullable = false)`
  - `String createdBy` -> `@Column(name = "created_by", nullable = false)`
  - `ZonedDateTime createdAt` -> `@Column(name = "created_at", nullable = false)`
  - `ZonedDateTime closedAt` -> `@Column(name = "closed_at")`
  - `String closedBy` -> `@Column(name = "closed_by")`
  - `Integer maxActiveTasks` -> `@Column(name = "max_active_tasks", nullable = false)`

#### 6. `com.ibpms.poc.domain.model.agile.AgileTask`
- **Table**: `ibpms_agile_tasks`
- **Annotations**: `@Entity`, `@Table(name = "ibpms_agile_tasks")`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@PrePersist`, `@PreUpdate`
- **Fields & Columns**:
  - `UUID id` -> `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`
  - `UUID projectId` -> `@Column(name = "project_id", nullable = false)`
  - `String teamId` -> `@Column(name = "team_id")`
  - `String title` -> `@Column(name = "title", nullable = false)`
  - `String description` -> `@Column(name = "description")`
  - `BigDecimal effortEstimated` -> `@Column(name = "effort_estimated")`
  - `BigDecimal effortActual` -> `@Column(name = "effort_actual")`
  - `String notes` -> `@Column(name = "notes")`
  - `String status` -> `@Column(name = "status", nullable = false)`
  - `Integer position` -> `@Column(name = "position", nullable = false)`
  - `String draftPayload` -> `@Column(name = "draft_payload", columnDefinition = "TEXT")`
  - `String draftPayloadHash` -> `@Column(name = "draft_payload_hash", length = 64)`
  - `ZonedDateTime draftExpiresAt` -> `@Column(name = "draft_expires_at")`
  - `ZonedDateTime slaDeadline` -> `@Column(name = "sla_deadline")`
  - `ZonedDateTime lastActivityAt` -> `@Column(name = "last_activity_at", nullable = false)`
  - `Integer timeoutExtensions` -> `@Column(name = "timeout_extensions")`
  - `String createdBy` -> `@Column(name = "created_by", nullable = false)`
  - `ZonedDateTime createdAt` -> `@Column(name = "created_at", nullable = false)`
  - `ZonedDateTime updatedAt` -> `@Column(name = "updated_at", nullable = false)`
  - `Set<String> assigneeIds` -> `@ElementCollection(fetch = FetchType.EAGER)`, `@CollectionTable(name = "ibpms_agile_task_assignees", joinColumns = @JoinColumn(name = "task_id"))`, `@Column(name = "user_id")`
  - `Set<String> tags` -> `@ElementCollection(fetch = FetchType.EAGER)`, `@CollectionTable(name = "ibpms_agile_task_tags", joinColumns = @JoinColumn(name = "task_id"))`, `@Column(name = "tag")`

#### 7. `com.ibpms.poc.domain.model.agile.AgileTimebox`
- **Table**: `ibpms_agile_timeboxes`
- **Annotations**: `@Entity`, `@Table(name = "ibpms_agile_timeboxes")`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@PrePersist`
- **Fields & Columns**:
  - `UUID id` -> `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`
  - `UUID projectId` -> `@Column(name = "project_id", nullable = false)`
  - `String name` -> `@Column(name = "name", nullable = false, length = 150)`
  - `String goal` -> `@Column(name = "goal", length = 500)`
  - `LocalDate startDate` -> `@Column(name = "start_date", nullable = false)`
  - `LocalDate endDate` -> `@Column(name = "end_date", nullable = false)`
  - `String status` -> `@Column(name = "status", nullable = false, length = 30)`
  - `String createdBy` -> `@Column(name = "created_by", nullable = false, length = 100)`
  - `ZonedDateTime createdAt` -> `@Column(name = "created_at", nullable = false, updatable = false)`

#### 8. `com.ibpms.poc.domain.model.agile.AgileSlaChangelog`
- **Table**: `ibpms_agile_sla_changelog`
- **Annotations**: `@Entity`, `@Table(name = "ibpms_agile_sla_changelog")`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@PrePersist`
- **Fields & Columns**:
  - `UUID id` -> `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`
  - `UUID taskId` -> `@Column(name = "task_id", nullable = false)`
  - `ZonedDateTime previousValue` -> `@Column(name = "previous_value")`
  - `ZonedDateTime newValue` -> `@Column(name = "new_value")`
  - `String changedBy` -> `@Column(name = "changed_by", nullable = false)`
  - `ZonedDateTime changedAt` -> `@Column(name = "changed_at", nullable = false)`

---

### Purification Mapping Strategy

1. **Purify Domain Models**: 
   Remove all JPA/Hibernate imports and annotations from the domain classes. Maintain only clean Lombok decorators (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Data`).
   
2. **Recreate JPA Entities**:
   Create corresponding JpaEntity classes under `com.ibpms.poc.infrastructure.jpa.entity` (and subpackage `agile` for Agile models) holding the exact annotations and mappings:
   - `AllowedDomainJpaEntity`
   - `OrphanPayloadJpaEntity`
   - `TriageTaskJpaEntity`
   - `WebhookTransactionJpaEntity`
   - `agile/AgileProjectJpaEntity`
   - `agile/AgileTaskJpaEntity`
   - `agile/AgileTimeboxJpaEntity`
   - `agile/AgileSlaChangelogJpaEntity`

3. **MapStruct Mappers**:
   Write bidirectional mappers under `com.ibpms.poc.infrastructure.jpa.mapper` annotated with `@Mapper(componentModel = "spring")`:
   - `AllowedDomainMapper`
   - `OrphanPayloadMapper`
   - `TriageTaskMapper`
   - `WebhookTransactionMapper`
   - `agile/AgileProjectMapper`
   - `agile/AgileTaskMapper`
   - `agile/AgileTimeboxMapper`
   - `agile/AgileSlaChangelogMapper`

---

## 2. Port Decoupling: TriageTaskRepository (R2)

### Inspection Findings
`TriageTaskRepository` (in `com.ibpms.poc.domain.port`) contains the method:
```java
Page<TriageTask> findByStatus(String status, Pageable pageable);
```
This couples the domain/application layers directly to Spring Data's `org.springframework.data.domain.Page` and `org.springframework.data.domain.Pageable`.

### Callers & Implementations
- **Implementation**: `TriageTaskRepositoryJpa.java` (infrastructure)
- **Callers**:
  - `TriageTaskService.java` (in `com.ibpms.poc.application.service`)
  - `TriageTaskController.java` (in `com.ibpms.poc.infrastructure.web`)

---

### Decoupling Strategy

1. **Introduce a Domain Pagination Record**:
   Define `com.ibpms.poc.domain.model.DomainPage` to represent paginated lists without Spring Data dependencies:
   ```java
   package com.ibpms.poc.domain.model;
   
   import java.util.List;
   
   public record DomainPage<T>(
       List<T> content,
       long totalElements,
       int page,
       int size
   ) {}
   ```

2. **Refactor the Domain Port**:
   Change signature in `TriageTaskRepository.java`:
   ```java
   DomainPage<TriageTask> findByStatus(String status, int page, int size);
   ```

3. **Update Application Layer**:
   Modify `TriageTaskService.java` method:
   ```java
   public DomainPage<TriageTask> listTasks(String status, int page, int size) {
       return triageTaskRepository.findByStatus(status != null ? status : "PENDING", page, size);
   }
   ```

4. **Update Infrastructure Adapter**:
   Update `TriageTaskRepositoryJpa.java` to convert primitive parameters to Spring Data types, query, and map back:
   ```java
   @Override
   public DomainPage<TriageTask> findByStatus(String status, int page, int size) {
       Page<TriageTaskJpaEntity> jpaPage = repository.findByStatus(status, PageRequest.of(page, size));
       List<TriageTask> content = jpaPage.getContent().stream()
               .map(mapper::toDomain)
               .toList();
       return new DomainPage<>(content, jpaPage.getTotalElements(), page, size);
   }
   ```

5. **Update Infrastructure Web Controller**:
   Adapt `TriageTaskController.java` to pass page/size to the service, and map the returned `DomainPage<TriageTask>` back to a Spring Data `PageImpl<TriageTask>` to maintain client contract compatibility:
   ```java
   @GetMapping
   public ResponseEntity<Page<TriageTask>> getTriageTasks(
           @RequestParam(required = false, defaultValue = "PENDING") String status,
           Pageable pageable) {
       DomainPage<TriageTask> domainPage = triageTaskService.listTasks(status, pageable.getPageNumber(), pageable.getPageSize());
       return ResponseEntity.ok(new PageImpl<>(domainPage.content(), pageable, domainPage.totalElements()));
   }
   ```

---

## 3. Adapter Package Consolidation (R3)

All adapter implementations currently located under `com.ibpms.poc.infrastructure.adapters` must be moved to `com.ibpms.poc.infrastructure.adapter`.

### Consolidation Mapping

Move the following files:

| Source Path (under `infrastructure/adapters`) | Target Path (under `infrastructure/adapter`) |
|---|---|
| `BpmnAuditJpaAdapter.java` | `BpmnAuditJpaAdapter.java` |
| `BpmnDesignJpaAdapter.java` | `BpmnDesignJpaAdapter.java` |
| `CamundaBpmnValidationAdapter.java` | `CamundaBpmnValidationAdapter.java` |
| `CamundaEngineAdapter.java` | `CamundaEngineAdapter.java` |
| `CamundaGenericTaskAdapter.java` | `CamundaGenericTaskAdapter.java` |
| `CamundaTaskQueryAdapter.java` | `CamundaTaskQueryAdapter.java` |
| `DataMappingJpaAdapter.java` | `DataMappingJpaAdapter.java` |
| `DeployRequestJpaAdapter.java` | `DeployRequestJpaAdapter.java` |
| `ExternalTaskTopicJpaAdapter.java` | `ExternalTaskTopicJpaAdapter.java` |
| `FeatureToggleJpaAdapter.java` | `FeatureToggleJpaAdapter.java` |
| `FormDefinitionFullAdapter.java` | `FormDefinitionFullAdapter.java` |
| `FormDesignJpaAdapter.java` | `FormDesignJpaAdapter.java` |
| `ProcessDesignJpaAdapter.java` | `ProcessDesignJpaAdapter.java` |
| `ProcessLockJpaAdapter.java` | `ProcessLockJpaAdapter.java` |
| `SecurityRoleJpaAdapter.java` | `SecurityRoleJpaAdapter.java` |
| `TaskSkipJpaAdapter.java` | `TaskSkipJpaAdapter.java` |
| `WorkdeskProjectionJpaAdapter.java` | `WorkdeskProjectionJpaAdapter.java` |
| `external/MsGraphWebClientAdapter.java` | `external/MsGraphWebClientAdapter.java` |
| `external/SharePointAdapterService.java` | `external/SharePointAdapterService.java` |
| `inbound/messaging/WebhookIntakeConsumer.java` | `inbound/messaging/WebhookIntakeConsumer.java` |
| `security/ImpersonationJpaAdapter.java` | `security/ImpersonationJpaAdapter.java` |
| `security/RoleHierarchyJpaAdapter.java` | `security/RoleHierarchyJpaAdapter.java` |
| `security/UserJpaAdapter.java` | `security/UserJpaAdapter.java` |
| `ui/MenuTopologyJpaAdapter.java` | `ui/MenuTopologyJpaAdapter.java` |

### Files Requiring Package/Import Updates

1. **All the Moved Adapters**: Change package declarations from `com.ibpms.poc.infrastructure.adapters...` to `com.ibpms.poc.infrastructure.adapter...`.
2. **Test Files inside `test/java/com/ibpms/poc/infrastructure/adapters/`**:
   - `BpmnAuditJpaAdapterTest.java`
   - `BpmnGatewayConvergenceGovernanceCA27Test.java`
   - `BpmnInfiniteLoopGovernanceCA23Test.java`
   - `BpmnStructuralGovernanceCA09Test.java`
   - `BpmnZombieNodeGovernanceCA22Test.java`
   - `DataMappingJpaAdapterTest.java`
   - `DmnBindingValidationTest.java`
   - `ProcessLockJpaAdapterTest.java`
   - `SecurityRoleJpaAdapterTest.java`
   (Their packages must change to `com.ibpms.poc.infrastructure.adapter...`)
3. **Integration Test Files importing moved classes**:
   - `DeploymentGovernanceIntegrationTest.java`
   - `DeploymentGovernanceIntegrationAdversarialTest.java`
   (Update import of `CamundaBpmnValidationAdapter`)

---

## 4. Controller Consolidation (R4)

### Current Setup & Behavior
- `TaskDraftController.java`:
  - **Path**: `/api/v1/workbox/tasks/{taskId}/draft`
  - **Behavior**: GET (reads draft from `TaskDraftRepository` for authenticated user), PUT (saves new/updates existing draft), DELETE (deletes draft). Implements a rate limiter bucket (6 requests / min).
- `TaskDraftApiController.java`:
  - **Path**: `/api/v1/drafts/{taskId}` (and `/api/v1/tasks/{taskId}/complete`)
  - **Behavior**: POST (saves partial payload to AgileTask draft fields via `TaskDraftService`), GET (returns draft from AgileTask), DELETE (clears AgileTask draft fields). Does not have rate limiting.

### Consolidation Strategy
Remove `TaskDraftController.java` completely.
Move rate limiting logic to `TaskDraftApiController.java`:
- In `TaskDraftApiController.java`, inject `Bucket draftRateLimiterBucket`.
- For GET, POST, and DELETE endpoints of `/api/v1/drafts/{taskId}`, call `draftRateLimiterBucket.tryConsume(1)`. If rate-limit exceeded, return `HttpStatus.TOO_MANY_REQUESTS` with `Retry-After: 10` header.

---

## 5. Summary of Files to Modify, Create, and Delete

### Create
1. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/DomainPage.java`
2. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/AllowedDomainJpaEntity.java`
3. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/OrphanPayloadJpaEntity.java`
4. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/TriageTaskJpaEntity.java`
5. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/WebhookTransactionJpaEntity.java`
6. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/agile/AgileProjectJpaEntity.java`
7. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/agile/AgileTaskJpaEntity.java`
8. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/agile/AgileTimeboxJpaEntity.java`
9. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/agile/AgileSlaChangelogJpaEntity.java`
10. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper/AllowedDomainMapper.java`
11. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper/OrphanPayloadMapper.java`
12. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper/TriageTaskMapper.java`
13. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper/WebhookTransactionMapper.java`
14. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper/agile/AgileProjectMapper.java`
15. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper/agile/AgileTaskMapper.java`
16. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper/agile/AgileTimeboxMapper.java`
17. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper/agile/AgileSlaChangelogMapper.java`

### Modify
1. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/AllowedDomain.java` (remove annotations)
2. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/OrphanPayload.java` (remove annotations)
3. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/TriageTask.java` (remove annotations)
4. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/WebhookTransaction.java` (remove annotations)
5. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/agile/AgileProject.java` (remove annotations)
6. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/agile/AgileTask.java` (remove annotations)
7. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/agile/AgileTimebox.java` (remove annotations)
8. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/agile/AgileSlaChangelog.java` (remove annotations)
9. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/port/TriageTaskRepository.java` (decouple page/pageable)
10. `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/TriageTaskService.java` (decouple page/pageable)
11. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/TriageTaskController.java` (use domainPage, map to PageImpl)
12. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/TaskDraftApiController.java` (add rate limiter and checks)
13. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/persistence/AllowedDomainRepositoryJpa.java` (adapt to AllowedDomainJpaEntity)
14. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/persistence/OrphanPayloadRepositoryJpa.java` (adapt to OrphanPayloadJpaEntity)
15. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/persistence/TriageTaskRepositoryJpa.java` (adapt to TriageTaskJpaEntity)
16. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/persistence/WebhookTransactionRepositoryJpa.java` (adapt to WebhookTransactionJpaEntity)
17. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/persistence/AgileProjectRepositoryJpa.java` (adapt to AgileProjectJpaEntity)
18. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/persistence/AgileTaskRepositoryJpa.java` (adapt to AgileTaskJpaEntity)
19. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/persistence/AgileTimeboxRepositoryJpa.java` (adapt to AgileTimeboxJpaEntity)
20. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/persistence/AgileSlaChangelogRepositoryJpa.java` (adapt to AgileSlaChangelogJpaEntity)
21. All 24 adapters and 11 tests (relocate to `com.ibpms.poc.infrastructure.adapter` package namespace and update imports/packages).

### Delete
1. `backend/ibpms-core/src/main/java/com/ibpms/poc/api/controller/TaskDraftController.java` (consolidation)
2. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/TaskDraftEntity.java` (redundant)
3. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/repository/TaskDraftRepository.java` (redundant)
