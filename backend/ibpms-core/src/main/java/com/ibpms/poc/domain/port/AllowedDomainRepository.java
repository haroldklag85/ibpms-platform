package com.ibpms.poc.domain.port;

import com.ibpms.poc.domain.model.AllowedDomain;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
 * Hexagonal port for allowed domain whitelist persistence (US-004 CA-4, CA-12).
 */
public interface AllowedDomainRepository {
    AllowedDomain save(AllowedDomain domain);
    Optional<AllowedDomain> findById(UUID id);
    List<AllowedDomain> findByTenantIdAndIsActiveTrue(String tenantId);
    boolean existsByDomainAndTenantIdAndIsActiveTrue(String domain, String tenantId);
    void deleteAll();
}
