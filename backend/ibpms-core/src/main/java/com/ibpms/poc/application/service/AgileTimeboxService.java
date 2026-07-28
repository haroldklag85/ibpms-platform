package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.agile.AgileTimebox;
import com.ibpms.poc.infrastructure.persistence.AgileTimeboxRepositoryJpa;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de gestión de Timeboxes (Sprints) dentro de un AgileProject.
 * Iteración 2: Implementación Mock funcional para estabilizar contrato API.
 */
@Service
public class AgileTimeboxService {

    private final AgileTimeboxRepositoryJpa repository;

    public AgileTimeboxService(AgileTimeboxRepositoryJpa repository) {
        this.repository = repository;
    }

    @Transactional
    public AgileTimebox createTimebox(UUID projectId, String name, String goal,
                                      java.time.LocalDate startDate, java.time.LocalDate endDate,
                                      String createdBy) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del Timebox es obligatorio.");
        }
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las fechas de inicio y fin son obligatorias.");
        }
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        AgileTimebox timebox = AgileTimebox.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .name(name)
                .goal(goal)
                .startDate(startDate)
                .endDate(endDate)
                .createdBy(createdBy)
                .build();

        return repository.save(timebox);
    }

    public List<AgileTimebox> listTimeboxes(UUID projectId) {
        return repository.findByProjectIdOrderByStartDateAsc(projectId);
    }

    public AgileTimebox getTimebox(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Timebox no encontrado"));
    }
}
