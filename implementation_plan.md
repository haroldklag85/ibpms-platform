# Implementation Plan - Backend Bugfix US-005 (Versions History API)

This plan details the steps to correct the Process Versions history endpoint (`/api/v1/design/processes/{processDefinitionKey}/versions`) in the backend to return an empty list with `200 OK` when the process is a draft or is not found, rather than throwing an unhandled `IllegalArgumentException` (which translates to `400 Bad Request`).

## 1. Target Files
- **Backend Controller**: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java`
- **Sandbox Governance Integration Tests**: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/bpmn/SandboxGovernanceTest.java`

---

## 2. Step-by-Step Implementation

### Step 1: Branch Creation
Create and switch to the target branch:
```bash
git checkout -b DevDavid/bugfix/US-005-versions-api
```

### Step 2: Modify `getProcessVersions` in `BpmnDesignController.java`
- Catch `IllegalArgumentException` thrown by `bpmnDesignService.obtenerPorTechnicalId` and return `ResponseEntity.ok(List.of())`.
- Check if the process design has a null version or version `0` (draft), returning `ResponseEntity.ok(List.of())`.
- Map standard fields required by the frontend:
  - `versionId` -> `dto.getCurrentVersion()`
  - `deploymentId` -> `"dep-" + processDefinitionKey`
  - `isLatest` -> `true`
  - `date` -> `dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : ""`
  - `author` -> `dto.getCreatedBy() != null ? dto.getCreatedBy() : "Sistema"`
  - `status` -> `dto.getStatus() != null ? dto.getStatus() : "BORRADOR"`
- Annotate the code change with the required traceability comment:
  `// @Traceability: US-005, CA-15, BUG-FIX: Retornar lista vacía de versiones si el proceso no tiene despliegues`

### Step 3: Run Verification Tests in WSL
Verify the fix by executing:
```bash
wsl sh -c "cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core && mvn test -Dtest=SandboxGovernanceTest"
```

### Step 4: Commit and Push
Ensure no `git stash` is used. Standard git workflow:
```bash
git add .
git commit -m "fix(backend): US-005 BUG-FIX get process versions empty list"
git push origin DevDavid/bugfix/US-005-versions-api
```

---

## 3. Definition of Done (DoD)
1. **Compilation without errors**: Spring Boot application compiles inside the native environment.
2. **Success of `testGetProcessVersionsNotFoundReturnsEmptyList`**: The RestAssured integration test returns `200 OK` with an empty JSON array `[]`.
3. **Traceability Tag**: Modified lines are properly tagged with reverse traceability tag.
4. **No Git Stash**: All changes are directly committed to the branch.
