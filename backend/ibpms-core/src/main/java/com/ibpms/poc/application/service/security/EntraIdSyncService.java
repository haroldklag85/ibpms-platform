package com.ibpms.poc.application.service.security;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * Service to handle synchronization with Microsoft EntraID (SSO).
 * Part of CA-01 US-036: Dual Motor Identity Mapping.
 */
@Service
public class EntraIdSyncService {

    /**
     * Fetches available groups from EntraID.
     * In V1, this returns a realistic set of groups based on naming conventions.
     */
    public List<Map<String, String>> fetchAvailableGroups() {
        // Simulation of Microsoft Graph API response
        return List.of(
            Map.of("id", "85822f30-84c6-4d05-9f5b-111111111111", "displayName", "GG_IBPMS_Process_Architects"),
            Map.of("id", "22222222-3333-4444-5555-666666666666", "displayName", "GG_IBPMS_Compliance_Managers"),
            Map.of("id", "77777777-8888-9999-0000-aaaaaaaaaaaa", "displayName", "GG_IBPMS_Standard_Users")
        );
    }
}
