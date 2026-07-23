# Analysis: Backend Refactoring for ibpms-platform

This report details the findings and plans for closing technical debt and architectural violations under ADR-001 (Hexagonal Architecture & DDD), mapping out the exact changes needed for requirements R1, R2, R3, and R4.

---

## R1. Purificación de Modelos de Dominio (Domain Model Purification)

### 1. Current JPA Annotations in Domain Models
The following 8 domain models under `com.ibpms.poc.domain.model` contain infrastructure-layer JPA annotations, violating the isolation of the domain layer. They must be stripped of all JPA/Hibernate annotations.

#### A. `AllowedDomain.java`
*   **Target Table:** `ibpms_webhook_allowed_domains`
*   **Primary Key:** `UUID id` (`@Id`, `@Column(name = "id", updatable = false, nullable = false)`)
*   **Columns:**
    *   `domain`: `VARCHAR`, `@Column(name = "domain", nullable = false)`
    *   `tenantId`: `VARCHAR`, `@Column(name = "tenant_id", nullable = false)`
    *   `description`: `VARCHAR`, `@Column(name = "description")`
    *   `createdBy`: `VARCHAR`, `@Column(name = "created_by", nullable = false)`
    *   `createdAt`: `TIMESTAMP`, `@Column(name = "created_at", nullable = false, updatable = false)`
    *   `isActive`: `BOOLEAN`, `@Column(name = "is_active", nullable = false)`
*   **Annotations to Remove:** `@Entity`, `@Table`, `@Id`, `@Column`

#### B. `OrphanPayload.java`
*   **Target Table:** `ibpms_orphan_payloads`
*   **Primary Key:** `UUID id` (`@Id`, `@Column(name = "id", updatable = false, nullable = false)`)
*   **Columns:**
    *   `rawPayload`: `JSONB`, `@JdbcTypeCode(SqlTypes.JSON)`, `@Column(name = "raw_payload", columnDefinition = "jsonb")`
    *   `errorType`: `VARCHAR`, `@Column(name = "error_type", nullable = false)`
    *   `fileHashSha256`: `VARCHAR`, `@Column(name = "file_hash_sha256")`
    *   `fileName`: `VARCHAR`, `@Column(name = "file_name")`
    *   `fileSizeBytes`: `BIGINT`, `@Column(name = "file_size_bytes")`
    *   `senderEmail`: `VARCHAR`, `@Column(name = "sender_email")`
    *   `createdAt`: `TIMESTAMP`, `@Column(name = "created_at", nullable = false, updatable = false)`
*   **Annotations to Remove:** `@Entity`, `@Table`, `@Id`, `@Column`, `@JdbcTypeCode`

#### C. `TriageTask.java`
*   **Target Table:** `ibpms_triage_tasks`
*   **Primary Key:** `UUID id` (`@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`)
*   **Columns:**
    *   `camundaProcessInstanceId`: `VARCHAR`, `@Column(name = "camunda_process_instance_id", nullable = false)`
    *   `messageId`: `VARCHAR`, `@Column(name = "message_id", nullable = false)`
    *   `senderEmail`: `VARCHAR`, `@Column(name = "sender_email", nullable = false)`
    *   `subject`: `VARCHAR`, `@Column(name = "subject")`
    *   `attachmentCount`: `INTEGER`, `@Column(name = "attachment_count")`
    *   `status`: `VARCHAR`, `@Column(name = "status", nullable = false)`
    *   `rejectionReason`: `VARCHAR`, `@Column(name = "rejection_reason")`
    *   `slaDeadline`: `TIMESTAMP`, `@Column(name = "sla_deadline", nullable = false)`
    *   `scanStatus`: `VARCHAR`, `@Column(name = "scan_status")`
    *   `fileSha256Hash`: `VARCHAR`, `@Column(name = "file_sha256_hash")`
    *   `createdAt`: `TIMESTAMP`, `@Column(name = "created_at", nullable = false)`
    *   `updatedAt`: `TIMESTAMP`, `@Column(name = "updated_at", nullable = false)`
