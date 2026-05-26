# Scope: Backend M1 Fix for US-004

## Architecture
- `ibpms-core` backend module.
- We need to completely delete the placeholder-deleted files `SharePointAdapterService.java` and `MsGraphWebClientAdapter.java` because their presence fails the victory audit.

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | File Deletion & Build | Delete specific Java files from filesystem and run `mvn clean package -DskipTests` | none | PLANNED |

## Interface Contracts
- None modified in this scope, pure cleanup and build verification.
