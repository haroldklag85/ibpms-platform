# Changes Summary

This document lists all modifications performed in the `ibpms-core` module to remediate dependency ordering and traceability issues identified by the Forensic Audit.

## 1. Dependency Ordering Configuration (`backend/ibpms-core/pom.xml`)
- Reordered annotation processor configurations under `maven-compiler-plugin`'s `<annotationProcessorPaths>` to declare `lombok` before `mapstruct-processor`.
- Added the `lombok-mapstruct-binding` dependency path (version `0.2.0`) to ensure Lombok getters, setters, and builders are fully visible to MapStruct during class mapping generation.

## 2. Test Configuration Adjustment (`backend/ibpms-core/src/test/java/com/ibpms/poc/AbstractIntegrationTest.java`)
- Updated `spring.liquibase.enabled` to `true` to guarantee that all database tables, indexes, and constraints (including append-only event store triggers) are properly initialized during integration test execution.
- Reset the PostgreSQL Docker environment to start with a clean database schema so that Liquibase migration scripts run without conflicting with pre-existing tables.

## 3. Traceability Headers Addition
Added `// @Traceability: US-003 - ADR-001` on line 1 of the following 14 files:
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
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/FormEventStoreEntity.java`
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/TaskDraftJpaEntity.java`
- `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/adapter/BpmnAuditJpaAdapterTest.java` (replaces old audit annotation comment)
- `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/adapter/DataMappingJpaAdapterTest.java` (replaces old audit annotation comment)
