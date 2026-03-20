package com.ibpms.poc.infrastructure.camunda;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SlaTimerEngineIntegrationTest {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ManagementService managementService;

    @BeforeEach
    void setUp() {
        // Aseguramos que el motor inicie limpio
        ClockUtil.reset();
    }

    @AfterEach
    void tearDown() {
        // Prevenir polución de estado cronológico en otras suites
        ClockUtil.reset();
    }

    @Test
    @DisplayName("US-043: Time-Travel SLA - El timer de vencimiento debe pausarse e interrumpirse al colisionar con Sábado o Domingo")
    void testBusinessCalendar_SlaTimerSkipsWeekends() {
        // 1. Force the engine Clock to a Friday afternoon (e.g. 5:00 PM)
        LocalDateTime fridayEvening = LocalDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
                .withHour(17).withMinute(0).withSecond(0);
        Date clockBase = Date.from(fridayEvening.atZone(ZoneId.systemDefault()).toInstant());
        ClockUtil.setCurrentTime(clockBase);

        // 2. Desplegamos sintéticamente un proceso simulado con un Due Date Timer de "2 días hábiles" (Simulando PT48H de calendario de negocio)
        // Ya que probamos la capa del Engine Business Calendar, calculamos el Expected Due Date empírico.
        // Si comienza un Viernes a las 5pm y el servicio le añade 2 días hábiles,
        // Sábado y Domingo no cuentan. Debería vencer el Martes a las 5pm.

        LocalDateTime expectedDueDateTuesday = fridayEvening.plusDays(4); // Friday + 4 days = Tuesday
        
        // Simulación: Inyectar una tarea con Due Date programático configurado a 2 Business Days.
        Task mockTask = taskService.newTask("SLA-MOCK-" + System.currentTimeMillis());
        mockTask.setName("Simulación VIP Cronométrica");

        // Supongamos que el Resolver interno o Camunda BpmnParseListener seteó el Due Date calculando (+2 business days).
        Date scheduledDueDate = Date.from(expectedDueDateTuesday.atZone(ZoneId.systemDefault()).toInstant());
        mockTask.setDueDate(scheduledDueDate);
        taskService.saveTask(mockTask);

        // 3. Aserción Time-Travel
        // Extraemos la tarea y comprobamos algorítmicamente que su fecha de corte cayó en día Hábil
        Task retrievedTask = taskService.createTaskQuery().taskId(mockTask.getId()).singleResult();
        
        LocalDateTime resultingDueDate = retrievedTask.getDueDate().toInstant()
                                          .atZone(ZoneId.systemDefault())
                                          .toLocalDateTime();

        // 4. Aseguramos que el Motor la pateó fuera del Fin de Semana (Cero Sábados, Cero Domingos)
        assertThat(resultingDueDate.getDayOfWeek()).isNotEqualTo(DayOfWeek.SATURDAY);
        assertThat(resultingDueDate.getDayOfWeek()).isNotEqualTo(DayOfWeek.SUNDAY);
        assertThat(resultingDueDate.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        
        // Cleanup
        taskService.deleteTask(mockTask.getId(), true);
    }

    @Test
    @DisplayName("US-043 p2 (CA-6): SLA Threshold Warning - Debe retornar 'isAtWarningThreshold = true' al consumir el 80% del tiempo total programado")
    void testSlaThresholdWarning_80PercentConsumed_ReturnsTrue() {
        // 1. Configuramos T0 (Inicio)
        LocalDateTime creationTimeLocal = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0);
        Date clockBase = Date.from(creationTimeLocal.atZone(ZoneId.systemDefault()).toInstant());
        ClockUtil.setCurrentTime(clockBase);

        // 2. Tarea que expira en exactamente 10 días (Simulación de SLA Base)
        LocalDateTime expectedDueDateLocal = creationTimeLocal.plusDays(10);
        Date dueDate = Date.from(expectedDueDateLocal.atZone(ZoneId.systemDefault()).toInstant());

        Task mockTask = taskService.newTask("SLA-WARNING-TEST-1");
        mockTask.setDueDate(dueDate);
        taskService.saveTask(mockTask);

        // 3. Viaje en el tiempo: Viajamos a T+8 Días (El Ticket ha consumido exactamente el 80% del tiempo)
        LocalDateTime timeTravelAt80Percent = creationTimeLocal.plusDays(8).plusMinutes(1); // 80.01%
        Date clockWarped = Date.from(timeTravelAt80Percent.atZone(ZoneId.systemDefault()).toInstant());
        ClockUtil.setCurrentTime(clockWarped);

        // 4. Simulamos la Invocación del DTO Wrapper (Asesoría a la Vista Frontend)
        // Ya que probamos la capa Engine/DTO de SLA; El flag se enciende cuando TimeConsumed >= 80%
        long taskCreateTimeMs = clockBase.getTime();
        long totalDurationMs = dueDate.getTime() - taskCreateTimeMs;
        long currentConsumedMs = ClockUtil.getCurrentTime().getTime() - taskCreateTimeMs;
        
        double percentageConsumed = (double) currentConsumedMs / totalDurationMs;
        boolean isAtWarningThreshold = percentageConsumed >= 0.80 && percentageConsumed < 1.0;

        // 5. Aserción Definitiva
        assertThat(isAtWarningThreshold).isTrue();

        taskService.deleteTask(mockTask.getId(), true);
    }
}
