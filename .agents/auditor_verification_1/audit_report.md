# Forensic Audit Report — Refactoring to Hexagonal Architecture & DDD (ADR-001)

**Work Product**: Backend execution engine (`ibpms-core` module)
**Profile**: General Project (Development Mode)
**Verdict**: INTEGRITY VIOLATION

---

## 1. Summary of Findings
The refactoring of the backend to Hexagonal Architecture and Domain-Driven Design (DDD) under ADR-001 is **incomplete** and **fundamentally broken** due to traceability check failures and a critical mapping configuration defect. 

While domain models have been successfully purified of JPA annotations and the directory namespaces have been renamed to singular adapters, the implementation fails in two major areas:
1. **Traceability Verification Check**: A total of 14 newly created or modified source and test files do not contain the mandatory `// @Traceability: US-003 - ADR-001` comment on their first line.
2. **Broken Mapping Implementations & Test Failure**: MapStruct and Lombok processors are misconfigured in `pom.xml`. The `mapstruct-processor` is declared before `lombok` in `annotationProcessorPaths`, causing MapStruct to compile empty implementations of all mappers (such as `AgileTaskMapperImpl` and `TriageTaskMapperImpl`) that map absolutely nothing. As a result, integration tests fail with `PSQLException` due to null constraint violations when saving entity drafts.

---

## 2. Validation Check Results

### Check 1: Domain Purification Check
* **Verdict**: **PASS**
* **Findings**:
  - The domain classes under `com.ibpms.poc.domain.model` (including the `agile` subpackage) contain only clean Lombok/standard decorators (e.g., `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`).
  - There are NO imports of `jakarta.persistence.*`, `@Entity`, `@Table`, `@Column`, or `@Id` inside the domain model package. Comments in files like `FormEvent.java` explicitly restrict JPA/Hibernate annotations.
  - JPA entities exist correctly under `com.ibpms.poc.infrastructure.jpa.entity` (with suffix `JpaEntity` for refactored classes: `AllowedDomainJpaEntity`, `OrphanPayloadJpaEntity`, `TaskDraftJpaEntity`, `TriageTaskJpaEntity`, and `WebhookTransactionJpaEntity`).
  - MapStruct mappers exist under `com.ibpms.poc.infrastructure.jpa.mapper`.

### Check 2: Domain Port Decoupling Check
* **Verdict**: **PASS**
* **Findings**:
  - The interface `com.ibpms.poc.domain.port.TriageTaskRepository.java` does NOT import or use Spring Data pagination abstractions like `Page` or `Pageable`.
  - It utilizes primitive paging arguments `int page`, `int size` along with the custom domain record `DomainPage<T>` for output pagination.
  - Spring Data pagination concepts (`PageRequest`, `Pageable`, `Page`) are isolated within `com.ibpms.poc.infrastructure.persistence.TriageTaskRepositoryJpa`.

### Check 3: Adapters Namespace Check
* **Verdict**: **PASS**
* **Findings**:
  - The plural namespace directory `infrastructure/adapters/` has been deleted from both the main package layout and test package layout.
  - All adapters have been migrated to the singular directory `infrastructure/adapter/`.
  - There are no residual imports or package declarations referring to `com.ibpms.poc.infrastructure.adapters`.

### Check 4: Redundancy Elimination Check
* **Verdict**: **PASS**
* **Findings**:
  - `TaskDraftController.java` is deleted from the codebase.
  - `TaskDraftApiController.java` under `com.ibpms.poc.infrastructure.web` is active and maps `/api/v1/drafts/{taskId}`.
  - Token-bucket rate limiting via Bucket4J (`draftRateLimiterBucket.tryConsume(1)`) is implemented in all endpoints of `TaskDraftApiController` and correctly returns a `429 TOO_MANY_REQUESTS` status on exhaustion.

### Check 5: Traceability Verification Check
* **Verdict**: **FAIL**
* **Findings**:
  A total of 14 Java source and test files lack the comment `// @Traceability: US-003 - ADR-001` on their very first line. 
  
  **Newly Created Application Ports (10 files)** (start with `package ...` declaration on line 1 instead of the comment):
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/in/UpdateFeatureToggleUseCase.java`
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/FeatureTogglePort.java`
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/FormDesignPort.java`
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/GenericProcessDefinitionPort.java`
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/GenericTaskPort.java`
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/ImpersonationPort.java`
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/MenuTopologyPort.java`
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/ProcessEnginePort.java`
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/RoleHierarchyPort.java`
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/TaskQueryPort.java`

  **Newly Created JPA Entities (2 files)** (start with `package ...` declaration on line 1):
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/FormEventStoreEntity.java`
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/TaskDraftJpaEntity.java`

  **Newly Created/Modified Test Files (2 files)** (start with `// @Traceability: US-005, CA-63 Aislamiento de Sandbox` instead of US-003):
  - `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/adapter/BpmnAuditJpaAdapterTest.java`
  - `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/adapter/DataMappingJpaAdapterTest.java`

### Check 6: Authenticity Check (Anti-Cheating)
* **Verdict**: **FAIL**
* **Findings**:
  Although there is no evidence of hardcoded cheats or bypasses, the compilation of the refactored code has a critical functional defect:
  - Inside `backend/ibpms-core/pom.xml`, the `annotationProcessorPaths` configuration lists `mapstruct-processor` before `lombok`. Because MapStruct runs prior to Lombok's getter/setter generation, it compiles empty mapper implementations:
    ```java
    // Example from generated target/.../AgileTaskMapperImpl.java
    @Override
    public AgileTaskJpaEntity toEntity(AgileTask domain) {
        if ( domain == null ) {
            return null;
        }
        AgileTaskJpaEntity agileTaskJpaEntity = new AgileTaskJpaEntity();
        return agileTaskJpaEntity; // All fields are left unmapped/null
    }
    ```
  - Consequently, running the integration test `TaskDraftIntegrationTest` results in a compilation success but runtime failure:
    ```
    Caused by: org.postgresql.util.PSQLException: ERROR: null value in column "created_by" of relation "ibpms_agile_tasks" violates not-null constraint
    ```
    Since mappers are empty, entity field values are not copied from domain objects, causing database integrity constraint violations.

---

## 3. Forensic Evidence

### A. Empty Generated Mapper Verification
The generated mapper implementation at `backend/ibpms-core/target/generated-sources/annotations/com/ibpms/poc/infrastructure/jpa/mapper/agile/AgileTaskMapperImpl.java` compiles as:
```java
package com.ibpms.poc.infrastructure.jpa.mapper.agile;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.jpa.entity.agile.AgileTaskJpaEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-30T00:17:15-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class AgileTaskMapperImpl implements AgileTaskMapper {

    @Override
    public AgileTask toDomain(AgileTaskJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }
        AgileTask agileTask = new AgileTask();
        return agileTask;
    }

    @Override
    public AgileTaskJpaEntity toEntity(AgileTask domain) {
        if ( domain == null ) {
            return null;
        }
        AgileTaskJpaEntity agileTaskJpaEntity = new AgileTaskJpaEntity();
        return agileTaskJpaEntity;
    }
}
```

### B. POM Dependency Ordering Defect
In `backend/ibpms-core/pom.xml`, the annotation processors are declared as:
```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>1.5.5.Final</version>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
    </path>
</annotationProcessorPaths>
```
To fix this, Lombok must be ordered before MapStruct, and the `lombok-mapstruct-binding` processor should be explicitly configured in `annotationProcessorPaths`. Since this is an audit-only task, no changes have been applied to the code.
