package com.ibpms.poc.infrastructure.config;

import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@Profile({ "dev", "e2e", "Zero-Mock-E2E" })
@RequiredArgsConstructor
public class E2EDataSeedConfig implements CommandLineRunner {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final WorkdeskProjectionRepository projectionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("🚀 Inicializando E2E Data Seed para motor Camunda...");

        // Evitar duplicación si el proceso ya está desplegado en un reinicio rápido
        long deploymentCount = repositoryService.createDeploymentQuery()
                .deploymentName("e2e-dummy-deployment")
                .count();

        if (deploymentCount == 0) {
            log.info("⚙️ Generando proceso BPMN Dummy programáticamente...");

            // Construir proceso dinámicamente con tareas para diferentes roles y
            // sys_generic_form
            BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("e2e_dummy_process")
                    .name("E2E Dummy Process")
                    .startEvent("startEvent")
                    .parallelGateway("fork")

                    // Rama 1: Tarea para Adjusters (se va a la cola grupal)
                    .userTask("task_adjusters").name("Auditar Información Siniestro")
                    .camundaCandidateGroups("Adjusters")
                    .camundaFormKey("sys_generic_form")
                    .endEvent()

                    // Rama 2: Tarea para Perito A (Multi-instance behavior simulada)
                    .moveToNode("fork")
                    .userTask("task_perito_a").name("Evaluar Daños Dinámicamente")
                    .camundaAssignee("perito_a")
                    .camundaFormKey("sys_generic_form")
                    .endEvent()

                    // Rama 2b: Tarea para Perito B (Multi-instance behavior simulada)
                    .moveToNode("fork")
                    .userTask("task_perito_b").name("Evaluar Daños Dinámicamente")
                    .camundaAssignee("perito_b")
                    .camundaFormKey("sys_generic_form")
                    .endEvent()

                    // Rama 3: Tarea para Directors (Cola grupal)
                    .moveToNode("fork")
                    .userTask("task_directors").name("Firma Final (Director)")
                    .camundaCandidateGroups("Directors")
                    .camundaFormKey("sys_generic_form")
                    .endEvent()

                    .done();

            // Desplegar el modelo
            repositoryService.createDeployment()
                    .name("e2e-dummy-deployment")
                    .addModelInstance("e2e_dummy_process.bpmn", modelInstance)
                    .deploy();

            log.info("✅ Proceso BPMN desplegado. Instanciando 3 instancias de prueba...");

            // Crear 3 instancias para tener volumen en la bandeja
            for (int i = 0; i < 3; i++) {
                runtimeService.startProcessInstanceByKey("e2e_dummy_process");
            }
        } else {
            log.info("✅ El Data Seed E2E ya está desplegado. Saltando BPMN...");
        }

        if (projectionRepository.findById("BPMN-9901").isEmpty()) {
            log.info("⚙️ Generando Data Seed para Workdesk Projections...");

            WorkdeskProjectionEntity w1 = new WorkdeskProjectionEntity();
            w1.setId("BPMN-9901");
            w1.setSourceSystem("BPMN");
            w1.setOriginalTaskId("tsk-9901");
            w1.setTitle("Workdesk Task de prueba (Rojo)");
            w1.setAssignee(null); // Unassigned
            w1.setTenantId("T-100");
            w1.setStatus("OPEN");
            w1.setImpactLevel(9); // Critical
            w1.setSlaExpirationDate(LocalDateTime.now().plusHours(1)); // SLA RED
            w1.setProcessDefinitionKey("credit_approval");
            w1.setCategoryTag("Finance");

            WorkdeskProjectionEntity w2 = new WorkdeskProjectionEntity();
            w2.setId("KANBAN-8802");
            w2.setSourceSystem("KANBAN");
            w2.setOriginalTaskId("kan-8802");
            w2.setTitle("Workdesk Task Despliegue J-04 (Verde)");
            w2.setAssignee("admin"); // Assumed some default test user
            w2.setTenantId("T-100");
            w2.setStatus("IN_PROGRESS");
            w2.setImpactLevel(7);
            w2.setSlaExpirationDate(LocalDateTime.now().plusDays(3)); // SLA GREEN
            w2.setProcessDefinitionKey("sys_agile");
            w2.setCategoryTag("DevOps");

            WorkdeskProjectionEntity w3 = new WorkdeskProjectionEntity();
            w3.setId("BPMN-9903");
            w3.setSourceSystem("BPMN");
            w3.setOriginalTaskId("tsk-9903");
            w3.setTitle("Workdesk Task Revisión Contable (Amarillo)");
            w3.setAssignee("analista_n1");
            w3.setTenantId("T-100");
            w3.setStatus("OPEN");
            w3.setImpactLevel(5);
            w3.setSlaExpirationDate(LocalDateTime.now().plusHours(10)); // SLA YELLOW
            w3.setProcessDefinitionKey("sys_finance");
            w3.setCategoryTag("Finance");

            WorkdeskProjectionEntity w4 = new WorkdeskProjectionEntity();
            w4.setId("BPMN-9904");
            w4.setSourceSystem("BPMN");
            w4.setOriginalTaskId("tsk-9904");
            w4.setTitle("Workdesk Task Auditoría Interna (Gris)");
            w4.setAssignee("analista_n1");
            w4.setTenantId("T-100");
            w4.setStatus("OPEN");
            w4.setImpactLevel(3);
            w4.setSlaExpirationDate(null); // SLA GRAY
            w4.setProcessDefinitionKey("sys_audit");
            w4.setCategoryTag("Audit");

            projectionRepository.save(w1);
            projectionRepository.save(w2);
            projectionRepository.save(w3);
            projectionRepository.save(w4);
            log.info("✅ Workdesk Projections sembradas.");
        }

        log.info("🎉 E2E Data Seed completado exitosamente. Bandejas pobladas.");
    }
}
