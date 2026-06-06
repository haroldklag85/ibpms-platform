# Implementation Plan - Backend Bugfix US-005 (Versions History API)

This plan details the steps to correct the Process Versions history endpoint (`/api/v1/design/processes/{processDefinitionKey}/versions`) in the backend to return an empty list with `200 OK` when the process is a draft or is not found, rather than throwing an unhandled `IllegalArgumentException` (which translates to `400 Bad Request`).

## 1. Target Files
- **Backend Controller**: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java`
- **Sandbox Governance Integration Tests**: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/bpmn/SandboxGovernanceTest.java`
- **Deploy Contract Integration Tests**: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/bpmn/BpmnDeployContractTest.java`

---

## 2. Step-by-Step Implementation

### Step 1: Branch Verification
Ensure we are working on the bugfix branch:
```bash
git checkout -b bugfix/DevDavid-us-005-versions-api
```

### Step 2: Modify `getProcessVersions` in `BpmnDesignController.java`
- Catch `IllegalArgumentException` thrown by `bpmnDesignService.obtenerPorTechnicalId` and return `ResponseEntity.ok(List.of())`.
- Check if the process design is null or has current version `0` (draft), returning `ResponseEntity.ok(List.of())`.
- Map standard fields required by the frontend:
  - `versionId` -> `dto.getCurrentVersion()`
  - `version` -> `dto.getCurrentVersion()`
  - `deploymentId` -> `"dep-" + processDefinitionKey`
  - `isLatest` -> `true`
  - `date` -> `dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : ""`
  - `author` -> `dto.getCreatedBy() != null ? dto.getCreatedBy() : "Sistema"`
  - `status` -> `dto.getStatus() != null ? dto.getStatus() : "BORRADOR"`
- Annotate the code change with the required traceability comment:
  `// @Traceability: US-005, CA-15, BUG-FIX: Retornar lista vacía de versiones si el proceso no tiene despliegues`

### Step 3: Seed Process Design in `BpmnDeployContractTest.java`
- Inject `BpmnProcessDesignRepository`.
- In `testGetVersionsForExistentProcessReturnsAlignedFields`, before invoking draft save or versions endpoint, seed a valid process design in the database (with status draft and version `1`) to avoid `IllegalArgumentException` when fetching the process.
- Assert that response fields match the contract (`version`, `date`, `author`, `status`).

### Step 4: Run Verification Tests in WSL
Verify the fix by executing:
```bash
wsl sh -c "cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core && mvn test -Dtest=SandboxGovernanceTest,BpmnDeployContractTest"
```

### Step 5: Commit and Push
Ensure no `git stash` is used. Standard git workflow:
```bash
git add .
git commit -m "fix(backend): US-005 BUG-FIX get process versions empty list"
git push origin bugfix/DevDavid-us-005-versions-api
```

---

## 3. Definition of Done (DoD)
1. **Compilation without errors**: Spring Boot application compiles inside the native environment.
2. **Success of `testGetProcessVersionsNotFoundReturnsEmptyList`**: The RestAssured integration test in `SandboxGovernanceTest` returns `200 OK` with an empty JSON array `[]`.
3. **Success of `testGetVersionsForExistentProcessReturnsAlignedFields`**: The RestAssured integration test in `BpmnDeployContractTest` returns `200 OK` with the versions array containing standard aligned fields.
4. **Traceability Tag**: Modified lines in `BpmnDesignController.java` are properly tagged with the reverse traceability tag.
5. **No Git Stash**: All changes are directly committed to the branch.

