package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import com.ibpms.poc.domain.exception.TaskAlreadyClaimedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReclamarTareaServiceTest {

    @Mock
    private ProcesoBpmPort procesoBpmPort;

    @InjectMocks
    private ReclamarTareaService service;

    // ─── Caso Feliz ───────────────────────────────────────────────
    @Test
    @DisplayName("Debe reclamar (claim) una tarea exitosamente cuando no tiene assignee previo")
    void reclamar_Exitoso_CuandoTareaDisponible() {
        // Arrange
        String taskId = "TK-001";
        String username = "maria.operativa";

        // Act
        service.reclamar(taskId, username);

        // Assert: El puerto BPM fue invocado exactamente una vez con los datos
        // correctos
        verify(procesoBpmPort, times(1)).reclamarTarea(taskId, username);
    }

    // ─── Caso Conflicto (HTTP 409) ────────────────────────────────
    @Test
    @DisplayName("Debe lanzar TaskAlreadyClaimedException cuando la tarea ya tiene assignee (doble-click / concurrencia)")
    void reclamar_Falla_CuandoTareaYaTieneAssignee() {
        // Arrange
        String taskId = "TK-002";
        String username = "carlos.operativo";

        // Simulamos que el adaptador de Camunda detecta que la tarea ya tiene dueño
        doThrow(new TaskAlreadyClaimedException(
                "La tarea TK-002 ya fue reclamada por otro usuario."))
                .when(procesoBpmPort).reclamarTarea(taskId, username);

        // Act & Assert
        TaskAlreadyClaimedException exception = assertThrows(
                TaskAlreadyClaimedException.class,
                () -> service.reclamar(taskId, username));

        assertTrue(exception.getMessage().contains("ya fue reclamada"));
        verify(procesoBpmPort, times(1)).reclamarTarea(taskId, username);
    }

    // ─── Caso Edge: taskId nulo ───────────────────────────────────
    @Test
    @DisplayName("Debe propagar la excepción si el puerto recibe un taskId inexistente")
    void reclamar_Falla_CuandoTareaNoExiste() {
        // Arrange
        String taskId = "TK-INEXISTENTE";
        String username = "pedro.fantasma";

        doThrow(new RuntimeException("Task not found: TK-INEXISTENTE"))
                .when(procesoBpmPort).reclamarTarea(taskId, username);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.reclamar(taskId, username));

        assertTrue(exception.getMessage().contains("Task not found"));
    }
}
