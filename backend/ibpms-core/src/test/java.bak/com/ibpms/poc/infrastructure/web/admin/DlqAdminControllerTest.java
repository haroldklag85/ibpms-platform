package com.ibpms.poc.infrastructure.web.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DlqAdminControllerTest {

    @Mock
    private RabbitAdmin rabbitAdmin;

    @InjectMocks
    private DlqAdminController controller;

    private Properties mockProperties;

    @BeforeEach
    void setUp() {
        mockProperties = new Properties();
        mockProperties.put("QUEUE_MESSAGE_COUNT", 42);
    }

    @Test
    void getDlqSummary_ShouldReturnMessageCountAndActiveStatus() {
        when(rabbitAdmin.getQueueProperties("ibpms.dlq.global")).thenReturn(mockProperties);

        ResponseEntity<Map<String, Object>> response = controller.getDlqSummary();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        Map<String, Object> body = Objects.requireNonNull(response.getBody());
        assertThat(body.get("message_count")).isEqualTo(42);
        assertThat(body.get("status")).isEqualTo("ACTIVE");
    }

    @Test
    void retryMessages_ShouldReturnOkStatus() {
        ResponseEntity<String> response = controller.retryMessages();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void purgeDlq_ShouldCallPurgeOnRabbitAdmin() {
        ResponseEntity<String> response = controller.purgeDlq();
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(rabbitAdmin).purgeQueue(eq("ibpms.dlq.global"), anyBoolean());
    }

    // --- REQUISITOS DE AUDITORÍA (DEF-02 Y DEF-03) ---

    @Test
    void verifySecurityRbacAnnotationsArePresent() throws NoSuchMethodException {
        // Asserting DEF-02 (Falta de @PreAuthorize)
        // El test fue instruido por el Arquitecto para asertar la presencia requerida, fallará hasta que el Backend corrija.
        Method purgeMethod = DlqAdminController.class.getMethod("purgeDlq");
        PreAuthorize purgeAuth = purgeMethod.getAnnotation(PreAuthorize.class);
        
        assertThat(purgeAuth).as("DEF-02: El endpoint de purga debe estar protegido por ADMIN_IT").isNotNull();
        assertThat(purgeAuth.value()).contains("hasRole('ADMIN_IT')");

        Method retryMethod = DlqAdminController.class.getMethod("retryMessages");
        PreAuthorize retryAuth = retryMethod.getAnnotation(PreAuthorize.class);
        
        assertThat(retryAuth).as("DEF-02: El endpoint de reintento debe estar protegido por ADMIN_IT").isNotNull();
        assertThat(retryAuth.value()).contains("hasRole('ADMIN_IT')");
    }

    // Nota para DEF-03:
    // Para probar que haya logs en ibpms_audit_log, idealmente inyectaríamos un AuditService mock.
    // Como la clase actual ni siquiera tiene el servicio inyectado, un test que trate de asertar interacciones
    // requeriría Reflection o esperar a la corrección del Backend. Documentamos que la validación
    // de ibpms_audit_log requiere la inyección del AuditService.
}