*   **Lifecycle Annotations:** `@PrePersist`, `@PreUpdate` (Remove or move logic to domain/application layer)
*   **Annotations to Remove:** `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@PrePersist`, `@PreUpdate`

#### D. `WebhookTransaction.java`
*   **Target Table:** `ibpms_webhook_transactions`
*   **Primary Key:** `UUID id` (`@Id`, `@Column(name = "id", updatable = false, nullable = false)`)
*   **Columns:**
    *   `messageId`: `VARCHAR`, `@Column(name = "message_id", nullable = false, unique = true)`
    *   `senderEmail`: `VARCHAR`, `@Column(name = "sender_email", nullable = false)`
    *   `senderDomain`: `VARCHAR`, `@Column(name = "sender_domain", nullable = false)`
    *   `subject`: `VARCHAR`, `@Column(name = "subject")`
    *   `payloadHash`: `VARCHAR`, `@Column(name = "payload_hash")`
    *   `status`: `VARCHAR`, `@Column(name = "status", nullable = false)`
    *   `rejectionReason`: `VARCHAR`, `@Column(name = "rejection_reason")`
    *   `camundaProcessInstanceId`: `VARCHAR`, `@Column(name = "camunda_process_instance_id")`
    *   `createdAt`: `TIMESTAMP`, `@Column(name = "created_at", nullable = false, updatable = false)`
*   **Annotations to Remove:** `@Entity`, `@Table`, `@Id`, `@Column`

#### E. `agile/AgileProject.java`
*   **Target Table:** `ibpms_agile_projects`
*   **Primary Key:** `UUID id` (`@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`)
*   **Columns:**
    *   `name`: `VARCHAR`, `@Column(name = "name", nullable = false)`
    *   `description`: `VARCHAR`, `@Column(name = "description")`
    *   `methodology`: `VARCHAR`, `@Column(name = "methodology", nullable = false)`
    *   `status`: `VARCHAR`, `@Column(name = "status", nullable = false)`
    *   `createdBy`: `VARCHAR`, `@Column(name = "created_by", nullable = false)`
    *   `createdAt`: `TIMESTAMP`, `@Column(name = "created_at", nullable = false)`
    *   `closedAt`: `TIMESTAMP`, `@Column(name = "closed_at")`
    *   `closedBy`: `VARCHAR`, `@Column(name = "closed_by")`
    *   `maxActiveTasks`: `INTEGER`, `@Column(name = "max_active_tasks", nullable = false)`
*   **Lifecycle Annotations:** `@PrePersist` (Remove and move logic to domain/application level)
*   **Annotations to Remove:** `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@PrePersist`

#### F. `agile/AgileSlaChangelog.java`
*   **Target Table:** `ibpms_agile_sla_changelog`
*   **Primary Key:** `UUID id` (`@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`)
*   **Columns:**
    *   `taskId`: `UUID`, `@Column(name = "task_id", nullable = false)`
    *   `previousValue`: `TIMESTAMP`, `@Column(name = "previous_value")`
    *   `newValue`: `TIMESTAMP`, `@Column(name = "new_value")`
    *   `changedBy`: `VARCHAR`, `@Column(name = "changed_by", nullable = false)`
    *   `changedAt`: `TIMESTAMP`, `@Column(name = "changed_at", nullable = false)`
*   **Lifecycle Annotations:** `@PrePersist` (Remove)
*   **Annotations to Remove:** `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@PrePersist`

#### G. `agile/AgileTimebox.java`
*   **Target Table:** `ibpms_agile_timeboxes`
*   **Primary Key:** `UUID id` (`@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`)
*   **Columns:**
    *   `projectId`: `UUID`, `@Column(name = "project_id", nullable = false)`
    *   `name`: `VARCHAR`, `@Column(name = "name", nullable = false, length = 150)`
    *   `goal`: `VARCHAR`, `@Column(name = "goal", length = 500)`
    *   `startDate`: `DATE`, `@Column(name = "start_date", nullable = false)`
    *   `endDate`: `DATE`, `@Column(name = "end_date", nullable = false)`
    *   `status`: `VARCHAR`, `@Column(name = "status", nullable = false, length = 30)`
    *   `createdBy`: `VARCHAR`, `@Column(name = "created_by", nullable = false, length = 100)`
    *   `createdAt`: `TIMESTAMP`, `@Column(name = "created_at", nullable = false, updatable = false)`
