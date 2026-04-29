package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.port.out.ProcessLockPort;
import com.ibpms.poc.infrastructure.jpa.entity.ProcessLockEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ProcessLockRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class ProcessLockJpaAdapter implements ProcessLockPort {

    private final ProcessLockRepository repository;

    public ProcessLockJpaAdapter(ProcessLockRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ProcessLockInfo> findLock(String processKey) {
        return repository.findById(processKey)
                .map(e -> new ProcessLockInfo(
                        e.getProcessDefinitionKey(),
                        e.getLockedBy(),
                        e.getLockedAt(),
                        e.getBrowserSessionId()
                ));
    }

    @Override
    public void saveLock(String processKey, String userId, String sessionId) {
        ProcessLockEntity entity = new ProcessLockEntity();
        entity.setProcessDefinitionKey(processKey);
        entity.setLockedBy(userId);
        entity.setLockedAt(LocalDateTime.now());
        entity.setBrowserSessionId(sessionId);
        repository.save(entity);
    }

    @Override
    public void deleteLock(String processKey) {
        repository.deleteById(processKey);
    }

    @Override
    public void refreshLock(String processKey, String userId) {
        ProcessLockEntity lock = repository.findById(processKey)
            .orElseThrow(() -> new IllegalStateException("El proceso no está bloqueado."));
        if (!lock.getLockedBy().equals(userId)) {
            throw new IllegalStateException("Solo " + lock.getLockedBy() + " puede renovar el bloqueo.");
        }
        lock.setLockedAt(LocalDateTime.now());
        repository.save(lock);
    }
}
