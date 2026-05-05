package com.ibpms.poc.infrastructure.jpa.entity.security;

/**
 * CA-07 US-036: User Lifecycle Status.
 * Defines the state of a user in the platform for Soft-Delete and Governance.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE
}