*   **Lifecycle Annotations:** `@PrePersist` (Remove)
*   **Annotations to Remove:** `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@PrePersist`

#### H. `agile/AgileTask.java`
*   **Target Table:** `ibpms_agile_tasks`
*   **Primary Key:** `UUID id` (`@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)`)
*   **Columns:**
    *   `projectId`: `UUID`, `@Column(name = "project_id", nullable = false)`
    *   `teamId`: `VARCHAR`, `@Column(name = "team_id")`
    *   `title`: `VARCHAR`, `@Column(name = "title", nullable = false)`
    *   `description`: `VARCHAR`, `@Column(name = "description")`
    *   `effortEstimated`: `NUMERIC`, `@Column(name = "effort_estimated")`
    *   `effortActual`: `NUMERIC`, `@Column(name = "effort_actual")`
    *   `notes`: `VARCHAR`, `@Column(name = "notes")`
    *   `status`: `VARCHAR`, `@Column(name = "status", nullable = false)`
    *   `position`: `INTEGER`, `@Column(name = "position", nullable = false)`
    *   `draftPayload`: `TEXT`, `@Column(name = "draft_payload", columnDefinition = "TEXT")`
    *   `draftPayloadHash`: `VARCHAR`, `@Column(name = "draft_payload_hash", length = 64)`
    *   `draftExpiresAt`: `TIMESTAMP`, `@Column(name = "draft_expires_at")`
    *   `slaDeadline`: `TIMESTAMP`, `@Column(name = "sla_deadline")`
    *   `lastActivityAt`: `TIMESTAMP`, `@Column(name = "last_activity_at", nullable = false)`
    *   `timeoutExtensions`: `INTEGER`, `@Column(name = "timeout_extensions")`
    *   `createdBy`: `VARCHAR`, `@Column(name = "created_by", nullable = false)`
    *   `createdAt`: `TIMESTAMP`, `@Column(name = "created_at", nullable = false)`
    *   `updatedAt`: `TIMESTAMP`, `@Column(name = "updated_at", nullable = false)`
*   **Child Collections:**
    *   `assigneeIds`: `Set<String>`, mapping to `ibpms_agile_task_assignees` (`@ElementCollection`, `@CollectionTable(name = "ibpms_agile_task_assignees", joinColumns = @JoinColumn(name = "task_id"))`, `@Column(name = "user_id")`)
    *   `tags`: `Set<String>`, mapping to `ibpms_agile_task_tags` (`@ElementCollection`, `@CollectionTable(name = "ibpms_agile_task_tags", joinColumns = @JoinColumn(name = "task_id"))`, `@Column(name = "tag")`)
*   **Lifecycle Annotations:** `@PrePersist`, `@PreUpdate` (Remove)
*   **Annotations to Remove:** `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@ElementCollection`, `@CollectionTable`, `@JoinColumn`, `@PrePersist`, `@PreUpdate`

---

### 2. Proposed Persistence Entities (`JpaEntity`)
To store and retrieve state, equivalent infrastructure JPA entities will be created in the package `com.ibpms.poc.infrastructure.jpa.entity`.

| Domain Class | Proposed JpaEntity Class | Target Database Table |
| --- | --- | --- |
| `AllowedDomain` | `AllowedDomainJpaEntity` | `ibpms_webhook_allowed_domains` |
| `OrphanPayload` | `OrphanPayloadJpaEntity` | `ibpms_orphan_payloads` |
| `TriageTask` | `TriageTaskJpaEntity` | `ibpms_triage_tasks` |
| `WebhookTransaction` | `WebhookTransactionJpaEntity` | `ibpms_webhook_transactions` |
| `AgileProject` | `AgileProjectJpaEntity` | `ibpms_agile_projects` |
| `AgileSlaChangelog` | `AgileSlaChangelogJpaEntity` | `ibpms_agile_sla_changelog` |
| `AgileTimebox` | `AgileTimeboxJpaEntity` | `ibpms_agile_timeboxes` |
| `AgileTask` | `AgileTaskJpaEntity` | `ibpms_agile_tasks` |

