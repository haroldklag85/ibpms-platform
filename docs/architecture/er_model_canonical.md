# iBPMS Canonical ER Model

## Security & RBAC Schema (Post-Consolidation V2)

This diagram reflects the consolidated Role-Based Access Control (RBAC) architecture after eliminating the legacy tripartite schemes (`sys_role`, `ibpms_roles`). The core permissions model now solely relies on `ibpms_security_role` mapping to users via the `ibpms_security_user_roles` cross-reference table (with composite PK to enforce unique constraint).

We also introduced `ibpms_tenant` to strictly enforce multi-tenancy, including referential integrity on cross-cutting projections like `ibpms_workdesk_projection`. Legacy proxy tables such as `user_delegation` and `user_roles` have been permanently eliminated.

```mermaid
erDiagram
    ibpms_tenant ||--o{ ibpms_security_user : "contains"
    ibpms_tenant ||--o{ ibpms_workdesk_projection : "contains"
    
    ibpms_tenant {
        VARCHAR slug PK
        VARCHAR name
        BOOLEAN is_active
    }

    ibpms_security_user ||--o{ ibpms_security_user_roles : "has"
    ibpms_security_user {
        UUID id PK
        VARCHAR username
        VARCHAR email
        VARCHAR password_hash
        BOOLEAN is_active
        BOOLEAN is_external_idp
    }

    ibpms_security_role ||--o{ ibpms_security_user_roles : "assigned_to"
    ibpms_security_role {
        UUID id PK
        VARCHAR name
        VARCHAR description
        BOOLEAN is_vip_restricted
        BOOLEAN is_template
        VARCHAR source
        VARCHAR process_definition_id
        VARCHAR lane_id
        UUID parent_role_id FK
    }

    ibpms_security_user_roles {
        UUID user_id PK, FK
        UUID role_id PK, FK
    }

    ibpms_workdesk_projection {
        UUID id PK
        VARCHAR tenant_id FK
        VARCHAR task_name
        VARCHAR status
        JSON payload
        TIMESTAMP created_at
    }
```
