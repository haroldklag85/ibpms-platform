// @Traceability: US-003 - ADR-001​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
package com.ibpms.poc.domain.model;

import java.util.List;

public record DomainPage<T>(
    List<T> content,
    long totalElements,
    int page,
    int size
) {}
