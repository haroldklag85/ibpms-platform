# Changes Record

The following fixes have been applied to resolve compilation and runtime errors in the security and web test suites of `backend/ibpms-core`:

## 1. `AuthControllerIntegrationTest.java`
- **Location**: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/security/AuthControllerIntegrationTest.java`
- **Change**: Updated the class to extend `AbstractLocalE2ETest` (which starts a WebEnvironment.MOCK context) instead of `AbstractIntegrationTest`.
- **Traceability**: Added `// @Traceability: US-003 - ADR-001` at line 1.

## 2. `JwtAuthFilter.java`
- **Location**: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/security/JwtAuthFilter.java`
- **Changes**:
  - Annotated all constructor parameters with `@org.springframework.context.annotation.Lazy` to solve dependency loading issues in slice tests.
  - Modified role parsing and filtering logic to accept either `"ibpms_rol_"` or `"ROLE_"` prefixes.
- **Traceability**: Added `// @Traceability: US-003 - ADR-001` at line 1.

## 3. `ProcessLifecycleControllerTest.java`
- **Location**: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/ProcessLifecycleControllerTest.java`
- **Changes**:
  - Added `@MockBean` for `DesplegarDefinicionUseCase` to solve constructor instantiation issues.
  - Declared `TestArchiveController` as a public static inner class.
  - Created a static `@TestConfiguration` class `TestConfig` inside the test class to register `TestArchiveController` as a bean in the test application context, resolving the `404 Not Found` mapping issue and correctly returning `403 Forbidden`.
  - Imported the config with `@Import({SecurityConfig.class, ProcessLifecycleControllerTest.TestConfig.class})`.
- **Traceability**: Added `// @Traceability: US-003 - ADR-001` at line 1.

## 4. `AuditReportControllerTest.java`
- **Location**: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/security/AuditReportControllerTest.java`
- **Changes**:
  - Extended `BaseWebMvcTest` (which declares and configures mocks for `JwtAuthFilter`, `JwtTokenProvider`, etc.) to fix context boot issues.
  - Removed duplicate `ServiceAccountRepository` mock bean since it is already defined in `BaseWebMvcTest`.
- **Traceability**: Added `// @Traceability: US-003 - ADR-001` at line 1.

## 5. `EntraIdSyncService.java`
- **Location**: `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/security/EntraIdSyncService.java`
- **Change**: Updated parameter presence checks in `provisionUser` to safely verify null claims (`email`, `name`, `Sucursal_ID`, `Codigo_Jefe`) before invoking `.isBlank()`.
- **Traceability**: Added `// @Traceability: US-003 - ADR-001` at line 1.