*Note: All created JPA entities must carry the `@Entity` and `@Table` mappings previously on the domain models, along with `@Id`, `@Column`, etc.*

---

### 3. Proposed MapStruct Mappers
MapStruct interfaces will be defined under `com.ibpms.poc.infrastructure.jpa.mapper` to map between domain models and JPA entities.

All mappers must include:
*   `@Mapper(componentModel = "spring")`
*   The annotation `// @Traceability: US-003 - ADR-001`
*   Custom converters for JSON fields to/from `String` mappings (utilizing an injected Jackson `ObjectMapper`) for `TriageTaskEntityMapper`, `OrphanPayloadEntityMapper`, and `WebhookTransactionEntityMapper` if necessary.

Example mapper definition for `AgileTaskEntityMapper.java`:
```java
package com.ibpms.poc.infrastructure.jpa.mapper;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.jpa.entity.AgileTaskJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
// @Traceability: US-003 - ADR-001 - Mapper para AgileTask
public interface AgileTaskEntityMapper {
    AgileTask toDomain(AgileTaskJpaEntity entity);
    AgileTaskJpaEntity toEntity(AgileTask domain);
}
```

---

## R2. Desacoplamiento de Puertos (TriageTaskRepository Refactoring)

### 1. Current Port Violations
`TriageTaskRepository.java` (under `com.ibpms.poc.domain.port`) leaks Spring Data classes:
```java
Page<TriageTask> findByStatus(String status, Pageable pageable);
```

### 2. Agnostic Pagination Design
To decouple the domain layer from Spring Data:
*   Create new decoupled container records in `com.ibpms.poc.domain.model.common` (or `com.ibpms.poc.domain.port.common`):
    *   `DomainPageable`: `public record DomainPageable(int page, int size)`
    *   `DomainPage<T>`: `public record DomainPage<T>(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages)`
*   Modify `TriageTaskRepository.java` signature:
    ```java
    DomainPage<TriageTask> findByStatus(String status, DomainPageable pageable);
    ```

### 3. Impact Analysis & Refactoring Steps

#### A. Repository Implementation (`TriageTaskRepositoryJpa.java`)
1.  Make `SpringDataTriageTaskRepository` extend `JpaRepository<TriageTaskJpaEntity, UUID>` instead of `JpaRepository<TriageTask, UUID>`.
2.  Implement mapping logic in `TriageTaskRepositoryJpa` methods using `TriageTaskEntityMapper`.
3.  Implement `findByStatus(String status, DomainPageable pageable)`:
    *   Map `DomainPageable` to `org.springframework.data.domain.PageRequest.of(pageable.page(), pageable.size())`.
    *   Call Spring Data repo which returns `org.springframework.data.domain.Page<TriageTaskJpaEntity>`.
    *   Map each `TriageTaskJpaEntity` in the page content to `TriageTask` via MapStruct.
    *   Return a new `DomainPage<TriageTask>` containing the mapped list and metadata.

#### B. Application Service (`TriageTaskService.java`)
1.  Update references to pagination classes:
    ```java
    public DomainPage<TriageTask> listTasks(String status, DomainPageable pageable) {
        return triageTaskRepository.findByStatus(status != null ? status : "PENDING", pageable);
    }
    ```

#### C. Inbound Controller (`TriageTaskController.java`)
1.  Keep Spring MVC's auto-binding of Spring Data `Pageable` in `@GetMapping` as it resides in the infrastructure web layer.
2.  Convert `Pageable` to `DomainPageable` before invoking the service:
    ```java
    DomainPageable domainPageable = new DomainPageable(pageable.getPageNumber(), pageable.getPageSize());
    ```
