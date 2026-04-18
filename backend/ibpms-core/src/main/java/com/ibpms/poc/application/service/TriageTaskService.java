package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.TriageTask;
import com.ibpms.poc.domain.port.TriageTaskRepository;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class TriageTaskService {

    private final TriageTaskRepository triageTaskRepository;
    private final RuntimeService runtimeService;

    public TriageTaskService(TriageTaskRepository triageTaskRepository, RuntimeService runtimeService) {
        this.triageTaskRepository = triageTaskRepository;
        this.runtimeService = runtimeService;
    }

    public Page<TriageTask> listTasks(String status, Pageable pageable) {
        return triageTaskRepository.findByStatus(status != null ? status : "PENDING", pageable);
    }

    @Transactional
    public TriageTask approveTask(UUID id, String processType) {
        if (processType == null || processType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requiere especificar processType.");
        }

        TriageTask task = triageTaskRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe tarea de triaje con ID (o bloqueada) " + id));

        if (!"PENDING".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La tarea ya no está pendiente.");
        }

        // 1. Iniciar nuevo proceso
        try {
            runtimeService.startProcessInstanceByKey(processType);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallo al iniciar el proceso " + processType + " en Camunda");
        }

        // 2. Terminar proceso temporal antiguo
        try {
            runtimeService.deleteProcessInstance(task.getCamundaProcessInstanceId(), "Triaje Aprobado");
        } catch (Exception e) {
            // Log & ignore if already finished
        }

        task.setStatus("APPROVED");
        return triageTaskRepository.save(task);
    }

    @Transactional
    public TriageTask rejectTask(UUID id, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo de rechazo es obligatorio.");
        }

        TriageTask task = triageTaskRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe tarea de triaje con ID (o bloqueada) " + id));

        if (!"PENDING".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La tarea ya no está pendiente.");
        }

        // Eliminar instancia temporal en Camunda
        try {
            runtimeService.deleteProcessInstance(task.getCamundaProcessInstanceId(), "Triaje Rechazado: " + rejectionReason);
        } catch (Exception e) {
            // Ignorar
        }

        task.setStatus("REJECTED");
        task.setRejectionReason(rejectionReason);
        return triageTaskRepository.save(task);
    }
}
