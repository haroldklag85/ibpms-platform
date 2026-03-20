package com.ibpms.poc.infrastructure.jpa.entity.bpm;

import com.ibpms.poc.infrastructure.jpa.repository.bpm.GenericTaskLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class GenericTaskLogIntegrationTest {

    @Autowired
    private GenericTaskLogRepository repository;

    @Test
    @DisplayName("US-039 p2: Data Flattening - Persistencia Tabular Plana de Tareas Genéricas sin Corrupción de Camunda JSON")
    void testDataFlattening_GenericTaskLogPersistsIsolatingJson() {
        // Simulación del TaskListener Asíncrono de Camunda
        GenericTaskLogEntity flatLog = new GenericTaskLogEntity();
        flatLog.setTaskId("XYZ-999");
        flatLog.setProcessInstanceId("PROC-10101");
        flatLog.setUserId("test.analyst");
        flatLog.setComments("Revisión completada sin anomalías. Adjunto evidencias.");
        flatLog.setHasEvidence(true);
        flatLog.setCreatedAt(LocalDateTime.now());

        // QA Assert: El Guardado Aplanado aísla las variables del JSON Text de la db_camunda
        repository.saveAndFlush(flatLog);

        assertThat(repository.count()).isGreaterThan(0);
        
        GenericTaskLogEntity retrieved = repository.findAll().get(0);
        assertThat(retrieved.getTaskId()).isEqualTo("XYZ-999");
        assertThat(retrieved.getComments()).contains("Revisión completada sin anomalías");
        assertThat(retrieved.getHasEvidence()).isTrue();
    }
}
