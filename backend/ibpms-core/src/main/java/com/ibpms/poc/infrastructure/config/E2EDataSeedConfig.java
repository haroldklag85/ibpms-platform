package com.ibpms.poc.infrastructure.config;

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

@Slf4j
@Configuration
@Profile({ "dev", "e2e", "Zero-Mock-E2E" })
@RequiredArgsConstructor
public class E2EDataSeedConfig implements CommandLineRunner {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("🚀 Inicializando E2E Data Seed para motor Camunda...");

        // Evitar duplicación si el proceso ya está desplegado en un reinicio rápido
        long deploymentCount = repositoryService.createDeploymentQuery()
                .deploymentName("e2e-dummy-deployment")
                .count();

        if (deploymentCount > 0) {
            log.info("✅ El Data Seed E2E ya está desplegado. Saltando...");
            return;
        }

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

        log.info("🎉 E2E Data Seed completado exitosamente. Bandejas pobladas.");
    }
}
