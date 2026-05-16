package com.ibpms.poc.application.ports.out;

import java.util.UUID;

public interface ImpersonationPort {
    void logImpersonationEvent(UUID adminId, UUID targetUserId, String action, String ipAddress, String userAgent);
    boolean isUserImpersonable(UUID targetUserId);
    String generateImpersonationToken(UUID adminId, UUID targetUserId);
    UUID getUserIdByUsername(String username);
}
