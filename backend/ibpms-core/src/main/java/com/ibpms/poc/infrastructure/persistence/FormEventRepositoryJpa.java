package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.port.FormEventRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import com.ibpms.poc.domain.model.EventType;
import java.util.UUID;

@Repository
public class FormEventRepositoryJpa implements FormEventRepository {

    private final SpringDataFormEventRepository springDataRepository;

    public FormEventRepositoryJpa(SpringDataFormEventRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public FormEvent save(FormEvent event) {
        return springDataRepository.save(event);
    }

    @Override
    public Optional<FormEvent> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public List<FormEvent> findAll() {
        return springDataRepository.findAll();
    }

    @Override
    public List<FormEvent> findByProcessInstanceIdAndEventType(String processInstanceId, EventType eventType) {
        return springDataRepository.findByProcessInstanceIdAndEventType(processInstanceId, eventType);
    }

    @Override
    public void deleteAll() {
        springDataRepository.deleteAll();
    }
}

interface SpringDataFormEventRepository extends JpaRepository<FormEvent, UUID> {
    List<FormEvent> findByProcessInstanceIdAndEventType(String processInstanceId, EventType eventType);
}
