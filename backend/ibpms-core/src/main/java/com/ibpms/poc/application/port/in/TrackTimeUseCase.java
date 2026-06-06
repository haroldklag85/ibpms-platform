package com.ibpms.poc.application.port.in;

import com.ibpms.poc.domain.model.kanban.TimeLogEntry;
import java.util.UUID;

public interface TrackTimeUseCase {
    TimeLogEntry startTimer(UUID referenceId, String referenceType, String userId);
    TimeLogEntry stopTimer(UUID logId, String userId);
}
