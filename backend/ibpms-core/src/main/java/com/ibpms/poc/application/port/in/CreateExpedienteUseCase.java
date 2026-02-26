package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.ExpedienteDTO;

public interface CreateExpedienteUseCase {
    ExpedienteDTO create(ExpedienteDTO request);
}
