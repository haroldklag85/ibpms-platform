package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.security.SecurityRole;
import java.util.List;

public interface SecurityRolePort {
    List<SecurityRole> findByIsVipRestrictedTrue();
}
