package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.ReclamarTareaUseCase;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.TaskAlreadyClaimedException;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReclamarTareaService implements ReclamarTareaUseCase {

    private final TaskService taskService;

    public ReclamarTareaService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    @Transactional
    public void reclamar(String taskId, String username) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (task == null) {
            throw new jakarta.persistence.EntityNotFoundException("La tarea no existe.");
        }

        try {
            taskService.claim(taskId, username);
        } catch (TaskAlreadyClaimedException e) {
            throw new IllegalStateException("La tarea ya fue asignada a otro usuario.");
        }
    }
}
