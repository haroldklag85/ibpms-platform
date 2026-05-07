package com.ibpms.poc.application.usecase.security;

import com.ibpms.poc.application.ports.out.ImpersonationPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ImpersonationUseCase {

    private final ImpersonationPort impersonationPort;

    public ImpersonationUseCase(ImpersonationPort impersonationPort) {
        this.impersonationPort = impersonationPort;
    }

    public String startImpersonation(UUID adminId, UUID targetUserId, HttpServletRequest request) {
        if (!impersonationPort.isUserImpersonable(targetUserId)) {
            throw new IllegalArgumentException("Target user is not impersonable or has SUPER_ADMIN role");
        }

        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        impersonationPort.logImpersonationEvent(adminId, targetUserId, "START", ipAddress, userAgent);

        return impersonationPort.generateImpersonationToken(adminId, targetUserId);
    }

    public String exitImpersonation(UUID adminId, UUID targetUserId, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        impersonationPort.logImpersonationEvent(adminId, targetUserId, "EXIT", ipAddress, userAgent);

        return impersonationPort.generateImpersonationToken(null, adminId);
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}
