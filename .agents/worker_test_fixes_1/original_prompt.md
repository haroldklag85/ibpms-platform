## 2026-05-30T05:03:08Z
You are the Worker. Your working directory is c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_test_fixes_1.
Your task is to fix the security and web test suites in `backend/ibpms-core` that have failed or failed to load.

### MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

### Objective
Apply the following targeted fixes to resolve compile and runtime errors in the test suite:

1. **`AuthControllerIntegrationTest.java`**:
   - Location: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/security/AuthControllerIntegrationTest.java`
   - Problem: Extends `AbstractIntegrationTest` (which starts a real Tomcat container on `RANDOM_PORT`), but autowires and uses `MockMvc` directly, throwing `IllegalArgumentException: Failed to find servlet [] in the servlet context`.
   - Fix: Update the class declaration to extend `com.ibpms.poc.AbstractLocalE2ETest` (which starts a WebEnvironment.MOCK context) instead of `AbstractIntegrationTest`.
   - Ensure the file has `// @Traceability: US-003 - ADR-001` at the very first line.

2. **`JwtAuthFilter.java`**:
   - Location: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/security/JwtAuthFilter.java`
   - Problems:
     a) Unsatisfied dependency of `JwtTokenProvider` and other services when loading slice tests (e.g. `@WebMvcTest`).
     b) Rejects roles that don't start with `"ibpms_rol_"`. Test tokens from integration tests use standard `"ROLE_"` prefixes like `"ROLE_AUDITOR_GLOBAL"`, returning 403 Forbidden.
   - Fixes:
     a) Annotate all constructor parameters of `JwtAuthFilter` with `@org.springframework.context.annotation.Lazy`.
     b) Modify the role parsing and filtering logic (around line 143) to accept either `"ibpms_rol_"` or `"ROLE_"` prefix:
        ```java
        List<String> rawRoles = jwtTokenProvider.getRoles(token);
        List<String> roles = rawRoles.stream()
                .filter(r -> r.startsWith("ibpms_rol_") || r.startsWith("ROLE_"))
                .map(r -> r.replace("ibpms_rol_", "").replace("ROLE_", ""))
                .collect(Collectors.toList());
        ```
   - Ensure the file has `// @Traceability: US-003 - ADR-001` at the very first line.

3. **`ProcessLifecycleControllerTest.java`**:
   - Location: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/ProcessLifecycleControllerTest.java`
   - Problem: Unsatisfied constructor dependency `DesplegarDefinicionUseCase` for `DeploymentController` when loading the web slice context.
   - Fix: Add a mock bean declaration inside `ProcessLifecycleControllerTest`:
     ```java
     @MockBean
     private com.ibpms.poc.application.port.in.DesplegarDefinicionUseCase desplegarDefinicionUseCase;
     ```
   - Ensure the file has `// @Traceability: US-003 - ADR-001` at the very first line.

4. **`AuditReportControllerTest.java`**:
   - Location: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/security/AuditReportControllerTest.java`
   - Problem: Context fails to boot because of unsatisfied filter dependencies.
   - Fix: Change class declaration to extend `com.ibpms.poc.infrastructure.web.BaseWebMvcTest` (which properly declares and configures mocks for `JwtAuthFilter`, `JwtTokenProvider`, etc.).
   - Ensure the file has `// @Traceability: US-003 - ADR-001` at the very first line.

5. **`EntraIdSyncService.java`**:
   - Location: `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/security/EntraIdSyncService.java`
   - Problem: Throws a NullPointerException mapping to 403 Forbidden in `provisionUser` when claims like `"Sucursal_ID"` or `"Codigo_Jefe"` are present in the map but their value is null.
   - Fix: Update the parameter presence and emptiness checks in `provisionUser` (lines 45-50) to safely check for null values before calling `.isBlank()`:
     ```java
     if (!claims.containsKey("email") || claims.get("email") == null || claims.get("email").isBlank()) missingFields.add("email");
     if (!claims.containsKey("name") || claims.get("name") == null || claims.get("name").isBlank()) missingFields.add("name");
     if (!claims.containsKey("Sucursal_ID") || claims.get("Sucursal_ID") == null || claims.get("Sucursal_ID").isBlank()) missingFields.add("Sucursal_ID");
     if (!claims.containsKey("Codigo_Jefe") || claims.get("Codigo_Jefe") == null || claims.get("Codigo_Jefe").isBlank()) missingFields.add("Codigo_Jefe");
     ```
   - Ensure the file has `// @Traceability: US-003 - ADR-001` at the very first line.

### Verification
1. Run `mvn clean compile` in the backend directory (`backend/ibpms-core`) to verify compilation.
2. Run `mvn test` in `backend/ibpms-core` to verify all tests (including the 5 failing test classes/methods) now pass.
3. Write your changes and execution log/outputs to `changes.md` and a structured `handoff.md`.
4. When complete, send a message to the orchestrator (b340978d-141d-4e11-a85f-c47b7d945b0a) indicating your completion.
