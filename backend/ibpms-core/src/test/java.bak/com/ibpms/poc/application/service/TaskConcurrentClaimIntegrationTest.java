package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

import com.ibpms.poc.infrastructure.config.TestcontainersBaseIT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskConcurrentClaimIntegrationTest extends TestcontainersBaseIT {

    @Autowired
    private AgileTaskService agileTaskService;

    @Autowired
    private AgileTaskRepositoryJpa agileTaskRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void testConcurrentClaim_returnsNotFoundOrLocks_forThreadB() throws InterruptedException {
        // Setup: Crear tarea inicial
        AgileTask initialTask = AgileTask.builder()
                .id(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .title("Concurrent Claim Test Task")
                .description("Testing pessimistic locks")
                .effortEstimated(BigDecimal.valueOf(5))
                .createdBy("admin")
                .status("PENDING")
                .build();
        agileTaskRepository.save(initialTask);
        UUID taskId = initialTask.getId();

        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        AtomicReference<Exception> threadBException = new AtomicReference<>();
        AtomicReference<String> successfulAssignee = new AtomicReference<>();

        Runnable claimTask = () -> {
            try {
                startLatch.await();
                transactionTemplate.execute(status -> {
                    // PESSIMISTIC_WRITE con SKIP LOCKED causará que el 2do hilo no encuentre la fila = Optional.empty()
                    // o lance alguna excepción dependiente de Hibernate.
                    try {
                        AgileTask task = agileTaskService.getTaskForUpdate(taskId);
                        String threadName = Thread.currentThread().getName();
                        task.getAssigneeIds().add(threadName);
                        agileTaskRepository.save(task);
                        successfulAssignee.set(threadName);
                        // Sleep to ensure the lock is held while thread B attempts
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        threadBException.set(e);
                        status.setRollbackOnly();
                    }
                    return null;
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };

        executor.submit(claimTask);
        executor.submit(claimTask);

        // Disparar hilos simultáneamente
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // Validaciones: Solo un hilo pudo lograrlo
        AgileTask finalTask = agileTaskRepository.findById(taskId).orElseThrow();
        
        assertThat(finalTask.getAssigneeIds()).hasSize(1);
        assertThat(finalTask.getAssigneeIds().iterator().next()).isEqualTo(successfulAssignee.get());

        // El hilo B debe tener una excepción del tipo ResponseStatusException debido al SKIP LOCKED
        // o un EntityNotFound Exception.
        assertThat(threadBException.get())
                .isNotNull()
                .isInstanceOf(ResponseStatusException.class);
    }
}
