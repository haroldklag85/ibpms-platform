package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.SkipReasonDTO;
import com.ibpms.poc.application.dto.WorkdeskGlobalItemDTO;
import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * Puerto de entrada para el caso de uso "Atender Siguiente Tarea".
 */
@Traceability(US = "US-001", CA = {"CA-28", "CA-21", "CA-16"})
public interface AttendNextTaskUseCase {
    @Traceability(US = "US-001", CA = {"CA-28", "CA-21", "CA-16"})
    WorkdeskGlobalItemDTO attendNext(String userId);

    @Traceability(US = "US-001", CA = {"CA-28", "CA-21", "CA-16"})
    WorkdeskGlobalItemDTO skipAndAttendNext(String userId, SkipReasonDTO skipReason);
}
