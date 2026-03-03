package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.ReclamarTareaUseCase;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReclamarTareaService implements ReclamarTareaUseCase {

    private final ProcesoBpmPort procesoBpmPort;

    public ReclamarTareaService(ProcesoBpmPort procesoBpmPort) {
        this.procesoBpmPort = procesoBpmPort;
    }

    @Override
    @Transactional
    public void reclamar(String taskId, String username) {
        // La validación de si la tarea existe debe recaer en el adaptador o una query
        // específica.
        // Asumiendo que ProcesoBpmPort maneja NotFound o lo asume, para esto la
        // delegación va directa.
        // Si es necesario validar, idealmente sería con `TareaRepositoryPort`.
        // Para simplificar y cumplir Hexagonal, delegamos la operación al motor nativo
        // que fallará si no existe.
        procesoBpmPort.reclamarTarea(taskId, username);
    }
}