3.  Invoke service: `DomainPage<TriageTask> domainPage = triageTaskService.listTasks(status, domainPageable);`.
4.  Map the returned `DomainPage` back to a Spring Data `PageImpl<TriageTask>` before returning it in `ResponseEntity.ok(...)` to maintain the API contract and exact JSON serialization keys expected by the frontend.

---

## R3. Consolidación de Adaptadores (Adapters Package Rename)

### 1. Folders to Rename
*   `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters` -> `adapter`
*   `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/adapters` -> `adapter`
*   `backend/ibpms-core/src/test/java.bak/com/ibpms/poc/infrastructure/adapters` -> `adapter`

### 2. Affected Main Source Files (24 files)
The package declarations in these files must change from `package com.ibpms.poc.infrastructure.adapters...` to `package com.ibpms.poc.infrastructure.adapter...`:

1.  `com/ibpms/poc/infrastructure/adapters/BpmnAuditJpaAdapter.java`
2.  `com/ibpms/poc/infrastructure/adapters/BpmnDesignJpaAdapter.java`
3.  `com/ibpms/poc/infrastructure/adapters/CamundaBpmnValidationAdapter.java`
4.  `com/ibpms/poc/infrastructure/adapters/CamundaEngineAdapter.java`
5.  `com/ibpms/poc/infrastructure/adapters/CamundaGenericTaskAdapter.java`
6.  `com/ibpms/poc/infrastructure/adapters/CamundaTaskQueryAdapter.java`
7.  `com/ibpms/poc/infrastructure/adapters/DataMappingJpaAdapter.java`
8.  `com/ibpms/poc/infrastructure/adapters/DeployRequestJpaAdapter.java`
9.  `com/ibpms/poc/infrastructure/adapters/ExternalTaskTopicJpaAdapter.java`
10. `com/ibpms/poc/infrastructure/adapters/FeatureToggleJpaAdapter.java`
11. `com/ibpms/poc/infrastructure/adapters/FormDefinitionFullAdapter.java`
12. `com/ibpms/poc/infrastructure/adapters/FormDesignJpaAdapter.java`
13. `com/ibpms/poc/infrastructure/adapters/ProcessDesignJpaAdapter.java`
14. `com/ibpms/poc/infrastructure/adapters/ProcessLockJpaAdapter.java`
15. `com/ibpms/poc/infrastructure/adapters/SecurityRoleJpaAdapter.java`
16. `com/ibpms/poc/infrastructure/adapters/TaskSkipJpaAdapter.java`
17. `com/ibpms/poc/infrastructure/adapters/WorkdeskProjectionJpaAdapter.java`
18. `com/ibpms/poc/infrastructure/adapters/external/MsGraphWebClientAdapter.java`
19. `com/ibpms/poc/infrastructure/adapters/external/SharePointAdapterService.java`
20. `com/ibpms/poc/infrastructure/adapters/inbound/messaging/WebhookIntakeConsumer.java`
21. `com/ibpms/poc/infrastructure/adapters/security/ImpersonationJpaAdapter.java`
22. `com/ibpms/poc/infrastructure/adapters/security/RoleHierarchyJpaAdapter.java`
23. `com/ibpms/poc/infrastructure/adapters/security/UserJpaAdapter.java`
24. `com/ibpms/poc/infrastructure/adapters/ui/MenuTopologyJpaAdapter.java`

### 3. Affected Test Files & Imports (19 files total)
Package declarations and imports must be updated in:
*   Tests located in `src/test/java/com/ibpms/poc/infrastructure/adapters` (9 files):
    *   `BpmnAuditJpaAdapterTest.java`
    *   `BpmnGatewayConvergenceGovernanceCA27Test.java`
    *   `BpmnInfiniteLoopGovernanceCA23Test.java`
    *   `BpmnStructuralGovernanceCA09Test.java`
    *   `BpmnZombieNodeGovernanceCA22Test.java`
    *   `DataMappingJpaAdapterTest.java`
    *   `DmnBindingValidationTest.java`
    *   `ProcessLockJpaAdapterTest.java`
    *   `SecurityRoleJpaAdapterTest.java`
