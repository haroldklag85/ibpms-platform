// @Traceability: US-005, CA-28 Concurrencia Atómica
package com.ibpms.poc.application.service;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.application.port.in.AttendNextTaskUseCase;
import com.ibpms.poc.application.dto.WorkdeskGlobalItemDTO;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.entity.FeatureToggleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import com.ibpms.poc.infrastructure.jpa.repository.FeatureToggleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Attend Next Concurrency Integration Test")
public class AttendNextConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AttendNextTaskUseCase attendNextTaskUseCase;

    @Autowired
    private WorkdeskProjectionRepository workdeskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeatureToggleRepository featureToggleRepository;

    @BeforeEach
    void setUp() {
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        for (int i = 1; i <= 10; i++) {
            workdeskRepository.findById("task_concur_" + i).ifPresent(workdeskRepository::delete);
        }
        for (int i = 1; i <= 4; i++) {
            userRepository.findByUsername("user_c" + i).ifPresent(userRepository::delete);
            featureToggleRepository.findByTenantIdAndToggleKey("user_c" + i, "FORCE_ROUTING")
                    .ifPresent(featureToggleRepository::delete);
        }
        featureToggleRepository.findByTenantIdAndToggleKey("tenant_alpha", "FORCE_ROUTING")
                .ifPresent(featureToggleRepository::delete);
    }

    @Test
    @DisplayName("Should assign distinct tasks concurrently to 4 users without errors")
    void shouldAssignDistinctTasksConcurrently() throws InterruptedException {
        // 1. Habilitar feature toggle FORCE_ROUTING en DB para tenant_alpha y usuarios correspondientes
        FeatureToggleEntity toggleAlpha = new FeatureToggleEntity();
        toggleAlpha.setTenantId("tenant_alpha");
        toggleAlpha.setToggleKey("FORCE_ROUTING");
        toggleAlpha.setEnabled(true);
        toggleAlpha.setChangedBy("test");
        toggleAlpha.setChangedAt(LocalDateTime.now());
        featureToggleRepository.save(toggleAlpha);

        for (int i = 1; i <= 4; i++) {
            FeatureToggleEntity toggleUser = new FeatureToggleEntity();
            toggleUser.setTenantId("user_c" + i);
            toggleUser.setToggleKey("FORCE_ROUTING");
            toggleUser.setEnabled(true);
            toggleUser.setChangedBy("test");
            toggleUser.setChangedAt(LocalDateTime.now());
            featureToggleRepository.save(toggleUser);
        }

        // 2. Crear e insertar 4 usuarios en userRepository
        for (int i = 1; i <= 4; i++) {
            UserEntity user = new UserEntity();
            user.setUsername("user_c" + i);
            user.setEmail("user_c" + i + "@alpha.com");
            user.setStatus(UserStatus.ACTIVE);
            user.setIsExternalIdp(false);
            user.setMustChangePassword(false);
            user.setSkills("[\"VIP_SUPPORT\"]");
            userRepository.save(user);
        }

        // 3. Insertar 10 tareas disponibles compartiendo el tenant "tenant_alpha" sin asignar
        for (int i = 1; i <= 10; i++) {
            WorkdeskProjectionEntity task = new WorkdeskProjectionEntity();
            task.setId("task_concur_" + i);
            task.setSourceSystem("BPMN");
            task.setOriginalTaskId("orig_concur_" + i);
            task.setTitle("Concurrency Task " + i);
            task.setTenantId("tenant_alpha");
            task.setImpactLevel(10 - i);
            task.setStatus("ACTIVE");
            task.setAssignee(null);
            task.setCategoryTag("VIP_SUPPORT");
            workdeskRepository.save(task);
        }

        // 4. Disparar 4 hilos concurrentes que invoquen attendNext(userId)
        int threadCount = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        List<WorkdeskGlobalItemDTO> results = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= threadCount; i++) {
            final String userId = "user_c" + i;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // esperar disparo simultáneo
                    WorkdeskGlobalItemDTO task = attendNextTaskUseCase.attendNext(userId);
                    results.add(task);
                } catch (Throwable t) {
                    exceptions.add(t);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Iniciar concurrentemente
        startLatch.countDown();

        // Esperar fin de hilos
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertTrue(completed, "Hilos no completaron en tiempo estimado");

        // 5. Validar que no ocurran excepciones y que cada operario reciba una tarea única
        if (!exceptions.isEmpty()) {
            for (Throwable t : exceptions) {
                System.err.println("Exception in test thread: " + t.getMessage());
                t.printStackTrace();
            }
        }

        assertTrue(exceptions.isEmpty(), "No deben ocurrir errores en los hilos concurrentes. Errores: " + exceptions);
        assertEquals(4, results.size(), "Deberían haberse asignado exactamente 4 tareas");

        Set<String> taskIds = new HashSet<>();
        for (WorkdeskGlobalItemDTO dto : results) {
            assertNotNull(dto.getUnifiedId(), "El ID de la tarea asignada no debe ser nulo");
            taskIds.add(dto.getUnifiedId());
        }
        assertEquals(4, taskIds.size(), "Cada operario debe recibir una tarea distinta y única (sin colisiones)");
    }
}
