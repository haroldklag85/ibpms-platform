package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.TaskDTO;
import java.util.List;

public interface ListarTareasUseCase {
    /**
     * Devuelve las tareas del usuario aplicando paginación y ordenamiento básico.
     * En V1.1 esto incluirá filtros ABAC según los roles del JWT.
     */
    List<TaskDTO> listar(int limit, int offset, String status, Integer priority);
}
