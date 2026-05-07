package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.agile.AgileProject;
import com.ibpms.poc.infrastructure.persistence.AgileProjectRepositoryJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AgileProjectService {

    private final AgileProjectRepositoryJpa repository;

    public AgileProjectService(AgileProjectRepositoryJpa repository) {
        this.repository = repository;
    }

    // @Traceability: US-030 - CA-2: Arranque vacío
    @Transactional
    public AgileProject createProject(String name, String description, String createdBy) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del proyecto es obligatorio.");
        }

        AgileProject project = AgileProject.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .createdBy(createdBy)
                .build();
        
        return repository.save(project);
    }

    // @Traceability: US-030 - CA-7: Vista Proyecto + Vista Portafolio
    public Page<AgileProject> listProjects(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public AgileProject getProject(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
    }
}
