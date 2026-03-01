package com.ibpms.poc.domain.port.out.security;

import com.ibpms.poc.domain.model.security.SecurityRole;

public interface SecurityRolePort {
    /**
     * Guarda un nuevo rol en el sistema o lo actualiza si ya existe por nombre.
     */
    SecurityRole saveOrUpdateRole(SecurityRole role);
}
