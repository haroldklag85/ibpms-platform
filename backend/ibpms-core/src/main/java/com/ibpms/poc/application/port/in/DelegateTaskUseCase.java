package com.ibpms.poc.application.port.in;

public interface DelegateTaskUseCase {

    /**
     * Permite a un humano o sistema partir una tarea Kanban (Intake o parte de
     * proceso) en N subtareas dinámicas.
     * 
     * @param parentTaskId ID de su tarea actual
     * @param subTaskName  Título de la subtarea a registrar
     * @param assignee     Correo del usuario o grupo responsable
     * @return El ID de la nueva sub-tarea registrada.
     */
    String delegateSubTask(String parentTaskId, String subTaskName, String assignee);
}