*   Tests located in backup folder `src/test/java.bak/com/ibpms/poc/infrastructure/adapters` (8 files):
    *   `BpmnAuditJpaAdapterTest.java`
    *   `BpmnDesignJpaAdapterTest.java`
    *   `CamundaBpmnValidationAdapterTest.java`
    *   `DataMappingJpaAdapterTest.java`
    *   `DeployRequestJpaAdapterTest.java`
    *   `ExternalTaskTopicJpaAdapterTest.java`
    *   `ProcessLockJpaAdapterTest.java`
    *   `SecurityRoleJpaAdapterTest.java`
*   External test files importing classes from `com.ibpms.poc.infrastructure.adapters` (2 files):
    *   `src/test/java/com/ibpms/poc/application/service/security/DeploymentGovernanceIntegrationAdversarialTest.java`
    *   `src/test/java/com/ibpms/poc/application/service/security/DeploymentGovernanceIntegrationTest.java`

---

## R4. Eliminación de Redundancia (TaskDraft Cleanup)

### 1. Controllers Mappings & Collision
*   `TaskDraftController.java` mapped `PUT` requests to `/api/v1/workbox/tasks/{taskId}/draft`, which clashed with the `PUT` mapping at `/api/v1/workbox/tasks/{id}/draft` in `WorkboxTaskController.java`.
*   Both of these endpoints were doing redundant tasks: one persisted drafts in a custom table `task_drafts` and the other in `ibpms_agile_tasks`.
*   The active CQRS-compliant API endpoint for drafts is `/api/v1/drafts/{taskId}` (mapped inside `TaskDraftApiController.java`). It routes to `TaskDraftService` and saves drafts directly in the `draft_payload` column of the `ibpms_agile_tasks` table.

### 2. Proposed Cleanup Actions
To completely resolve the redundancy and clean up legacy, unused code:
1.  **Delete** `TaskDraftController.java` (`com.ibpms.poc.api.controller`).
2.  **Delete** the duplicate `saveDraft` PUT method mapping (lines 202-221) in `WorkboxTaskController.java` (`com.ibpms.poc.infrastructure.web`).
3.  **Delete** the legacy draft domain and persistence files:
    *   `src/main/java/com/ibpms/poc/domain/model/TaskDraft.java`
    *   `src/main/java/com/ibpms/poc/domain/port/TaskDraftRepository.java`
    *   `src/main/java/com/ibpms/poc/infrastructure/jpa/entity/TaskDraftEntity.java`
    *   `src/main/java/com/ibpms/poc/infrastructure/jpa/entity/TaskDraftJpaEntity.java`
    *   `src/main/java/com/ibpms/poc/infrastructure/jpa/repository/TaskDraftRepository.java`
    *   `src/main/java/com/ibpms/poc/infrastructure/persistence/TaskDraftRepositoryJpa.java`
    *   `src/test/java/com/ibpms/poc/TaskDraftCrudTest.java`
    *   `src/test/java.bak/com/ibpms/poc/TaskDraftCrudTest.java`
4.  **Consolidate in Frontend:**
    *   Update the `saveTaskDraft` method in `frontend/src/services/apiClient.ts` to call the new `/drafts/{taskId}` endpoint using a `POST` method:
        ```typescript
        // @Traceability: US-003 - ADR-001 - Consolidación de Borradores
        saveTaskDraft: (taskId: string, payload: any) => apiClient.post(`/drafts/${taskId}`, payload),
        ```
    *   Update the store `frontend/src/stores/useFormStore.ts` and its test `frontend/src/tests/stores/useFormStore.spec.ts` to confirm that draft saves work correctly with the consolidated endpoint (tests will mock `api.saveTaskDraft` as usual but compile against the updated signature).

---

## Traceability & Implementation Instructions
All created, renamed, or modified backend files must start with or contain the traceability comment:
`// @Traceability: US-003 - ADR-001`
This ensures compliance with compliance rules and architectural debt tracking.
