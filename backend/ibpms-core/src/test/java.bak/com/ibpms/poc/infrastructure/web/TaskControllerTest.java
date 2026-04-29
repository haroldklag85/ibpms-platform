package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.port.in.CompletarTareaUseCase;
import com.ibpms.poc.application.port.in.ListarTareasUseCase;
import com.ibpms.poc.application.port.in.ObtenerFormularioUseCase;
import com.ibpms.poc.application.port.in.ReclamarTareaUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListarTareasUseCase listarTareasUseCase;

    @MockBean
    private CompletarTareaUseCase completarTareaUseCase;

    @MockBean
    private ObtenerFormularioUseCase obtenerFormularioUseCase;

    @MockBean
    private ReclamarTareaUseCase reclamarTareaUseCase;

    @BeforeEach
    void setUp() {
        // Nada específico por ahora
    }

    @Test
    @WithMockUser(username = "maria.lopez", roles = "USER")
    void shouldClaimTaskSuccessfully() throws Exception {
        String taskId = "TK-099";

        // Mocks por defecto no hacen nada en métodos void, así que la llamada pasará
        doNothing().when(reclamarTareaUseCase).reclamar(taskId, "maria.lopez");

        mockMvc.perform(post("/tasks/{taskId}/claim", taskId)
                .with(java.util.Objects.requireNonNull(csrf()))) // Protege contra fallos de seguridad CSRF si están activos
                .andExpect(status().isOk());

        verify(reclamarTareaUseCase, times(1)).reclamar(taskId, "maria.lopez");
    }

    @Test
    @WithMockUser(username = "maria.lopez", roles = "USER")
    void shouldReturnConflictWhenTaskAlreadyClaimed() throws Exception {
        String taskId = "TK-099";

        doThrow(new IllegalStateException("La tarea ya fue asignada a otro usuario."))
                .when(reclamarTareaUseCase).reclamar(taskId, "maria.lopez");

        mockMvc.perform(post("/tasks/{taskId}/claim", taskId)
                .with(java.util.Objects.requireNonNull(csrf())))
                .andExpect(status().isConflict());

        verify(reclamarTareaUseCase, times(1)).reclamar(taskId, "maria.lopez");
    }

    @Test
    @WithMockUser(username = "maria.lopez", roles = "USER")
    void shouldReturnNotFoundWhenTaskMissing() throws Exception {
        String taskId = "TK-999";

        doThrow(new jakarta.persistence.EntityNotFoundException("La tarea no existe."))
                .when(reclamarTareaUseCase).reclamar(taskId, "maria.lopez");

        mockMvc.perform(post("/tasks/{taskId}/claim", taskId)
                .with(java.util.Objects.requireNonNull(csrf())))
                .andExpect(status().isNotFound());

        verify(reclamarTareaUseCase, times(1)).reclamar(taskId, "maria.lopez");
    }
}
