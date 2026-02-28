package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.UiTemplate;

import java.util.List;
import java.util.Optional;

public interface UiTemplatePersistencePort {

    UiTemplate save(UiTemplate template);

    Optional<UiTemplate> findLatestByName(String name);

    List<UiTemplate> findAllVersionsByName(String name);

    boolean existsByNameAndVersion(String name, String version);
}
