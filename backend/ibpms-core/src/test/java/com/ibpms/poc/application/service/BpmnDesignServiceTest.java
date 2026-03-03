package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.exception.ProcessDesignLockedException;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BpmnDesignServiceTest {

    @Mock
    private BpmnProcessDesignRepository designRepository;

    @Mock
    private BpmnDesignAuditLogRepository auditRepository;

    @InjectMocks
    private BpmnDesignService service;

    private BpmnProcessDesignEntity mockEntity;
    private final UUID processId = UUID.randomUUID();
    private final String designer1 = "ana.garcia@ibpms.co";
    private final String designer2 = "carlos.m@ibpms.co";

    @BeforeEach
    void setUp() {
        mockEntity = new BpmnProcessDesignEntity();
        mockEntity.setId(processId);
        mockEntity.setLockedBy(designer1);
        mockEntity.setLockedAt(LocalDateTime.now());
    }

    // ── QA Instruction 1: Test Lock Pesimista ──
    @Test
    @DisplayName("Debe lanzar ProcessDesignLockedException cuando un usuario intenta editar un proceso bloqueado por otro usuario")
    void acquireLock_Falla_CuandoEstaBloqueadoPorOtro() {
        // Arrange
        when(designRepository.findById(processId)).thenReturn(Optional.of(mockEntity));

        // Act & Assert
        ProcessDesignLockedException ex = assertThrows(ProcessDesignLockedException.class, () -> {
            service.acquireLock(processId, designer2);
        });

        assertTrue(ex.getMessage().contains("está bloqueado por " + designer1));
        verify(designRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe otorgar el Lock si el bloqueo del otro usuario ya expiró (>30 mins)")
    void acquireLock_Exitoso_CuandoBloqueoExpiro() {
        // Arrange: Carlos tuvo el lock hace más de 30 mins
        mockEntity.setLockedBy(designer2);
        mockEntity.setLockedAt(LocalDateTime.now().minusMinutes(35));
        when(designRepository.findById(processId)).thenReturn(Optional.of(mockEntity));

        // Act: Ana pide el lock
        service.acquireLock(processId, designer1);

        // Assert
        assertEquals(designer1, mockEntity.getLockedBy());
        assertNotNull(mockEntity.getLockedAt());
        // Se debió auto-liberar y luego guardar 2 veces en la lógica (autoRelease +
        // acquire)
        verify(designRepository, atLeast(1)).save(mockEntity);
    }
}
