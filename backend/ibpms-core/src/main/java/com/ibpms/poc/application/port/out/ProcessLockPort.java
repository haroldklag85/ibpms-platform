package com.ibpms.poc.application.port.out;

import java.util.Optional;

public interface ProcessLockPort {
    Optional<ProcessLockInfo> findLock(String processKey);
    void saveLock(String processKey, String userId, String sessionId);
    void refreshLock(String processKey, String userId);
    void deleteLock(String processKey);
    
    record ProcessLockInfo(String processKey, String lockedBy, java.time.LocalDateTime lockedAt, String browserSessionId) {}
}
