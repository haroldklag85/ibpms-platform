package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.ManualStartDTO;
import com.ibpms.poc.application.port.in.IniciarServicioManualUseCase;
import com.ibpms.poc.application.port.out.ExpedienteRepositoryPort;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import com.ibpms.poc.domain.model.Expediente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class IniciarServicioManualService implements IniciarServicioManualUseCase {

    private final ProcesoBpmPort procesoBpmPort;
    private final ExpedienteRepositoryPort expedienteRepositoryPort;

    public IniciarServicioManualService(ProcesoBpmPort procesoBpmPort,
            ExpedienteRepositoryPort expedienteRepositoryPort) {
        this.procesoBpmPort = procesoBpmPort;
        this.expedienteRepositoryPort = expedienteRepositoryPort;
    }

    @Override
    @Transactional
    public UUID iniciarServicio(ManualStartDTO request) {

        // 1. Crear Entidad de Dominio
        Expediente expediente = Expediente.iniciarNuevo(
                request.getDefinitionKey(),
                request.getBusinessKey(),
                request.getType(),
                request.getInitialVariables());

        // 2. Persistir Inicial para obtener ID (Opcional, pero ancla la DB)
        Expediente guardado = expedienteRepositoryPort.save(expediente);

        // 3. Orquestar el BPM
        String processInstanceId = procesoBpmPort.iniciarProceso(
                request.getDefinitionKey(),
                guardado.getId().toString(), // Usamos el ID interno como clave de negocio en Camunda si no hay otra
                request.getInitialVariables());

        // 4. Vincular y re-persistir
        guardado = guardado.vincularProceso(processInstanceId);
        expedienteRepositoryPort.save(guardado);

        return guardado.getId();
    }
}
