package com.ibpms.poc.domain.port;

import java.util.UUID;

public interface DocumentSecurityPort {
    /**
     * Checks if the document belongs to the taskId and userId. 
     * If so, marks it as CONFIRMED. If not, throws an exception or handles access denied.
     * @param documentId the UUID of the document
     * @param taskId the associated task id
     * @param userId the user who claims ownership
     */
    void confirmOwnershipAndMarkConfirmed(UUID documentId, String taskId, String userId);
}
