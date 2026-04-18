package com.ibpms.poc.domain.port;

import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.model.EventType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormEventRepository {
    FormEvent save(FormEvent event);
    Optional<FormEvent> findById(UUID id);
    List<FormEvent> findAll();
    List<FormEvent> findByProcessInstanceIdAndEventType(String processInstanceId, EventType eventType);
    void deleteAll();
}
